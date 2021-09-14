package com.ibm.cohort.cql.spark;

import java.io.Serializable;
import java.util.Map;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.delta.DeltaLog;
import org.junit.Before;

public class BaseSparkTest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Java8API {
        ENABLED(true), DISABLED(false);
        
        boolean value;
        Java8API(boolean value) {
            this.value = value;
        }
        public boolean getValue() {
            return this.value;
        }
    }

    @Before
    public void setup() {
        // We need to clear DeltaLake's RDD cache in between each test.
        // If we do not, we run the risk of a test accidentally using a closed SparkContext.
        DeltaLog.clearCache();
    }

    protected static SparkSession initializeSession() {
        return initializeSession(Java8API.ENABLED);
    }

    protected static SparkSession initializeSession(Java8API java8APIEnabled) {
        SparkSession.Builder builder = SparkSession.builder()
                .appName("Local Application")
                .master("local[4]")
                .config("spark.sql.datetime.java8API.enabled", String.valueOf(java8APIEnabled.getValue()))
                .config("spark.sql.sources.default", "delta");

        return builder.getOrCreate();
    }
}
