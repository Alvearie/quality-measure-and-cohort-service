package com.ibm.cohort.engine.measure;

import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ParameterDefinition;
import org.hl7.fhir.r4.model.Type;

import com.ibm.cohort.engine.cdm.CDMConstants;
import com.ibm.cohort.engine.parameter.DateParameter;
import com.ibm.cohort.engine.parameter.DatetimeParameter;

public class ParameterDefinitionWithDefaultToCQLHelper {

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
				cqlValue = new DatetimeParameter(((DateTimeType) extensionValue).getValueAsString()).toCqlType();
			}
			else if (extensionValue instanceof DecimalType) {
				cqlValue = ((DecimalType) extensionValue).getValue();
			}
		}

		return cqlValue;
	}
}
