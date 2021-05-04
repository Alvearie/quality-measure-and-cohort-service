package com.ibm.cohort.engine.measure;

import java.util.stream.Collectors;

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
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;
import org.opencds.cqf.cql.engine.runtime.Time;

import com.ibm.cohort.engine.cdm.CDMConstants;
import com.ibm.cohort.engine.measure.parameter.UnsupportedFhirTypeException;
import com.ibm.cohort.engine.parameter.DateParameter;
import com.ibm.cohort.engine.parameter.DatetimeParameter;

/**
 * Expected types to handle derived from this definition:
 * https://pages.github.ibm.com/watson-health-fhir-server/ig-common-data-model/StructureDefinition-parameter-definition-with-default.html
 */
public class R4ParameterDefinitionWithDefaultToCQLConverter {

	public static Object getCqlObject(ParameterDefinition parameterDefinition) {
		Extension defaultValueExtension = parameterDefinition.getExtensionByUrl(CDMConstants.PARAMETER_DEFAULT_URL);

		Object cqlValue = null;
		if (defaultValueExtension != null) {
			Type extensionValue = defaultValueExtension.getValue();
			if (extensionValue instanceof Base64BinaryType) {
				cqlValue = ((Base64BinaryType) extensionValue).asStringValue();
			}
			else if (extensionValue instanceof BooleanType) {
				cqlValue = ((BooleanType) extensionValue).booleanValue();
			}
			else if (extensionValue instanceof DateType) {
				 cqlValue = new DateParameter(((DateType) extensionValue).asStringValue()).toCqlType();
			}
			else if (extensionValue instanceof DateTimeType) {
				cqlValue = convertDateTimeType((DateTimeType) extensionValue);
			}
			else if (extensionValue instanceof DecimalType) {
				cqlValue = ((DecimalType) extensionValue).getValue();
			}
			else if (extensionValue instanceof InstantType) {
				cqlValue = new DatetimeParameter(((InstantType) extensionValue).getValueAsString()).toCqlType();
			}
			else if (extensionValue instanceof IntegerType) {
				cqlValue = ((IntegerType) extensionValue).getValue();
			}
			else if (extensionValue instanceof StringType) {
				cqlValue = ((StringType) extensionValue).getValue();
			}
			else if (extensionValue instanceof TimeType) {
				cqlValue = new Time(((TimeType) extensionValue).asStringValue());
			}
			else if (extensionValue instanceof UriType) {
				cqlValue = ((UriType) extensionValue).getValue();
			}
			else if (extensionValue instanceof Coding) {
				cqlValue = convertCoding((Coding) extensionValue);
			}
			else if (extensionValue instanceof CodeableConcept) {
				cqlValue = convertCodeableConcept((CodeableConcept) extensionValue);
			}
			else if (extensionValue instanceof Period) {
				Period castValue = (Period) extensionValue;

				cqlValue = new Interval(convertDateTimeType(castValue.getStartElement()), true,
										convertDateTimeType(castValue.getEndElement()), true);
			}
			else if (extensionValue instanceof org.hl7.fhir.r4.model.Quantity) {
				cqlValue = convertQuantity((org.hl7.fhir.r4.model.Quantity) extensionValue);
			}
			else if (extensionValue instanceof Range) {
				Range castValue = (Range) extensionValue;

				cqlValue = new Interval(convertQuantity(castValue.getLow()), true,
										convertQuantity(castValue.getHigh()), true);
			}
			else if (extensionValue instanceof org.hl7.fhir.r4.model.Ratio) {
				org.hl7.fhir.r4.model.Ratio castValue = (org.hl7.fhir.r4.model.Ratio) extensionValue;

				cqlValue = new Ratio()
						.setDenominator(convertQuantity(castValue.getDenominator()))
						.setNumerator(convertQuantity(castValue.getNumerator()));
			}
			else {
				throw new UnsupportedFhirTypeException(extensionValue);
			}
		}

		return cqlValue;
	}

	private static Code convertCoding(Coding coding) {
		return new Code().withCode(coding.getCode())
				.withSystem(coding.getSystem())
				.withDisplay(coding.getDisplay())
				.withVersion(coding.getVersion());
	}

	private static Concept convertCodeableConcept(CodeableConcept codeableConcept) {
		Concept concept = new Concept();
		concept.withDisplay(codeableConcept.getText());

		concept.withCodes(
				codeableConcept.getCoding()
						.stream()
						.map(R4ParameterDefinitionWithDefaultToCQLConverter::convertCoding)
						.collect(Collectors.toList()));

		return concept;
	}

	private static DateTime convertDateTimeType(DateTimeType dateTimeType) {
		return (DateTime) new DatetimeParameter(dateTimeType.getValueAsString()).toCqlType();
	}

	private static Quantity	convertQuantity(org.hl7.fhir.r4.model.Quantity fhirQuantity) {
		return new Quantity().withUnit(fhirQuantity.getUnit()).withValue(fhirQuantity.getValue());
	}
}
