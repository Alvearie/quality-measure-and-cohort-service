/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelInfoLoader;
import org.cqframework.cql.cql2elm.ModelInfoProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class CqlToElmTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(CqlToElmTranslator.class);

    public void registerModelInfo(Reader modelInfoXML) {
        final ModelInfo modelInfo = JAXB.unmarshal(modelInfoXML, ModelInfo.class);
        final VersionedIdentifier modelId = new VersionedIdentifier().withId(modelInfo.getName())
                .withVersion(modelInfo.getVersion());
        final ModelInfoProvider modelProvider = () -> modelInfo;
        ModelInfoLoader.registerModelInfoProvider(modelId, modelProvider);
    }

    public CqlTranslationResult translate(CqlLibrary primaryLibrary, CqlLibrarySourceProvider sourceProvider) {
        LibraryBuilder.SignatureLevel signatureLevel = LibraryBuilder.SignatureLevel.None;

        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager);

        libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);

        Collection<CqlLibrary> dependencies = new ArrayList<>();

        CqlTranslator cqlTranslator = CqlTranslator.fromText(primaryLibrary.getContent(), modelManager, libraryManager,
                /* ucumService= */null, CqlTranslatorException.ErrorSeverity.Info, signatureLevel);

        if (cqlTranslator.getErrors().size() > 0) {
            throw new CqlTranslatorException("There were errors during cql translation: " + formatMsg(cqlTranslator.getErrors()));
        } else if (cqlTranslator.getExceptions().size() > 0) {
            throw new CqlTranslatorException(
                    "There were exceptions during cql translation: " + formatMsg(cqlTranslator.getExceptions()));
        } else if (cqlTranslator.getWarnings().size() > 0) {
            String msg = "There were warnings during cql translation:\n"
                    + cqlTranslator.getWarnings().stream().map(Object::toString).collect(Collectors.joining("\n"));
            LOG.warn(msg);
        }

        CqlLibrary translatedLibrary = new CqlLibrary().setDescriptor(new CqlLibraryDescriptor()
                .setLibraryId(primaryLibrary.getDescriptor().getLibraryId())
                .setVersion(primaryLibrary.getDescriptor().getVersion()).setFormat(CqlLibraryDescriptor.Format.ELM))
                .setContent(cqlTranslator.toXml());

        try {
            for (TranslatedLibrary tl : libraryManager.getTranslatedLibraries().values()) {
                CqlLibrary library = new CqlLibrary()
                        .setDescriptor(new CqlLibraryDescriptor().setLibraryId(tl.getLibrary().getIdentifier().getId())
                                .setVersion(tl.getLibrary().getIdentifier().getVersion())
                                .setFormat(CqlLibraryDescriptor.Format.ELM))
                        .setContent(cqlTranslator.convertToXml(tl.getLibrary()));

                dependencies.add(library);
            }
        } catch (JAXBException ex) {
            throw new RuntimeException("ELM serialization failure", ex);
        }

        return new CqlTranslationResult(translatedLibrary, dependencies);
    }
    
    /**
     * Some of this was adapted from the CQL Translation Server TranslationFailureException.
     * 
     * @param translationErrs List of translation errors.
     * @return String representation of the list of translation errors.
     */
    private static String formatMsg(List<CqlTranslatorException> translationErrs) {
        StringBuilder msg = new StringBuilder();
        for (CqlTranslatorException error : translationErrs) {
          TrackBack tb = error.getLocator();
          String lines = tb == null ? "[n/a]" : String.format("[%s:%s (start:%d:%d, end:%d:%d)]",
                  tb.getLibrary().getId(), tb.getLibrary().getVersion(),
                  tb.getStartLine(), tb.getStartChar(), tb.getEndLine(),
                  tb.getEndChar());
          msg.append(String.format("%s %s%n", lines, error.getMessage()));
        }
        return msg.toString();
    }
}
