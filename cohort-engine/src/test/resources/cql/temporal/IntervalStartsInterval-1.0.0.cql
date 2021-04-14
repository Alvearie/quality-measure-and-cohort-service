 library "IntervalStartsInterval" version '1.0.0'
 
 define LHS:
 	Interval[@2020-07-04T12:00:00.000-05:00, @2021-07-04T12:00:00.000-05:00]
 
 
 define RHS:
 	Interval[@2020-07-04T12:00:00.000-05:00, @2022-07-04T12:00:00.000-05:00]
 
 define "LHS Starts RHS":
 	LHS starts RHS