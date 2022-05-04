/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface CodeableConceptWrapper extends ElementWrapper {

    void setCoding(CodingWrapper coding);

}
