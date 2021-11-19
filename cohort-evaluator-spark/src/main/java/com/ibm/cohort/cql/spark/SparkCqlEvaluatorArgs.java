/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

/**
 * Command-line arguments for the SparkCqlEvaluator program.
 */
public class SparkCqlEvaluatorArgs implements Serializable {
    private static final long serialVersionUID = 1L;

    @Parameter(names = { "-h", "--help" }, description = "Print help text", help = true)
    public boolean help;

    @Parameter(names = { "-d",
            "--context-definitions" }, description = "Filesystem path to the context-definitions file.", required = true)
    public String contextDefinitionPath;

    @Parameter(names = { "--input-format" }, description = "Spark SQL format identifier for input files. If not provided, the value of spark.sql.datasources.default is used.", required = false)
    public String inputFormat;
    
    @Parameter(names = { "--disable-column-filter" }, description = "Disable CQL-based column filtering. When specified, all columns of the Spark input data are read regardless of whether or not they are needed by the CQL queries being evaluated.", required = false)
    public boolean disableColumnFiltering = false;
    
    @DynamicParameter(names = { "-i",
            "--input-path" }, description = "Key-value pair of resource=URI controlling where Spark should read resources referenced in the context definitions file will be read from. Specify multiple files by providing a separate option for each input.", required = true)
    public Map<String, String> inputPaths = new HashMap<>();

    @Parameter(names = { "--output-format" }, description = "Spark SQL format identifier for output files. If not provided, the value of spark.sql.datasources.default is used.", required = false)
    public String outputFormat;
    
    @DynamicParameter(names = { "-o",
            "--output-path" }, description = "Key-value pair of context=URI controlling where Spark should write the results of CQL evaluation requests. Specify multiple files by providing a separate option for each output.", required = true)
    public Map<String, String> outputPaths = new HashMap<>();

    @Parameter(names = { "-j", "--jobs" }, description = "Filesystem path to the CQL job file", required = true)
    public String jobSpecPath;

    @Parameter(names = { "-m",
            "--model-info" }, description = "Filesystem path(s) to custom model-info files that may be required for CQL translation.", required = true)
    public List<String> modelInfoPaths = new ArrayList<>();

    @Parameter(names = { "-c",
            "--cql-path" }, description = "Filesystem path to the location containing the CQL libraries referenced in the jobs file.", required = true)
    public String cqlPath;

    @Parameter(names = { "-a",
            "--aggregation-contexts" }, description = "One or more context names, as defined in the context-definitions file, that should be run in this evaluation. Defaults to all evaluations.", required = false)
    public List<String> aggregationContexts = new ArrayList<>();

    @DynamicParameter(names = { "-l",
            "--library" }, description = "One or more library=version key-value pair(s), as defined in the jobs file, that describe the libraries that should be run in this evaluation. Defaults to all libraries. Specify multiple libraries by providing a separate option for each library.", required = false)
    public Map<String, String> libraries = new HashMap<>();

    @Parameter(names = { "-e",
            "--expressions" }, description = "One or more expression names, as defined in the context-definitions file, that should be run in this evaluation. Defaults to all expressions.", required = false)
    public List<String> expressions = new ArrayList<>();

    @Parameter(names = { "-n",
            "--output-partitions" }, description = "Number of partitions to use when storing data", required = false)
    public Integer outputPartitions = null;
    
    @Parameter(names = {"--overwrite-output-for-contexts"}, description = "WARNING: NOT RECOMMENDED FOR PRODUCTION USE. If option is set, program overwrites existing output when writing result data.")
    public boolean overwriteResults = false;

    @Parameter(names = {"--default-output-column-delimiter"}, description = "Delimiter to use when a result column is named using the default naming rule of `LIBRARY_ID + delimiter + DEFINE_NAME`.")
    public String defaultOutputColumnDelimiter = "|";
    
    @Parameter(names = { "-t",
    "--terminology-path" }, description = "Filesystem path to the location containing the ValueSet definitions in FHIR XML or JSON format.")
    public String terminologyPath;

    @Parameter(names = {"--metadata-output-path"}, description = "Folder where program output metadata (a batch summary file and possible _SUCCESS marker file) will be written.", required = true)
    public String metadataOutputPath = null;

    @Parameter(names = {"--halt-on-error"}, description = "If set, errors during CQL evaluations will cause the program to halt. Otherwise, errors are collected and reported in the program's batch summary file and will not cause the program to halt.")
    public Boolean haltOnError = false;
    
    @Parameter(names = { "--debug" }, description = "Enables CQL debug logging")
    public boolean debug = false;

    @Parameter(names = { "--disable-result-grouping" }, description = "Disable use of CQL parameters to group context results into separate rows", required = false)
    public boolean disableResultGrouping = false;
    
    @Parameter(names = { "--key-parameters" }, description = "One or more parameter names that should be included in the parameters column for output rows that are generated.", required = false)
    public List<String> keyParameterNames = null;
}
