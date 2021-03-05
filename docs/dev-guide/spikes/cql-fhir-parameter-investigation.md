## `cql` and `FHIR` parameter model investigation

[cql_engine's](https://github.com/DBCG/cql_engine) `FHIR` to `cql` model support in parameter usage

### Supported Types

#### Base
→ [BaseFhirTypeConverter](https://github.com/DBCG/cql_engine/blob/master/engine.fhir/src/main/java/org/opencds/cqf/cql/engine/fhir/converter/BaseFhirTypeConverter.java#L189)

- `IdType`
- `BooleanType`
- `IntegerType`
- `DecimalType`
- `DateType`
- `InstantType`
- `DateTimeType`
- `TimeType`
- `StringType`
- `QuantityType`
- `RatioType`
- `Coding`
- `CodeableConcept`
- `Period`
- `Range`

#### R4
→ [R4FhirTypeConverter](https://github.com/DBCG/cql_engine/blob/master/engine.fhir/src/main/java/org/opencds/cqf/cql/engine/fhir/converter/R4FhirTypeConverter.java)

- `Quanity`
- `Ratio`
- `Code`
- `Concept`
- `Interval`
- `Temporal`

---

### Unsupported Types

#### Explicitly
- `Any` (`cql` → `fhir`)
- `Tuple` (`cql` → `fhir`)

#### Partially Supported
- `base64Binary`
- `unsignedInt`/`positiveInt`
- `moneyQuanitity`/`SimpleQuantity`

#### Gaps

##### primitive
- url
- markdown

##### general-purpose
- attachment
- identifier
- annotation
- human name
- contact point
- address
- signature (trial usage)
- sampled data (trial usage)
- duration
- count (trial usage)
- distance (trial usage)
- age (trial usage)
- timing
- money
- annotation

##### metadata (most are "trial use" standards)
- contact detail (normative use)
- related artifact
- parameter definition
- expression
- trigger definition
- usage context
- data requirement

##### special purpose
- reference
- meta
- xhtml
- narrative
- extension
- element definition
- dosage (trial use)

---

### Resources
- [R4 data types](https://www.hl7.org/fhir/datatypes.html)
