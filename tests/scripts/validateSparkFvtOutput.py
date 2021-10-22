#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
import os
import sys

#Set environment variables for both the PySpark workers and driver to use the same python executable
os.environ['PYSPARK_PYTHON'] = sys.executable
os.environ['PYSPARK_DRIVER_PYTHON'] = sys.executable

from pyspark.sql import SparkSession
from pyspark.sql import Row
from pyspark.sql.types import BooleanType
from pyspark.sql.types import IntegerType
from pyspark.sql.types import StructType
from pyspark.sql.types import StructField

# Declare global variables to keep track of number of passing and failing tests
passing=0
failing=0
# Open an xml file to write validation results to in global scope
xmlfile = open("sparkfvttest.xml", "w")

#Create a SparkSession object (one time only)
spark = SparkSession\
    .builder\
    .appName("fvt-check")\
    .getOrCreate()
	
#Function to validate results for the Device Cohort
def validateDeviceCohortEval():
	global passing
	global failing
	global xmlfile

	actual_df = spark.read.parquet("spark-cos/fvt-output/Device_cohort")

	expected_data = [
           Row(1, False),
           Row(2, True),
           Row(3, False),
		   Row(4, False),
		   Row(5, False),
		   Row(6, False),
		   Row(7, False),
		   Row(8, True),
		   Row(9, True),
		   Row(10, True)
	]

	sch = StructType([StructField("id", IntegerType(), False),\
          StructField("DeviceMeasureFVT|cohort", BooleanType(), True)])

	expected_df = spark.createDataFrame(expected_data, sch)
	print(" ")
	print("Expected Device_Cohort Results:")
	expected_df.show()
	print("------------------------------------------------")
	print(" ")
	print("Actual Device_Cohort Results read in Parquet format from Cloud Object Storage:")
	actual_df.sort(["id"]).show()

    #Do the validation of actual results to expected results
	exp_to_act_cnt=expected_df.exceptAll(actual_df.select(["id", "DeviceMeasureFVT|cohort"])).count()
	act_to_exp_cnt=actual_df.exceptAll(expected_df.select(["id", "DeviceMeasureFVT|cohort"])).count()

	if((exp_to_act_cnt == 0) and (act_to_exp_cnt == 0)):
		print("Actual results match the Expected results for the evaluated Device Cohort.")
		passing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validateDeviceCohortEval\"/>" + "\n")
	else:
		print("Actual and Expected results differed for the evaluated Device Cohort for: ", act_to_exp_cnt, " evaluations.")
		failing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validateDeviceCohortEval\"><failure message=\"Actual and Expected results differed for the evaluated Device Cohort for: "+ str(act_to_exp_cnt) +" evaluations.\">fail</failure></testcase>" + "\n")

#Function to validate results for the Observation Cohort
def validateObservationCohortEval():
	global passing
	global failing
	global xmlfile

	actual_df = spark.read.parquet("spark-cos/fvt-output/Observation_cohort")

	expected_data = [
           Row(1, True),
           Row(2, False),
           Row(3, False),
		   Row(4, False),
		   Row(5, False),
		   Row(6, False),
		   Row(7, False),
		   Row(8, True),
		   Row(9, True),
		   Row(10, False)
	]

	sch = StructType([StructField("id", IntegerType(), False),\
          StructField("Observation_Cohort", BooleanType(), True)])

	expected_df = spark.createDataFrame(expected_data, sch)
	print(" ")
	print("Expected Observation_Cohort Results:")
	expected_df.show()
	print("------------------------------------------------")
	print(" ")
	print("Actual Observation_Cohort Results read in Parquet format from Cloud Object Storage:")
	actual_df.sort(["id"]).show()

    #Do the validation of actual results to expected results
	exp_to_act_cnt=expected_df.exceptAll(actual_df.select(["id", "Observation_Cohort"])).count()
	act_to_exp_cnt=actual_df.exceptAll(expected_df.select(["id", "Observation_Cohort"])).count()

	if((exp_to_act_cnt == 0) and (act_to_exp_cnt == 0)):
		print("Actual results match the Expected results for the evaluated Observation Cohort.")
		passing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validateObservationCohortEval\"/>" + "\n")
	else:
		print("Actual and Expected results differed for the evaluated Observation Cohort for: ", act_to_exp_cnt, " evaluations.")
		failing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validateObservationCohortEval\"><failure message=\"Actual and Expected results differed for the evaluated Observation Cohort for: "+ str(act_to_exp_cnt) +" evaluations.\">fail</failure></testcase>" + "\n")

