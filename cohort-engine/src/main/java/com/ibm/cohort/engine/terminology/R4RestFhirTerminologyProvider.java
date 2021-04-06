/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.terminology;


import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

/**
 * Customized FHIR terminology provider based on the original FHIR R4 provider
 * in the cql_engine, but with logic for dealing with situations where ValueSet
 * resources are not found and with an additional query parameter for version
 * added, as needed, during ValueSet resource resolution.
 */
public class R4RestFhirTerminologyProvider extends R4FhirTerminologyProvider {

    private IGenericClient fhirClient;
    
    public R4RestFhirTerminologyProvider(IGenericClient fhirClient) {
        super(fhirClient);
        this.fhirClient = fhirClient;
    }
    
    /**
     * This implementation patches several issues with the default OSS
     * implementation. We opened a
     * <a href="https://github.com/DBCG/cql_engine/pull/462">PR</a> with these
     * changes in the cql_engine project, but we are patching here while waiting for
     * that to resolve.
     * 
     * <ol>
     * <li>In the original OSS implementation, all ValueSet.id values would be
     * filtered through java.net.URL and values that did not parse were treated like
     * an raw resource ID. In some situations, such as with urn:oid:XXX values, this
     * would be pass an invalid ID to downstream operations and cause failures. In
     * this new implementation, to java.net.URL check is performed, but by-URL
     * lookup is only done on HTTP or HTTP/S prefixed IDs. IDs prefixed by urn:oid:
     * are handled specially.</li>
     * <li>When the ValueSet.id field is prefixed by urn:oid:, the prefix is
     * stripped and the remaining value is considered to the ValueSet resource
     * ID.</li>
     * <li>There are
     * <a href="https://cql.hl7.org/02-authorsguide.html#valuesets">two features of
     * CQL</a> related to ValueSets - version and codesystem - that were not handled
     * in the original OSS implementation and just silently ignored. I added logic
     * that will throw UnsupportedOperationException if either is encountered during
     * execution.</li>
     * <li>If an incorrect number of ValueSet resources are returned in the search
     * by URL operation, the OSS would either pass it through silently as 0 codes
     * when no records were found or arbitrarily pick the first result row in the
     * case of multiple results. Those conditions will now yield an
     * IllegalArgumentException.</li>
     * </ol>
     */
    @Override
    public Boolean resolveByUrl(ValueSetInfo valueSet) {
        if (valueSet.getVersion() != null
                || (valueSet.getCodeSystems() != null && valueSet.getCodeSystems().size() > 0)) {
            throw new UnsupportedOperationException(String.format(
                    "Could not expand value set %s; version and code system bindings are not supported at this time.",
                    valueSet.getId()));
        }
        
        if (valueSet.getId().startsWith("urn:oid:")) {
            valueSet.setId(valueSet.getId().replace("urn:oid:", ""));
        } else if (valueSet.getId().startsWith("http:") || valueSet.getId().startsWith("https:")) {
            Bundle searchResults = fhirClient.search().forResource(ValueSet.class)
                    .where(ValueSet.URL.matches().value(valueSet.getId())).returnBundle(Bundle.class).execute();
            if (searchResults.getEntry().size() == 0 ) {
                throw new IllegalArgumentException(String.format("Could not resolve value set %s.", valueSet.getId()));
            } else if (searchResults.getEntry().size() == 1) {
                valueSet.setId(searchResults.getEntryFirstRep().getResource().getIdElement().getIdPart());
            } else {
                throw new IllegalArgumentException("Found more than 1 ValueSet with url: " + valueSet.getId());
            }
        }

        return true;
    }
    
    /** 
     * This is a small patch to the OSS implementation to use named-parameter lookup on 
     * the operation response instead of just assuming a positional location.
     */
    @Override
    public Code lookup(Code code, CodeSystemInfo codeSystem) throws ResourceNotFoundException {
        Parameters respParam = fhirClient
                .operation()
                .onType(CodeSystem.class)
                .named("lookup")
                .withParameter(Parameters.class, "code", new CodeType(code.getCode()))
                .andParameter("system", new UriType(codeSystem.getId()))
                .execute();
        
        StringType display = (StringType) respParam.getParameter("display");
        if( display != null ) {
        	code.withDisplay( display.getValue() );
        }

        return code.withSystem(codeSystem.getId());
    }
}
