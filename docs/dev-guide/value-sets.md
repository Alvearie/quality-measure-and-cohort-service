## Value Sets

### FHIR server setup

?> Because the default setting for the FHIR servers are "bare-bones", 
the `serverRegistryResourceProviderEnabled` properties is `false` by _default_. <br/>
As such, you need to enable this property in order to be able to perform `joins`
with `Resource` type value sets. <br/>
It being disabled by default should not significantly impact _memory_ and _cpu_ requirements <br/>
per [@John Timm](https://ibm-watsonhealth.slack.com/archives/C14JTTR6C/p1610324841183900) <br/> <br/>
As the [IBM Guide](https://ibm.github.io/FHIR/guides/FHIRServerUsersGuide#51-configuration-properties-reference) indicates
that `serverRegistryResourceProviderEnabled` indicate that the property is not _dynamic_, <br/>
a server restart is required to pick up any configuration changes for this property.


### Examples

!> "cross"-tenant value set joins are not possible due to the _"tenant"_ being a part of the request `header`:
```bash
--header 'X-FHIR-TENANT-ID: test-fvt'
```

#### _get_ IG Value Set

```bash
curl -k 'https://localhost:9443/fhir-server/api/v4/ValueSet/$expand?url=http://cts.nlm.nih.gov/fhir/ValueSet/1.2.91.13925.17760.26050446'
```

```json
{
  "resourceType": "ValueSet",
...
  "compose": {
    "include": [
      {
        "system": "http://www.ama-assn.org/go/cpt",
        "concept": [
   ...
      }
    ]
  },
  "expansion": {
    "timestamp": "2020-12-28T20:41:14.451Z",
    "total": 8,
    "contains": [
  ...
}
```

#### _add_ Resource Value Set

```bash
curl --location --request PUT 'https://localhost:9443/fhir-server/api/v4/ValueSet/test-value-set' \
--data-raw '{
    "resourceType": "ValueSet",
    "id": "test-base",
    "url": "http://cts.nlm.nih.gov/fhir/ValueSet/test-base",
    "version": "1",
    "name": "Test Base",
    "status": "active",
    "publisher": "test",
    "compose": {
        "include": [
            {
                "system": "http://hl7.org/fhir/administrative-gender",
                "concept": [
                    {
                        "code": "female",
                        "display": "Female"
                    }
                ]
            }
        ]
    }
}'
```

#### _join_ Value Set 

```bash
curl --location --request POST 'https://localhost:9443/fhir-server/api/v4/ValueSet/$expand' \
--data-raw '
{
    "resourceType": "ValueSet",
    "id": "test-extension",
    "url": "http://hl7.org/fhir/ValueSet/test-extension",
    "version": "1",
    "name": "Test Extension",
    "status": "active",
    "compose": {
        "include": [
            {
                "valueSet": [
                    "http://cts.nlm.nih.gov/fhir/ValueSet/test-base"
                ]
            },
            {
                "system": "http://hl7.org/fhir/administrative-gender",
                "concept": [
                    {
                        "code": "male",
                        "display": "Male"
                    }
                ]
            }
        ]
    }
}'
```
