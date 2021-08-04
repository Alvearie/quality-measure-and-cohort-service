package com.ibm.cohort.cql.spark.data;

import org.opencds.cqf.cql.engine.data.CompositeDataProvider;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.datarow.engine.DataRowModelResolver;
import com.ibm.cohort.datarow.engine.DataRowRetrieveProvider;

public class DataRowDataProvider extends CompositeDataProvider implements CqlDataProvider {

    public DataRowDataProvider(DataRowRetrieveProvider retrieveProvider) {
        super(new DataRowModelResolver(), retrieveProvider);
    }
}
