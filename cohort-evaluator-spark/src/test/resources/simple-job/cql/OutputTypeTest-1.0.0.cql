library OutputTypeTest version '1.0.0'

using MOCK version '1.0.0'

define IsInteger:
  10

define IsString:
  'Hello, World'

define IsBoolean:
  true

define IsDecimal:
  3.1415

define IsDate:
  @2000-07-04

define IsDateTime:
  @2014-01-25T14:30:14.559

define IsList:
  [Patient]

// Non-convertible types
define IsTuple:
  Tuple {
    Name: 'Patrick',
    DOB: @2014-01-01,
    Address: Tuple { Line1: '41 Spinning Ave', City: 'Dayton', State: 'OH' },
    Phones: { Tuple { Number: '202-413-1234', Use: 'Home' } }
  }

define IsTime:
  @T14:30:14.559

define IsInterval:
  Interval[@2013-01-01, @2014-01-01)

define IsCode:
  Code { code: '123', system: 'http://dummy.system' }

define IsQuantity:
  6 'gm/cm3'

define IsRatio:
  5 'mg' : 10 'mL'
