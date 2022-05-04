/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure;

import com.ibm.cohort.cql.evaluation.parameters.Parameter;

public interface ParameterConverter<T> {

    Parameter toCohortParameter(T rawParameter);

}