#Function to validate results for the Patient Cohort
def validatePatientCohortEval():
	global passing
	global failing
	global xmlfile

	actual_df = spark.read.parquet("spark-cos/fvt-output/Patient_cohort")

	expected_data = [
           Row(1, False),
           Row(2, True),
           Row(3, False)
	]

	sch = StructType([StructField("id", IntegerType(), False),\
          StructField("Patient_Cohort", BooleanType(), True)])

	expected_df = spark.createDataFrame(expected_data, sch)
	print(" ")
	print("Expected Patient_Cohort Results:")
	expected_df.show()
	print("------------------------------------------------")
	print(" ")
	print("Actual Patient_Cohort Results read in Parquet format from Cloud Object Storage:")
	actual_df.sort(["id"]).show()

    #Do the validation of actual results to expected results
	exp_to_act_cnt=expected_df.exceptAll(actual_df.select(["id", "Patient_Cohort"])).count()
	act_to_exp_cnt=actual_df.exceptAll(expected_df.select(["id", "Patient_Cohort"])).count()

	if((exp_to_act_cnt == 0) and (act_to_exp_cnt == 0)):
		print("Actual results match the Expected results for the evaluated Patient Cohort.")
		passing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validatePatientCohortEval\"/>" + "\n")
	else:
		print("Actual and Expected results differed for the evaluated Patient Cohort for: ", act_to_exp_cnt, " evaluations.")
		failing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validatePatientCohortEval\"><failure message=\"Actual and Expected results differed for the evaluated Patient Cohort for: "+ str(act_to_exp_cnt) +" evaluations.\">fail</failure></testcase>" + "\n")
		
#Function to validate results for the Practitioner Cohort
def validatePractitionerCohortEval():
	global passing
	global failing
	global xmlfile
	
	actual_df = spark.read.parquet("spark-cos/fvt-output/Practitioner_cohort")
	
	expected_data = [
           Row(1, False),
           Row(2, False),
           Row(3, True),
		   Row(4, True),
		   Row(5, True),
		   Row(6, True),
		   Row(7, False),
		   Row(8, True),
		   Row(9, True),
		   Row(10, False)
	]

	sch = StructType([StructField("id", IntegerType(), False),\
          StructField("PractitionerMeasureFVT|cohort", BooleanType(), True)])

	expected_df = spark.createDataFrame(expected_data, sch)
	print(" ")
	print("Expected Practitioner_Cohort Results:")
	expected_df.show()
	print("------------------------------------------------")
	print(" ")
	print("Actual Practitioner_Cohort Results read in Parquet format from Cloud Object Storage:")
	actual_df.sort(["id"]).show()

    #Do the validation of actual results to expected results
	exp_to_act_cnt=expected_df.exceptAll(actual_df.select(["id", "PractitionerMeasureFVT|cohort"])).count()
	act_to_exp_cnt=actual_df.exceptAll(expected_df.select(["id", "PractitionerMeasureFVT|cohort"])).count()

	if((exp_to_act_cnt == 0) and (act_to_exp_cnt == 0)):
		print("Actual results match the Expected results for the evaluated Practitioner Cohort.")
		passing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validatePractitionerCohortEval\"/>" + "\n")
	else:
		print("Actual and Expected results differed for the evaluated Practitioner Cohort for: ", act_to_exp_cnt, " evaluations.")
		failing+=1
		xmlfile.write("\t\t"+ "<testcase classname=\"sparkValidation\" name=\"validatePractitionerCohortEval\"><failure message=\"Actual and Expected results differed for the evaluated Practitioner Cohort for: "+ str(act_to_exp_cnt) +" evaluations.\">fail</failure></testcase>" + "\n")

#main() function that calls each of the validation functions.
def main():
	global passing
	global failing
	global xmlfile

	# write out header portion of xml file
	xmlfile.write("<?xml version='1.0' encoding='UTF-8'?>" +"\n")
	xmlfile.write("<testsuites>" + "\n")
	xmlfile.write("\t"+ "<testsuite name=\"Spark Fvt Tests\">" + "\n")
	# call individual validation functions in order
	validateDeviceCohortEval()
	validateObservationCohortEval()
	validatePatientCohortEval()
	validatePractitionerCohortEval()
    # write out footer portion of xml file
	xmlfile.write("\t"+ "</testsuite>" + "\n")
	xmlfile.write("</testsuites>")
	xmlfile.close()
	total = int(passing)+int(failing)
	print(" ")
	print("Total number of cohort evaluation tests run: " + str(total))
	print("Number of Passing tests: " + str(passing))
	print("Number of Failing tests: " + str(failing))
   
if __name__ == "__main__":
   main()


