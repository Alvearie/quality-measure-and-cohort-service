/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.flink.KafkaCommon;
import com.ibm.cohort.engine.flink.KafkaInfo;
import com.ibm.cohort.engine.flink.MeasureExecution;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

public class InputProducer {

	private static final Logger LOG = LoggerFactory.getLogger(InputProducer.class);

	public static void main(String[] args) throws Exception {
		ParameterTool params = ParameterTool.fromArgs(args);

		KafkaInfo kafkaInfo = new KafkaInfo(
				params.getRequired("kafkaBrokers"),
				params.getRequired("kafkaTopic"),
				params.getRequired("kafkaPassword")
		);

		LOG.info("Starting...");
		run(
				params.getInt("numRecords"),
				kafkaInfo,
				params.getRequired("measureFile"),
				params.getRequired("patientFile")
		);
		LOG.info("Finished");
	}

	private static void run(int numRecords, KafkaInfo kafkaInfo, String measureFile, String patientFile) throws Exception {
		Random random = new Random(13);
		List<String> measureIds = readFile(measureFile);
		List<String> patientIds = readFile(patientFile);
		Properties properties = KafkaCommon.getProducerProperties(kafkaInfo);
		ObjectMapper objectMapper = new ObjectMapper();

		try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
			for (int i = 0; i < numRecords; i++) {
				String measureId = measureIds.get(random.nextInt(measureIds.size()));
				String patientId = patientIds.get(random.nextInt(patientIds.size()));

				MeasureExecution measureExecution = new MeasureExecution(measureId, patientId);
				String rawJson = objectMapper.writeValueAsString(measureExecution);
				ProducerRecord<String, String> record = new ProducerRecord<>(kafkaInfo.getTopic(), rawJson);
				producer.send(record);

				if (i % 100 == 0 && i > 0) {
					LOG.info("Wrote {} records", i);
				}
			}
		}
	}

	private static List<String> readFile(String filename) throws IOException {
		return Files.lines(Paths.get(filename)).collect(Collectors.toList());
	}

}
