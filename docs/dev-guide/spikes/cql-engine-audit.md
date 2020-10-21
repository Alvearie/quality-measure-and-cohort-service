## cql_engine Audit

### Audit Metadata
- date: 10/12/20
- github repo: https://github.com/DBCG/cql_engine
- tag: `v1.5.0`


### TODOs

#### Summary

| Count | Category       | Reported           | Severity | Description |
| ----: | :------:       | :------:           | :------: | :---------- |
|    6  | `java`         | :heavy_check_mark: |  low     | `TODO`s with open github issue |
|  104  | `cql`          |                    |  low     | Didn't do a full audit of `cql` files but generally seemed to be innocuous |
|    7  | `xml`/`xsd`    |                    |  low     | test `xml`/`xsd` files |
|   43  | `java` _test_  |                    |  varies  | `TODO`s found in `java` _test_ classes |
|   58  | `java` _main_  |                    |  varies  | `TODO`s found in `java` _main_ classes |

Total: `218`

```bash
# cql_engine on tags/v1.5.0
ag -a "todo" | wc -l
```

#### Java Classes Detailed

| Name                      | Type   | Severity     | Description |
| :---                      | :----: | :------:     | :---------- |
| `TestR4ModelResolver`     | _test_ |   low        | "The cause of failure for this is unknown" |
| `ElmTests`                | _test_ |  medium      | Test validating that the ELM library deserialized from `json` <br/> matches the ELM library deserialized from `xml` is not implemented |    
| `RepeatEvaluator`         | _main_ |   low        | `NotImplementedException` |
| `MessageEvaluator`        | _main_ |  low/medium  | `stripPHI` helper method is used, but not implemented |
| `Dstu2FhirModelResolver`  | _main_ |   low        | profile validation not performed in `is(Object value, Class<?> type)` API |
| `Dstu3FhirModelResolver`  | _main_ |   low        | profile validation not performed in `is(Object value, Class<?> type)` API |
| `R4FhirModelResolver`     | _main_ |   low        | profile validation not performed in `is(Object value, Class<?> type)` API |
| `Context`                 | _main_ |   low        | "This is actually wrong, but to fix this would require preserving type information in the ELM" <br/> in `resolveFunctionRef` helper method |
| `CqlEngine`               | _main_ |   low        | possible peformance benefit: "reset a context rather than create a new one" <br/> `this.initializeContext(libraryCache, library, debugMap);` |
| `CqlEngine`               | _main_ |   low/medium | context does not support the use of multiple `Library`s <br/> `Context context = new Context(library);` |


### Github Bugs :bug:
- https://github.com/DBCG/cql_engine/labels/bug

| Issue # | Stale* | Category    | Severity | Description |
| ------: | :----: | :--------:  | :------: | :---------- |
|     35  |        | edge-case   |   low    | invalid resolve when operating on "qualified values: <br/> _e.g._ unit is `cm` instead of `cm^2` when multiplying |
|     41  |        | date-logic  |   low    | interval calculation issue |
|     43  |        | edge-case   |   low    | no unit normalization support: <br/> _e.g._ `1'm' < 1'cm'` |
|     45  |        | edge-case   |   low    | no interval "width" support: <br/> _e.g._ `Equivalent(x, 20 days)` |
|     85  |        | edge-case   |   low    | precision inaccuracy: <br/> _e.g._ `Power(10.0,-8.0)` |
|     86  |        | edge-case   |   low    | no implicit casting support: <br/> _e.g._ `2*Power(10,-8)` |
|     166 |        | edge-case   |   low    | `fhirClient` provides incorrect `POST` vs `GET` request |
|     213 |        | date-logic  |   low    | interval does not "merge" properly |
|     216 |        | null        |   low    | `null` handling issue |
|     246 |        | date-logic  |   low    | date math issue <br/> _e.g._ `@2016 + 365 days` |
|     264 |        | null        |   low    | `null` handling issue |
|     269 |        | edge-case   |   medium | `Observation.value.value` is empty, but has workaround |
|     303 | :zzz:  |             |          |             |
|     378 |        | edge-case   |   low    | decimal precision issue **(has code fix)** |
|     379 |        | edge-case   |   low    | decimal precision issue **(has code fix)** |
|     382 |        | date-logic  |   low    | date interval issue |
|     399 |        | edge-case   |   low    | `ConvertsTo` function issue **(has code fix)** |
|     403 |        | date-logic  |   low    | date interval issue |
|     404 |        | date-logic  |   low    | date interval issue: relates to #403 |
|     411 |        | edge-case   |   low    | unit conversion issue **(has code fix)** |

Total: `20`

_Stale: issue hasn't been commented on for a "long time" after requesting more details_ 
