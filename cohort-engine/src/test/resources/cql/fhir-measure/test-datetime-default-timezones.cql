library "TestDatetimeDefaultTimezones" version '1.0.0'
using "FHIR" version '4.0.0'

define "Initial Population":
  test1 and test2 and test3 and test4 and test5 and test6 and test7 and test8

define "defaultDateTimeJan1":
  DateTime(2020, 1, 1, 0, 0, 0, 0)

define "defaultDateTimeJan2":
  DateTime(2020, 1, 2, 0, 0, 0, 0)

define "gmtMinus1DateTimeJan1":
  DateTime(2020, 1, 1, 0, 0, 0, 0, -1)

define "gmtMinus1DateTimeJan2":
  DateTime(2020, 1, 2, 0, 0, 0, 0, -1)

define "gmtPlus1DateTimeJan1":
  DateTime(2020, 1, 1, 0, 0, 0, 0, +1)

define "gmtPlus1DateTimeJan2":
  DateTime(2020, 1, 2, 0, 0, 0, 0, +1)

define "defaultDateJan1":
  Date(2020, 1, 1)

define "defaultDateJan2":
  Date(2020, 1, 2)

define "defaultIntervalDateTime":
  Interval[defaultDateTimeJan1, defaultDateTimeJan2]

define "gmtMinus1IntervalDateTime":
  Interval[gmtMinus1DateTimeJan1, gmtMinus1DateTimeJan2]

define "gmtPlus1IntervalDateTime":
  Interval[gmtPlus1DateTimeJan1, gmtPlus1DateTimeJan2]

define "defaultIntervalDate":
  Interval[defaultDateJan1, defaultDateJan2]

// The defines below will be true when evaluated in UTC,
// but one or more defines should be false when evaluated
// in a different timezone

// Tessts for GMT minus an offset
define "test1":
  defaultIntervalDateTime contains gmtMinus1DateTimeJan1 and
  gmtMinus1DateTimeJan2 after end of defaultIntervalDateTime

define "test2":
  gmtMinus1IntervalDateTime contains defaultDateTimeJan2 and
  defaultDateTimeJan1  before gmtMinus1IntervalDateTime

define "test3":
  defaultIntervalDate contains gmtMinus1DateTimeJan1 and
  gmtMinus1DateTimeJan2 after end of defaultIntervalDate

define "test4":
  gmtMinus1IntervalDateTime contains defaultDateJan2 and
  defaultDateJan1 before start of gmtMinus1IntervalDateTime

// Tests for GMT plus an offset
define "test5":
  defaultIntervalDateTime contains gmtPlus1DateTimeJan2 and
  gmtPlus1DateTimeJan1 before start of defaultIntervalDateTime

define "test6":
  gmtPlus1IntervalDateTime contains defaultDateTimeJan1 and
  defaultDateTimeJan2 after gmtPlus1IntervalDateTime

define "test7":
  defaultIntervalDate contains gmtPlus1DateTimeJan2 and
  gmtPlus1DateTimeJan1 before start of defaultIntervalDate

define "test8":
  gmtPlus1IntervalDateTime contains defaultDateJan1 and
  defaultDateJan2 after end of gmtPlus1IntervalDateTime
