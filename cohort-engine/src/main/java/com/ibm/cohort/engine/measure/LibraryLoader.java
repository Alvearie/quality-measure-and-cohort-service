package com.ibm.cohort.engine.measure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Attachment;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;

import com.helger.commons.base64.Base64;
import com.ibm.cohort.engine.CqlTranslatorWrapper;

public class LibraryLoader implements org.opencds.cqf.cql.engine.execution.LibraryLoader {

	private Map<String, Library> libraries = new HashMap<>();

	private LibraryResolutionProvider<org.hl7.fhir.r4.model.Library> provider;
	private CqlTranslatorWrapper translationProvider;

	public LibraryLoader(LibraryResolutionProvider<org.hl7.fhir.r4.model.Library> provider,
			CqlTranslatorWrapper translationProvider) {
		this.provider = provider;
		this.translationProvider = translationProvider;
	}

	public Library resolveLibrary(VersionedIdentifier libraryIdentifier) {
		String key = mangleIdentifier(libraryIdentifier);
		Library library = libraries.get(key);
		if (library == null) {
			library = loadLibrary(libraryIdentifier);
			libraries.put(key, library);
		}
		return library;
	}

	public Library loadLibrary(VersionedIdentifier libraryIdentifier) {
		Library elmLibrary = null;

		org.hl7.fhir.r4.model.Library fhirLibrary = provider.resolveLibraryByName(libraryIdentifier.getId(),
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
			try {
				elmLibrary = CqlLibraryReader.read(getAttachmentData(attachment));
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
				try {
					elmLibrary = translationProvider.translate(getAttachmentData(attachment));
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

	protected InputStream getAttachmentData(Attachment attachment) throws IOException {
		return new ByteArrayInputStream(Base64.decode(attachment.getData()));
	}

	@Override
	public Library load(VersionedIdentifier libraryIdentifier) {
		return resolveLibrary(libraryIdentifier);
	}

	protected String mangleIdentifier(VersionedIdentifier libraryIdentifier) {
		String id = libraryIdentifier.getId();
		String version = libraryIdentifier.getVersion();

		return version == null ? id : id + "-" + version;
	}

}
