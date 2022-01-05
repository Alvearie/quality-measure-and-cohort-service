/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.hapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.provider.ResolverLibraryResolutionProvider;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.cql.provider.CqlLibrarySourceProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.provider.SourceProviderBasedCqlLibrarySourceProvider;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Attachment;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.common.providers.LibrarySourceProvider;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;

import com.ibm.cohort.cql.OptimizedCqlLibraryReader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

/**
 * Implementation of a {@link LibraryLoader} that resolves FHIR R4 {@link Library}
 * resources using a {@link FhirResourceResolver}. Library resources are
 * expected to have either an XML-formatted ELM (application/elm+xml) or raw
 * CQL (text/cql) attachment. The ELM is preferred. When only CQL is present
 * the loader will automatically translate to ELM using the configured
 * translation provider
 */
public class R4TranslatingLibraryLoader implements LibraryLoader {

	private final FhirResourceResolver<org.hl7.fhir.r4.model.Library> resolver;
	private final CqlToElmTranslator translator;

	public R4TranslatingLibraryLoader(FhirResourceResolver<org.hl7.fhir.r4.model.Library> resolver, CqlToElmTranslator translator) {
		this.resolver = resolver;
		this.translator = translator;
	}

	@Override
	public Library load(VersionedIdentifier libraryIdentifier) {
		Library elmLibrary = null;

		org.hl7.fhir.r4.model.Library fhirLibrary = resolver.resolveByName(libraryIdentifier.getId(),
				libraryIdentifier.getVersion());
		if (fhirLibrary == null) {
			throw new IllegalArgumentException(String.format("Library %s-%s not found", libraryIdentifier.getId(),
					libraryIdentifier.getVersion()));
		}
		Map<String, Attachment> mimeTypeIndex = new HashMap<>();

		for (Attachment attachment : fhirLibrary.getContent()) {
			if (attachment.hasContentType()) {
				mimeTypeIndex.put(attachment.getContentType(), attachment);
			} else {
				throw new IllegalArgumentException(
						String.format("Library %s-%s contains an attachment with no content type",
								libraryIdentifier.getId(), libraryIdentifier.getVersion()));
			}
		}

		Attachment attachment = mimeTypeIndex.get("application/elm+xml");
		if (attachment != null) {
			try (InputStream is = getAttachmentDataAsStream(attachment)){
				elmLibrary = CqlLibraryReader.read(is);
			} catch (Exception ex) {
				throw new IllegalArgumentException(String.format("Library %s-%s elm attachment failed to deserialize",
						libraryIdentifier.getId(), libraryIdentifier.getVersion()), ex);
			}
		}

		if (elmLibrary == null) {
			attachment = mimeTypeIndex.get("text/cql");
			if (attachment == null) {
				throw new IllegalArgumentException(
						String.format("Library %s-%s must contain either a application/elm+xml or text/cql attachment",
								libraryIdentifier.getId(), libraryIdentifier.getVersion()));
			} else {
				String content = getAttachmentDataAsString(attachment);
				try {
					elmLibrary = OptimizedCqlLibraryReader.read(translateLibrary(content, libraryIdentifier));
				} catch (Exception ex) {
					throw new IllegalArgumentException(
							String.format("Library %s-%s cql attachment failed to deserialize",
									libraryIdentifier.getId(), libraryIdentifier.getVersion()),
							ex);
				}
			}
		}

		return elmLibrary;
	}

	private String translateLibrary(String content, VersionedIdentifier libraryIdentifier) {
		LibraryResolutionProvider<org.hl7.fhir.r4.model.Library> libraryResolutionProvider = new ResolverLibraryResolutionProvider<>(resolver);
		LibrarySourceProvider<org.hl7.fhir.r4.model.Library, Attachment> librarySourceProvider = new LibrarySourceProvider<>(
				libraryResolutionProvider,
				org.hl7.fhir.r4.model.Library::getContent,
				Attachment::getContentType,
				Attachment::getData
		);
		CqlLibrarySourceProvider cqlLibrarySourceProvider = new SourceProviderBasedCqlLibrarySourceProvider(librarySourceProvider);

		CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
				.setFormat(Format.CQL)
				.setLibraryId(libraryIdentifier.getId())
				.setVersion(libraryIdentifier.getVersion());
		CqlLibrary library = new CqlLibrary()
				.setDescriptor(descriptor)
				.setContent(content);

		return translator.translate(library, cqlLibrarySourceProvider)
				.getMainLibrary()
				.getContent();
	}

	private InputStream getAttachmentDataAsStream(Attachment attachment) {
		byte[] content = attachment.getData();
		return new ByteArrayInputStream(content);
	}

	private String getAttachmentDataAsString(Attachment attachment) {
		byte[] content = attachment.getData();
		return new String(content, StandardCharsets.UTF_8);
	}

}
