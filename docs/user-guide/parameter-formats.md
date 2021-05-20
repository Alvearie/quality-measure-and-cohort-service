# Parameter Formats
This page contains the fields for each type of parameter and examples of how to specify each parameter type in JSON.
Each field is required unless marked with `(Optional)`.

## Integer
* `value`: Integer value of the parameter.

Example:
```json
{
    "type": "integer",
    "value": 20
}
```

## Decimal
* `value`: String containing decimal value of the parameter.

Example:
```json
{
    "type": "decimal",
    "value": "20.9"
}
```

## String
* `value`: String value of the parameter.

Example:
```json
{
    "type": "string",
    "value": "string_value"
}
```

## Boolean
* `value`: Boolean value of the parameter.

Example:
```json
{
    "type": "boolean",
    "value": false
}
```

## DateTime
* `value`: String containing the datetime value of the parameter.
    * If `value` does not specify a timezone, the underlying CQL DateTime will be
      created with a timezone of `UTC`.

Example:
```json
{
    "type": "datetime",
    "value": "@2020-01-02T12:13:14"
}
```

## Date
* `value`: String containing the date value of the parameter.

Example:
```json
{
    "type": "date",
    "value": "2020-04-05"
}
```

## Time
* `value`: String containing the time value of the parameter.

Example:
```json
{
    "type": "time",
    "value": "23:12:35"
}
```

## Quantity
* `amount`: String containing the quantity amount.
* `unit`: String containing the quantity unit.


Example:
```json
{
    "type": "quantity",
    "amount": "100",
    "unit": "mg/mL"
}
```

## Ratio
* `numerator`: Quantity parameter JSON object (see above).
* `denominator`: Quantity parameter JSON object (see above).


Example:
```json
{
    "type": "ratio",
    "numerator": {
        "type": "quantity",
        "amount": "100",
        "unit": "mg/mL"
    },
    "denominator": {
        "type": "quantity",
        "amount": "23",
        "unit": "mg/mL"
    }
}
```

## Interval
* `start`: A non-interval parameter JSON object with the same type as `end`.
* `startInclusive` (Optional): Boolean value specifying if the start of the interval is inclusive.
* `end`: A non-interval parameter JSON object with the same type as `start`.
* `endInclusive` (Optional): Boolean value specifying if the end of the interval is inclusive.


Example with date parameters:
```json
{
    "type": "interval",
    "start": {
        "type": "date",
        "value": "2019-07-04"
    },
    "startInclusive": true,
    "end": {
        "type": "date",
        "value": "2020-07-04"
    },
    "endInclusive": true
}
```

Example with integer parameters:
```json
{
    "type": "interval",
    "start": {
        "type": "integer",
        "value": 1
    },
    "startInclusive": true,
    "end": {
        "type": "integer",
        "value": "100"
    },
    "endInclusive": false
}
```

## Code
* `value`: String value containing the code value.
* `system` (Optional): String value containing the code system.
* `display` (Optional): String value containing the code display.
* `version` (Optional): String value containing the code version.

Minimal Code Example:
```json
{
    "type": "code",
    "value": "12345"
}
```

Full Code Example:
```json
{
    "type": "code",
    "value": "12345",
    "system": "http://example-system.org",
    "display": "A code",
    "version": "1.2.3"
}
```

## Concept
* `codes`: JSON list of one or more Code parameter objects (see above).
* `display` (Optional): String containing the concept display value.

Example:
```json
{
    "type": "concept",
    "display": "Display string for concept",
    "codes": [
        {
            "type": "code",
            "value": "12345",
            "system": "http://example-system.org",
            "display": "A code",
            "version": "1.2.3"
        },
        {
            "type": "code",
            "value": "77442",
            "system": "http://example-system.org",
            "display": "Another code"
        }
    ]
}
```