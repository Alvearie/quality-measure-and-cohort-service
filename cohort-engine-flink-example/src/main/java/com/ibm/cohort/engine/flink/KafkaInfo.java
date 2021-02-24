/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink;

public class KafkaInfo {

	private String brokers;
	private String topic;
	private String password;

	public KafkaInfo() {
	}

	public KafkaInfo(String brokers, String topic, String password) {
		this.brokers = brokers;
		this.topic = topic;
		this.password = password;
	}

	public String getBrokers() {
		return brokers;
	}

	public void setBrokers(String brokers) {
		this.brokers = brokers;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "KafkaInfo{" +
				"brokers='" + brokers + '\'' +
				", topic='" + topic + '\'' +
				", password='redacted'" +
				'}';
	}
}
