/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 *  A method annotated with this annotation will be excluded
 *  from Jacoco's code coverage requirements.
 *  
 *  Intended use is for excluding generated methods for classes
 *  such as getters, setters, and hashcode.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Generated {
}
