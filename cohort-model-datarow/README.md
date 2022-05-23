# Cohort Model DataRow

This module defines a flat "key-value" based data model and contains classes that integrate said data model into the CQL engine.
The primary implementation of this model is based on Spark's `Row` class (found in `cohort-evaluator-spark`), but other implementations can be created as well.
