/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import java.util.stream.Collectors;

import com.ibm.cohort.measure.ParameterConverter;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.ParameterDefinition;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;

import com.ibm.cohort.cql.cdm.CDMConstants;
import com.ibm.cohort.cql.evaluation.parameters.BooleanParameter;
import com.ibm.cohort.cql.evaluation.parameters.CodeParameter;
import com.ibm.cohort.cql.evaluation.parameters.ConceptParameter;
import com.ibm.cohort.cql.evaluation.parameters.DateParameter;
import com.ibm.cohort.cql.evaluation.parameters.DatetimeParameter;
import com.ibm.cohort.cql.evaluation.parameters.DecimalParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntervalParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.evaluation.parameters.QuantityParameter;
import com.ibm.cohort.cql.evaluation.parameters.RatioParameter;
import com.ibm.cohort.cql.evaluation.parameters.StringParameter;
import com.ibm.cohort.cql.evaluation.parameters.TimeParameter;

/**
 * Expected types to handle derived from this definition:
 * https://pages.github.ibm.com/watson-health-fhir-server/ig-common-data-model/StructureDefinition-parameter-definition-with-default.html
 */
public class R4ParameterDefinitionWithDefaultToCohortParameterConverter implements ParameterConverter<ParameterDefinition> {

	@Override
	public Parameter toCohortParameter(ParameterDefinition parameterDefinition) {
		Extension defaultValueExtension = parameterDefinition.getExtensionByUrl(CDMConstants.PARAMETER_DEFAULT_URL);

		Parameter parameter = null;
		if (defaultValueExtension != null) {
			parameter = toCohortParameter(defaultValueExtension);
		}

		return parameter;
	}

	public Parameter toCohortParameter(Extension extension) {
		Parameter parameter;

		Type extensionValue = extension.getValue();
		if (extensionValue instanceof Base64BinaryType) {
			parameter = new StringParameter(((Base64BinaryType) extensionValue).asStringValue());
		}
		else if (extensionValue instanceof BooleanType) {
			parameter = new BooleanParameter(((BooleanType) extensionValue).booleanValue());
		}
		else if (extensionValue instanceof DateType) {
			parameter = new DateParameter(((DateType) extensionValue).asStringValue());
		}
		else if (extensionValue instanceof DateTimeType) {
			parameter = convertDateTimeType((DateTimeType) extensionValue);
		}
		else if (extensionValue instanceof DecimalType) {
			parameter = new DecimalParameter(((DecimalType) extensionValue).getValueAsString());
		}
		else if (extensionValue instanceof InstantType) {
			parameter = new DatetimeParameter(((InstantType) extensionValue).getValueAsString());
		}
		else if (extensionValue instanceof IntegerType) {
			parameter = new IntegerParameter(((IntegerType) extensionValue).getValue());
		}
		else if (extensionValue instanceof StringType) {
			parameter = new StringParameter(((StringType) extensionValue).getValue());
		}
		else if (extensionValue instanceof TimeType) {
			parameter = new TimeParameter(((TimeType) extensionValue).asStringValue());
		}
		else if (extensionValue instanceof UriType) {
			parameter = new StringParameter(((UriType) extensionValue).getValue());
		}
		else if (extensionValue instanceof Coding) {
			parameter = convertCoding((Coding) extensionValue);
		}
		else if (extensionValue instanceof CodeableConcept) {
			parameter = convertCodeableConcept((CodeableConcept) extensionValue);
		}
		else if (extensionValue instanceof Period) {
			Period castValue = (Period) extensionValue;

			parameter = new IntervalParameter(convertDateTimeType(castValue.getStartElement()), true,
											  convertDateTimeType(castValue.getEndElement()), true);
		}
		else if (extensionValue instanceof Quantity) {
			parameter = convertQuantity((Quantity) extensionValue);
		}
		else if (extensionValue instanceof Range) {
			Range castValue = (Range) extensionValue;

			parameter = new IntervalParameter(convertQuantity(castValue.getLow()), true,
											  convertQuantity(castValue.getHigh()), true);
		}
		else if (extensionValue instanceof Ratio) {
			Ratio castValue = (Ratio) extensionValue;

			parameter = new RatioParameter()
					.setDenominator(convertQuantity(castValue.getDenominator()))
					.setNumerator(convertQuantity(castValue.getNumerator()));
		}
		else {
			throw new UnsupportedFhirTypeException(extensionValue);
		}

		return parameter;
	}

	private CodeParameter convertCoding(Coding coding) {
		return new CodeParameter().setValue(coding.getCode())
				.setSystem(coding.getSystem())
				.setDisplay(coding.getDisplay())
				.setVersion(coding.getVersion());
	}

	private ConceptParameter convertCodeableConcept(CodeableConcept codeableConcept) {
		ConceptParameter conceptParameter = new ConceptParameter();
		conceptParameter.setDisplay(codeableConcept.getText());

		conceptParameter.setCodes(
				codeableConcept.getCoding()
						.stream()
						.map(this::convertCoding)
						.collect(Collectors.toList()));

		return conceptParameter;
	}

	private DatetimeParameter convertDateTimeType(DateTimeType dateTimeType) {
		return new DatetimeParameter(dateTimeType.getValueAsString());
	}

	private QuantityParameter convertQuantity(Quantity fhirQuantity) {
		return new QuantityParameter(fhirQuantity.getValue().toString(), fhirQuantity.getUnit());
	}
}
