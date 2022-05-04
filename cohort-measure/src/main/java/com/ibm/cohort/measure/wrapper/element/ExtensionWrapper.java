/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.wrapper.element;

public interface ExtensionWrapper extends ElementWrapper {

    String getUrl();
    void setUrl(String url);

    ElementWrapper getValue();
//    void setValue(ElementWrapper value);

}
