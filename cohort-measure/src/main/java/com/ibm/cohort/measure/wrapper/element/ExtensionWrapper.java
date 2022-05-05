/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

import com.ibm.cohort.measure.wrapper.BaseWrapper;

public interface ExtensionWrapper extends ElementWrapper {

    String getUrl();
    void setUrl(String url);

    BaseWrapper getValue();
    void setValue(BaseWrapper value);

}
