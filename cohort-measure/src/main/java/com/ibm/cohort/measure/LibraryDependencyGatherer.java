/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure;

import java.util.List;

public interface LibraryDependencyGatherer<L, M> {

    List<L> gatherForLibraryId(String rootLibraryId);

    List<L> gatherForMeasure(M measure);

}
