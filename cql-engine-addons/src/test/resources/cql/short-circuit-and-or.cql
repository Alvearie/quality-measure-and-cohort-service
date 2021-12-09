library "short-circuit-and-or"
//Or
define TrueOrTrue : true or true
define TrueOrFalse : true or false
define TrueOrNull : true or null
define FalseOrTrue : false or true
define FalseOrFalse : false or false
define FalseOrNull : false or null
define NullOrTrue : null or true
define NullOrFalse : null or false
define NullOrNull : null or null

//And
define TrueAndTrue : true and true
define TrueAndFalse : true and false
define TrueAndNull : true and null
define FalseAndTrue : false and true
define FalseAndFalse : false and false
define FalseAndNull : false and null
define NullAndTrue : null and true
define NullAndFalse : null and false
define NullAndNull : null and null
