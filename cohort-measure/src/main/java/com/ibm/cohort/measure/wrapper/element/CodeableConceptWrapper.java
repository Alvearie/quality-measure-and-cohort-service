/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import java.util.List;

public interface CodeableConceptWrapper extends ElementWrapper {

    List<CodingWrapper> getCoding();
    void setCoding(List<CodingWrapper> coding);
    void addCoding(CodingWrapper coding);

    // KWAS TODO: Implement as a helper method on a base class?
    boolean hasCoding(String system, String code);

    void setText(String text);

}
