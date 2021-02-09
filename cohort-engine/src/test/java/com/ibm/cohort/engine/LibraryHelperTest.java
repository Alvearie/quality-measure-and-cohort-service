/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.junit.Test;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

import com.ibm.cohort.engine.measure.LibraryHelper;


public class LibraryHelperTest {
	@Test
	public void graph_with_cycles___no_infinite_loop() {
		
		@SuppressWarnings("unchecked")
		LibraryResolutionProvider<Library> llp = mock(LibraryResolutionProvider.class);
		LibraryLoader ll = mock(LibraryLoader.class);
		
		Library parent = new Library();
		parent.setId("Parent");
		
		Library child = new Library();
		child.setId("Child");
		
		parent.addRelatedArtifact(asRelation(child));
		child.addRelatedArtifact(asRelation(parent));
		
		when(llp.resolveLibraryById("Parent")).thenReturn(parent);
		when(llp.resolveLibraryById("Child")).thenReturn(child);
		when(ll.load(any())).thenReturn( new org.cqframework.cql.elm.execution.Library() );
		
		List<org.cqframework.cql.elm.execution.Library> loaded = LibraryHelper.loadLibraries(parent, ll, llp);
		assertEquals( 2, loaded.size() );
	}

	protected RelatedArtifact asRelation(Library library) {
		return new RelatedArtifact().setType( RelatedArtifact.RelatedArtifactType.DEPENDSON ).setResource("Library/" + library.getId());
	}
}
