/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink.execution;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.flink.KafkaCommon;
import com.ibm.cohort.engine.flink.KafkaInfo;
import com.ibm.cohort.engine.flink.MeasureExecution;
import com.ibm.cohort.engine.measure.FHIRClientContext;
import com.ibm.cohort.engine.measure.MeasureContext;
import com.ibm.cohort.engine.measure.MeasureEvaluator;
import com.ibm.cohort.engine.measure.R4MeasureEvaluatorBuilder;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.DefaultRetrieveCacheContext;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.engine.r4.cache.CachingR4FhirModelResolver;

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

		CohortEngineFlinkDriver example = new CohortEngineFlinkDriver(
				fhirServerInfo,
				params.has("disable-retrieve-cache")
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
	private final boolean disableRetrieveCache;

	private transient MeasureEvaluator evaluator;
	private transient FhirContext fhirContext;
	private transient ObjectMapper objectMapper;
	private transient RetrieveCacheContext retrieveCacheContext;

	public CohortEngineFlinkDriver(FHIRServerInfo fhirServerInfo, boolean disableRetrieveCache) {
		this.fhirServerInfo = fhirServerInfo;
		this.disableRetrieveCache = disableRetrieveCache;
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
			retrieveCacheContext = new DefaultRetrieveCacheContext();
		}
		return retrieveCacheContext;
	}

	private MeasureEvaluator createEvaluator() {
		FHIRClientContext clientContext = new FHIRClientContext.Builder()
				.withDefaultClient(fhirServerInfo.toIbmServerConfig())
				.build();
		R4MeasureEvaluatorBuilder evalBuilder = new R4MeasureEvaluatorBuilder()
				.withClientContext(clientContext)
				.withModelResolver(new CachingR4FhirModelResolver());
		if (!disableRetrieveCache) {
			evalBuilder.withRetrieveCacheContext(getRetrieveCacheContext());
		}
		return evalBuilder.build();
	}

	private MeasureExecution deserializeMeasureExecution(String input) throws Exception {
		ObjectMapper mapper = getObjectMapper();
		return mapper.readValue(input, MeasureExecution.class);
	}

}
