package com.ibm.cohort.cql.spark;

import java.io.Serializable;

import org.apache.spark.sql.SparkSession;

public class BaseSparkTest implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum Java8API {
        ENABLED(true), DISABLED(false);
        
        boolean value;
        private Java8API(boolean value) {
            this.value = value;
        }
        public boolean getValue() {
            return this.value;
        }
    };
        
    protected SparkSession initializeSession(Java8API java8APIEnabled) {
        return SparkSession.builder()
                .appName("Local Application")
                .master("local[2]")
                .config("spark.sql.datetime.java8API.enabled", String.valueOf(java8APIEnabled.getValue()))
                .config("spark.sql.sources.default", "delta")
                .getOrCreate();
    }
}
