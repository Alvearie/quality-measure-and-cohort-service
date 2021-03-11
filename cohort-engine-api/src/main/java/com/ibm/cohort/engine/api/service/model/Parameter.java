package com.ibm.cohort.engine.api.service.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
	@JsonSubTypes.Type( value = IntegerParameter.class, name = ParameterType.INTEGER ),
	@JsonSubTypes.Type( value = DecimalParameter.class, name = ParameterType.DECIMAL ),
	@JsonSubTypes.Type( value = StringParameter.class, name = ParameterType.STRING ),
	@JsonSubTypes.Type( value = BooleanParameter.class, name = ParameterType.BOOLEAN ),
	@JsonSubTypes.Type( value = DatetimeParameter.class, name = ParameterType.DATETIME ),
	@JsonSubTypes.Type( value = DateParameter.class, name = ParameterType.DATE ),
	@JsonSubTypes.Type( value = TimeParameter.class, name = ParameterType.TIME ),
	@JsonSubTypes.Type( value = QuantityParameter.class, name = ParameterType.QUANTITY ),
	@JsonSubTypes.Type( value = RatioParameter.class, name = ParameterType.RATIO ),
	@JsonSubTypes.Type( value = IntervalParameter.class, name = ParameterType.INTERVAL ),
	@JsonSubTypes.Type( value = CodeParameter.class, name = ParameterType.CODE ),
	@JsonSubTypes.Type( value = ConceptParameter.class, name = ParameterType.CONCEPT )
	
})
public abstract class Parameter {
	private String name;
	private String type;
	
	public String getName() {
		return name;
	}
	public Parameter setName(String name) {
		this.name = name;
		return this;
	}
	public String getType() {
		return type;
	}
	public Parameter setType(String type) {
		this.type = type;
		return this;
	}
	
	public abstract Object toCqlType();
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}
}
