/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import java.util.Objects;

public class MultiManyToMany extends ManyToMany {

    private ManyToMany with;

    public ManyToMany getWith() {
        return with;
    }

    public void setWith(ManyToMany with) {
        this.with = with;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MultiManyToMany that = (MultiManyToMany) o;
        return Objects.equals(with, that.with);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), with);
    }

}
