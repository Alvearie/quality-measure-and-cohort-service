# Cohort Evaluator Spark

This project contains a Spark Job that encapsulates reading data from flat files, organizing the data into context groupings, and then executing Clinical Quality Language (CQL) queries against those context groups. A single output is generated for each context grouping where the columns of the result record are the result of each inidivual CQL expression evaluated. The program is intelligent enough to translate raw CQL to ELM provided an appropriate model info artifact is provided.

Context groupings are described using a context-defintions.json file and the CQL libraries to evaluate, with input parameters, are described in a cql-jobs.json file. See src/test/resources for examples.

See our [official documentation](https://alvearie.io/quality-measure-and-cohort-service/#/user-guide/spark-user-guide) for more information on the Spark cohort usecase.
