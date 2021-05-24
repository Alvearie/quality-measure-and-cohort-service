/** 
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.retrieve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseCoding;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterMap;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;

import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;

/**
 * Custom implementation of the CQL Engine RestFhirRetrieveProvider that
 * provides some additional features and workarounds for known
 * issues in the OSS implementation.
 */
public class R4RestFhirRetrieveProvider extends RestFhirRetrieveProvider {

	private Integer searchPageSize = null;
	
	public R4RestFhirRetrieveProvider(SearchParameterResolver searchParameterResolver, IGenericClient fhirClient) {
		super(searchParameterResolver, fhirClient);
	}
	
	/**
	 * Provide the ability to configure the _count parameter in FHIR search
	 * queries. By default, some servers will set this to a very small number which
	 * results in a lot of REST calls for any reasonably large dataset. This enables the 
	 * user to configure that value as needed.
	 * 
	 * @param pageSize Number of records to return per request in a FHIR search query
	 */
	public void setSearchPageSize(Integer pageSize) {
		this.searchPageSize = pageSize;
	}
	
	/**
	 * Retrieve the user-configured default page size setting.
	 * 
	 * @return whatever value the user provided or null if no value was set. If this
	 * value is null, it is left up to the server to determine how many records will 
	 * be retrieved per REST call.
	 */
	public Integer getSearchPageSize() {
		return this.searchPageSize;
	}
	
	/**
	 * This is a copy/paste hack job on the OSS <code>executeQuery</code>
	 * implementation that adds the _count parameter into the generated queries.
	 * 
	 * @param dataType Resource type that is being queried
	 * @param map      search parameters
	 * @return result of the query
	 */
	@Override
	protected IBaseResource executeQuery(String dataType, SearchParameterMap map) {
		if (map.containsKey("_id")) {
			return this.queryById(dataType, map);
		} else {
			IQuery<IBaseBundle> search = this.fhirClient.search().forResource(dataType);

			Map<String, List<IQueryParameterType>> flattenedMap = new HashMap<>();
			for (Map.Entry<String, List<List<IQueryParameterType>>> entry : map.entrySet()) {
				String name = entry.getKey();
				if (name == null) {
					continue;
				}

				List<List<IQueryParameterType>> value = entry.getValue();
				if (value == null || value.size() == 0) {
					continue;
				}

				List<IQueryParameterType> flattened = new ArrayList<>();

				for (List<IQueryParameterType> subList : value) {

					if (subList == null || subList.size() == 0) {
						continue;
					}

					if (subList.size() == 1) {
						flattened.add(subList.get(0));
						continue;
					}

					// Sublists are logical "Ors"
					// The only "Or" supported from the engine are tokens at the moment.
					// So this is a hack to add them to a "criterion", which is the
					// only way the HAPI POST api supports passing them.
					IQueryParameterType first = subList.get(0);
					if (first instanceof TokenParam) {
						TokenClientParam tcp = new TokenClientParam(name);

						IBaseCoding[] codings = this.toCodings(subList);

						ICriterion<?> criterion = tcp.exactly().codings(codings);

						search = search.where(criterion);
					} else {
						flattened.addAll(subList);
					}
				}

				flattenedMap.put(name, flattened);
			}

			// THIS IS THE NEW CODE
			if( getSearchPageSize() != null ) {
				search.count( getSearchPageSize() );
			}
			// END NEW CODE
			
			return search.where(flattenedMap).usingStyle(getSearchStyle()).execute();
		}
	}
			

	@Override
	/**
	 * Workaround to enable user-configurable page size in retrieve operations.
	 * 
	 * See <a href="https://github.com/DBCG/cql_engine/issues/463">https://github.com/DBCG/cql_engine/issues/463</a>
	 */
	protected SearchParameterMap getBaseMap(Pair<String, IQueryParameterType> templateParam,
			Pair<String, IQueryParameterType> contextParam, Pair<String, DateRangeParam> dateRangeParam) {
		SearchParameterMap map = super.getBaseMap(templateParam, contextParam, dateRangeParam);
		if( searchPageSize != null ) {
			map.setCount( searchPageSize );
		}
		return map;
	}

	
}
