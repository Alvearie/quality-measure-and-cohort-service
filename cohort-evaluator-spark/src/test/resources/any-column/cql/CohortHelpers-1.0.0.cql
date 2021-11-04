library CohortHelpers version '1.0.0'

using "CohortTest" version '0.0.1'

define function AnyColumn(obj Choice<CohortTest.Base>, pathPrefix String) returns List<CohortTest.Base> : external

define function AnyColumnRegex(obj Choice<CohortTest.Base>, pathRegex String) returns List<CohortTest.Base> : external
