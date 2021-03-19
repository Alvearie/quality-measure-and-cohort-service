/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink.execution;

import java.io.Serializable;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.ibm.cohort.engine.flink.KafkaCommon;
import com.ibm.cohort.engine.flink.KafkaInfo;
import com.ibm.cohort.engine.flink.MeasureExecution;
import com.ibm.cohort.engine.measure.FHIRClientContext;
import com.ibm.cohort.engine.measure.MeasureContext;
import com.ibm.cohort.engine.measure.MeasureEvaluator;
import com.ibm.cohort.engine.measure.R4MeasureEvaluatorBuilder;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheKey;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.DefaultRetrieveCacheContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import org.hl7.fhir.r4.model.MeasureReport;

public class CohortEngineFlinkDriver implements Serializable {

	private static final long serialVersionUID = 1966474691011266880L;

	// Remove this when adding proper evidence support to the flink job
	private static final MeasureEvidenceOptions NO_EVIDENCE_OPTIONS = new MeasureEvidenceOptions();

	public static void main(String[] args) throws Exception {
		ParameterTool params = ParameterTool.fromArgs(args);

		FHIRServerInfo fhirServerInfo = new FHIRServerInfo(
				params.getRequired("fhir-tenant-id"),
				params.getRequired("fhir-username"),
				params.getRequired("fhir-password"),
				params.getRequired("fhir-endpoint")
		);

		KafkaInfo kafkaInputInfo = new KafkaInfo(
				params.getRequired("kafka-brokers"),
				params.getRequired("kafka-input-topic"),
				params.getRequired("kafka-password")
		);

		KafkaInfo kafkaOutputInfo = null;
		if (params.has("kafka-output-topic")) {
			kafkaOutputInfo = new KafkaInfo(
					params.getRequired("kafka-brokers"),
					params.getRequired("kafka-output-topic"),
					params.getRequired("kafka-password")
			);
		}

		boolean enableRetrieveCache = params.has("enable-retrieve-cache");
		RetrieveCacheConfiguration retrieveCacheConfiguration = new RetrieveCacheConfiguration(
				params.getInt("max-retrieve-cache-size", 1_000),
				params.getInt("retrieve-cache-expire-on-write", 300),
				params.has("enable-retrieve-cache-statistics")
		);

		CohortEngineFlinkDriver example = new CohortEngineFlinkDriver(
				fhirServerInfo,
				enableRetrieveCache ? retrieveCacheConfiguration : null
		);
		example.run(
				params.get("job-name", "cohort-engine"),
				params.getRequired("kafka-group-id"),
				kafkaInputInfo,
				kafkaOutputInfo,
				params.has("print-output-to-console"),
				params.has("rebalance-input"),
				params.has("read-from-start")
		);
	}

	private final FHIRServerInfo fhirServerInfo;
	private final RetrieveCacheConfiguration retrieveCacheConfiguration;

	private transient MeasureEvaluator evaluator;
	private transient FhirContext fhirContext;
	private transient ObjectMapper objectMapper;
	private transient RetrieveCacheContext retrieveCacheContext;

	public CohortEngineFlinkDriver(FHIRServerInfo fhirServerInfo, RetrieveCacheConfiguration retrieveCacheConfiguration) {
		this.fhirServerInfo = fhirServerInfo;
		this.retrieveCacheConfiguration = retrieveCacheConfiguration;
	}

	private void run(
			String jobName,
			String kafkaGroupId,
			KafkaInfo kafkaInputInfo,
			KafkaInfo kafkaOutputInfo,
			boolean printOutputToConsole,
			boolean rebalanceInput,
			boolean readFromStart
	) throws Exception {
		FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
				kafkaInputInfo.getTopic(),
				new SimpleStringSchema(),
				KafkaCommon.getConsumerProperties(kafkaInputInfo, kafkaGroupId)
		);
		if (readFromStart) {
			consumer.setStartFromEarliest();
		}

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<String> stream = env.addSource(consumer);
		if (rebalanceInput) {
			stream = stream.rebalance();
		}

		stream = stream
				.map(this::deserializeMeasureExecution)
				.map(this::evaluate)
				.flatMap((List<String> measureReports, Collector<String> collector) -> measureReports.forEach(collector::collect))
				.returns(String.class);

		if (printOutputToConsole) {
			stream.print();
		}

		if (kafkaOutputInfo != null) {
			FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<>(
					kafkaOutputInfo.getTopic(),
					new SimpleStringSchema(),
					KafkaCommon.getProducerProperties(kafkaOutputInfo)
			);
			stream.addSink(producer);
		}

		env.execute(jobName);
	}

	private List<String> evaluate(MeasureExecution execution) {
		MeasureEvaluator evaluator = getEvaluator();

		List<MeasureContext> measureContexts = execution.getMeasureIds().stream()
				.map(MeasureContext::new)
				.collect(Collectors.toList());

		List<MeasureReport> result = evaluator.evaluatePatientMeasures(
				execution.getPatientId(),
				measureContexts,
				NO_EVIDENCE_OPTIONS
		);

		FhirContext fhirContext = getFhirContext();
		IParser jsonParser = fhirContext.newJsonParser();

		return result.stream()
				.map(jsonParser::encodeResourceToString)
				.collect(Collectors.toList());
	}

	private ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}

	private FhirContext getFhirContext() {
		if (fhirContext == null) {
			fhirContext = FhirContext.forR4();
		}
		return fhirContext;
	}

	private MeasureEvaluator getEvaluator() {
		if (evaluator == null) {
			evaluator = createEvaluator();
		}
		return evaluator;
	}

	private RetrieveCacheContext getRetrieveCacheContext() {
		if (retrieveCacheContext == null) {
			retrieveCacheContext = createRetrieveCacheContext();
		}
		return retrieveCacheContext;
	}

	private MeasureEvaluator createEvaluator() {
		FhirClientBuilderFactory factory = new DefaultFhirClientBuilderFactory();

		FhirClientBuilder clientBuilder = factory.newFhirClientBuilder(getFhirContext());
		IGenericClient genericClient = clientBuilder.createFhirClient(fhirServerInfo.toIbmServerConfig());

		FHIRClientContext clientContext = new FHIRClientContext.Builder()
				.withDefaultClient(genericClient)
				.build();
		R4MeasureEvaluatorBuilder evalBuilder = new R4MeasureEvaluatorBuilder().withClientContext(clientContext);
		if (retrieveCacheConfiguration != null) {
			evalBuilder.withRetrieveCacheContext(getRetrieveCacheContext());
		}
		return evalBuilder.build();
	}

	private MeasureExecution deserializeMeasureExecution(String input) throws Exception {
		ObjectMapper mapper = getObjectMapper();
		return mapper.readValue(input, MeasureExecution.class);
	}

	private RetrieveCacheContext createRetrieveCacheContext() {
		CaffeineConfiguration<RetrieveCacheKey, Iterable<Object>> cacheConfig = new CaffeineConfiguration<>();
		cacheConfig.setMaximumSize(OptionalLong.of(retrieveCacheConfiguration.getMaxSize()));
		cacheConfig.setExpireAfterWrite(OptionalLong.of(TimeUnit.SECONDS.toNanos(retrieveCacheConfiguration.getExpireOnWrite())));
		cacheConfig.setStatisticsEnabled(retrieveCacheConfiguration.isEnableStatistics());
		return new DefaultRetrieveCacheContext(cacheConfig);
	}

}
