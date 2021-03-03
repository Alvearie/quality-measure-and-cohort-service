#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

import sys
from xml.etree import ElementTree
from xml.dom import minidom

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
    
    with open("combinedResults.xml", "w") as f:
        f.write(output.toxml())

if __name__ == "__main__":
    run(sys.argv[1:])