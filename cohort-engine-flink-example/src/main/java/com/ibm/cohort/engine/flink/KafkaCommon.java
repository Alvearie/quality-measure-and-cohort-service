/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaCommon {

	public static Properties getConsumerProperties(KafkaInfo kafkaInfo, String groupId) {
		Properties configs = getCommonProperties(kafkaInfo);

		configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		configs.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG,"use_all_dns_ips");

		return configs;
	}

	public static Properties getProducerProperties(KafkaInfo kafkaInfo) {
		Properties configs = getCommonProperties(kafkaInfo);

		configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		configs.put(ProducerConfig.ACKS_CONFIG, "all");
		configs.put(ProducerConfig.CLIENT_DNS_LOOKUP_CONFIG,"use_all_dns_ips");

		return configs;
	}

	private static Properties getCommonProperties(KafkaInfo kafkaInfo) {
		Properties configs = new Properties();

		configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaInfo.getBrokers());
		configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		configs.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		configs.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\"" + kafkaInfo.getPassword() + "\";");
		configs.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
		configs.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2");
		configs.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");

		return configs;
	}
}
