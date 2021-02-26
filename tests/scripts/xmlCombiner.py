#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

import sys
from xml.etree import ElementTree
from xml.dom import minidom

def runOld(files):
    first = None
    suites = None
    for filename in files:
        print("Examining: " + filename)
        data = ElementTree.parse(filename)
        #need to change the testSuite to match filename
        data.find('testsuite').set('name',filename.split('.')[0])
        if first is None:
            first = data
            #do we need to find the testSuites object to add the rest of the items to?
            suites = data.find('testsuites')
            print(suites)
        else:
            first.extend(data.find('testsuite'))
    if first is not None:
        print(ElementTree.tostring(first))

def run(files):
    output = minidom.Document()
    suites = output.createElement('testsuites')
    output.appendChild(suites)
    for filename in files:
        doc = minidom.parse(filename)
        data = doc.getElementsByTagName('testsuite')
        #need to change the testSuite to match filename
        data[0].setAttribute('name',filename.split('.')[0])
        suites.appendChild(data[0])
    
    #xml_str = output.toprettyxml(indent = "\t")
    #xml_str = output.toxml()
    with open("combinedResults.xml", "w") as f:
        #f.write(xml_str)
        f.write(output.toxml())

if __name__ == "__main__":
    run(sys.argv[1:])