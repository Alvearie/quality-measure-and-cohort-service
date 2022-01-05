/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.terminology;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import java.util.ArrayList;

public class DefaultCqlTerminologyProviderTest {

    @Test
    public void in() {
        Code code = new Code();
        ValueSetInfo vsi = new ValueSetInfo();
        boolean expected = true;

        TerminologyProvider mock = Mockito.mock(TerminologyProvider.class);
        Mockito.when(mock.in(code, vsi))
                .thenReturn(expected);

        DefaultCqlTerminologyProvider provider = new DefaultCqlTerminologyProvider(mock);
        Assert.assertEquals(expected, provider.in(code, vsi));
    }

    @Test
    public void expand() {
        ValueSetInfo vsi = new ValueSetInfo();
        Iterable<Code> expected = new ArrayList<>();

        TerminologyProvider mock = Mockito.mock(TerminologyProvider.class);
        Mockito.when(mock.expand(vsi))
                .thenReturn(expected);

        DefaultCqlTerminologyProvider provider = new DefaultCqlTerminologyProvider(mock);
        Assert.assertEquals(expected, provider.expand(vsi));
    }

    @Test
    public void lookup() {
        Code code = new Code();
        CodeSystemInfo csi = new CodeSystemInfo();
        Code expected = new Code();

        TerminologyProvider mock = Mockito.mock(TerminologyProvider.class);
        Mockito.when(mock.lookup(code, csi))
                .thenReturn(expected);

        DefaultCqlTerminologyProvider provider = new DefaultCqlTerminologyProvider(mock);
        Assert.assertEquals(expected, provider.lookup(code, csi));
    }

}
