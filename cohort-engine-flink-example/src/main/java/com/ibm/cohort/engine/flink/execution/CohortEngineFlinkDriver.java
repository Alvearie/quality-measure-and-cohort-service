/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink.execution;

import java.io.Serializable;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.flink.KafkaCommon;
import com.ibm.cohort.engine.flink.KafkaInfo;
import com.ibm.cohort.engine.flink.MeasureExecution;
import com.ibm.cohort.engine.measure.MeasureContext;
import com.ibm.cohort.engine.measure.MeasureEvaluator;
import com.ibm.cohort.engine.measure.ProviderFactory;
import com.ibm.cohort.engine.measure.cache.RetrieveCacheContext;
import com.ibm.cohort.engine.measure.cache.TransientRetrieveCacheContext;
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
import org.hl7.fhir.r4.model.MeasureReport;
import org.opencds.cqf.common.evaluation.EvaluationProviderFactory;

public class CohortEngineFlinkDriver implements Serializable {

	private static final long serialVersionUID = 1966474691011266880L;

	// Remove this when adding proper evidence support to the flink job
	private static final MeasureEvidenceOptions NO_EVIDENCE_OPTIONS = new MeasureEvidenceOptions();

	public static void main(String[] args) throws Exception {
		ParameterTool params = ParameterTool.fromArgs(args);

		FHIRServerInfo fhirServerInfo = new FHIRServerInfo(
				params.getRequired("fhirTenantId"),
				params.getRequired("fhirUsername"),
				params.getRequired("fhirPassword"),
				params.getRequired("fhirEndpoint")
		);

		KafkaInfo kafkaInputInfo = new KafkaInfo(
				params.getRequired("kafkaBrokers"),
				params.getRequired("kafkaInputTopic"),
				params.getRequired("kafkaPassword")
		);

		KafkaInfo kafkaOutputInfo = null;
		if (params.has("kafkaOutputTopic")) {
			kafkaOutputInfo = new KafkaInfo(
					params.getRequired("kafkaBrokers"),
					params.getRequired("kafkaOutputTopic"),
					params.getRequired("kafkaPassword")
			);
		}

		CohortEngineFlinkDriver example = new CohortEngineFlinkDriver(fhirServerInfo);
		example.run(
				params.get("jobName", "cohort-engine"),
				params.getRequired("kafkaGroupId"),
				kafkaInputInfo,
				kafkaOutputInfo,
				params.has("printOutputToConsole"),
				params.has("rebalanceInput"),
				params.has("readFromStart")
		);
	}

	private final FHIRServerInfo fhirServerInfo;

	private transient MeasureEvaluator evaluator;
	private transient FhirContext fhirContext;
	private transient ObjectMapper objectMapper;

	public CohortEngineFlinkDriver(FHIRServerInfo fhirServerInfo) {
		this.fhirServerInfo = fhirServerInfo;
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
				.map(this::evaluate);

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

	private String evaluate(MeasureExecution execution) {
		MeasureEvaluator evaluator = getEvaluator();

		MeasureReport result = evaluator.evaluatePatientMeasure(
				new MeasureContext(execution.getMeasureId()),
				execution.getPatientId(),
				NO_EVIDENCE_OPTIONS
		);

		FhirContext fhirContext = getFhirContext();
		IParser jsonParser = fhirContext.newJsonParser();

		return jsonParser.encodeResourceToString(result);
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

	private MeasureEvaluator createEvaluator() {
		FhirClientBuilderFactory factory = new DefaultFhirClientBuilderFactory();

		FhirClientBuilder builder = factory.newFhirClientBuilder(getFhirContext());
		IGenericClient genericClient = builder.createFhirClient(fhirServerInfo.toIbmServerConfig());

		RetrieveCacheContext retrieveCacheContext = new TransientRetrieveCacheContext();
		EvaluationProviderFactory providerFactory = new ProviderFactory(genericClient, genericClient, retrieveCacheContext);

		return new MeasureEvaluator(providerFactory, genericClient);
	}

	private MeasureExecution deserializeMeasureExecution(String input) throws Exception {
		ObjectMapper mapper = getObjectMapper();
		return mapper.readValue(input, MeasureExecution.class);
	}

}
