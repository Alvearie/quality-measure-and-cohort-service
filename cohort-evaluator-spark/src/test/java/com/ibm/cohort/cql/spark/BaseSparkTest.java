package com.ibm.cohort.cql.spark;

import java.io.Serializable;
import java.util.Map;

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
        return initializeSession(java8APIEnabled, null);
    }
    
    protected SparkSession initializeSession(Java8API java8APIEnabled, Map<String,String> overrides) {
        SparkSession.Builder builder = SparkSession.builder()
                .appName("Local Application")
                .master("local[4]")
                .config("spark.sql.datetime.java8API.enabled", String.valueOf(java8APIEnabled.getValue()))
                .config("spark.sql.sources.default", "delta");
        
        if( overrides != null ) {
            overrides.entrySet().stream().forEach( e -> {
                builder.config( e.getKey(), e.getValue() );
            });
        }

        return builder.getOrCreate();
    }
}
