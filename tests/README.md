# Test setup steps:

## Build and run the Docker image

To build a docker image that  will run in the same cluster as the FHIR server you will use for testing, run:

```bash
# ensure you have the latest image
docker pull blazemeter/taurus
```

```bash
# build the standard image
cd ${Your path to}/quality-measure-and-cohort-service/tests/docker
docker build -t taurus-javabridge -f taurus-bridge.dockerfile .
```

Or, if you are building an image that will use port forwarding to access a FHIR server in IBM Cloud, you must instead build an image
using the `taurus-bridge-portForwarding.dockerfile` dockerfile:

```bash
# build an image that will port-forward localhost:9080 to a remote FHIR server running in IBM cloud
docker build -t taurus-javabridge-portforwarding -f taurus-bridge-portForwarding.dockerfile .
```

After building the docker image, you can run tests with:

```bash
docker run -it --rm --env-file {Your path to}/quality-measure-and-cohort-service/tests/docker/Env.txt -v {Your path to}/quality-measure-and-cohort-service:/bzt-configs -v {Your path to}/quality-measure-and-cohort-service/tests/results:/tmp/artifacts -v {Your path to}/fhir-config:/fhir-config {docker-image-name} ./tests/scenarios/{test-scenarios}
```

Where {docker-image-name} is `taurus-javabridge` or `taurus-javabridge-portforwarding` in the examples above and {test-scenarios} is a yaml file
containing the tests to run.

You will note that the docker image uses configuration data mounted from your local system volumes from using the ``-v localDir:imageDir`` option. This allows easy plug points for configuration data and quick turnaround when you make updates. If you are running Docker for Windows, your local folders must be enabled for sharing. Open Docker for Windows, go to Settings -> RESOURCES -> FILE SHARING and add each folder (\path\to\quality-measure-and-cohort-service, \path\to\quality-measure-and-cohort-service\tests\results, and \path\to\fhir-config). The fhir-config volume is useful for mounting configuration files such as the SSL trustStore needed to communicated with a remote FHIR server.


### Environment file for docker runs

Running Docker involves passing in a file containing environment variables to use during the run with the
`--env-file` argument. `quality-measure-and-cohort-service/tests/docker/Env.txt` contains the expected environment
variables along with a comment of what each variable is for. As at least one of the variables is for an IBM Cloud API key, it is
recommended to populate a separate file outside of git to use during your Docker runs rather than updating the provided
`Env.txt` file directly. File paths in Env.txt are paths inside the running docker image. If you need to point to a trustStore file 
that you've created, make sure that you have it available in a mounted volume and that your Env.txt path points to the mounted
location (e.g. /fhirConfig/trustStore.pk.

## Examing test results

After the tests complete you will find test output in the local directory that is mounted as /tmp/artifacts (this is mounted from ${Your path to}/quality-measure-and-cohort-service/tests/results in the examples above). You will find a number of different output files in this folder, but you will probably want to look at the the PyTestExecutor.ldjson for details on test failures. Each line in the file is a JSON object that contains the results of a single test case in the scenario file that was executed. You can use whatever tool/text-editor you like to examine these results, but you might find that [JSON Query (aka jq)](https://stedolan.github.io/jq/) is a useful mechansim for working with these files.

For example, the following JSON query command will dump a concise report of FAILED tests.
```bash
jq 'select(.status == "FAILED") | "====================", .status, .test_case, .error_msg' PyTestExecutor.ldjson
```

Each run will create a new number-tagged version of the PyTestExecutor file (e.g. PyTestExecutor-1.ldson, etc.), so make sure you are using the correct version for the latest run.

## If you want to run Taurus/Blazemeter locally, without Docker, you will need to install two specific Python packages:

### Install Taurus

```bash
pip3 install bzt
```

Or for Mac OS
```bash
brew install bzt
brew update bzt
```

## Install python-javabridge (openblas may not be necessary on non-mac systems)

```bash
pip3 install python-javabridge
```

Or for Mac OS...

```bash
brew install openblas
OPENBLAS="$(brew --prefix openblas)" /usr/local/opt/python@3.7/bin/pip3 install numpy
/usr/local/opt/python@3.7/bin/pip3 install numpy
/usr/local/opt/python@3.7/bin/pip3 install python-javabridge
```

On Windows, you will need to set an environment variable JDK_HOME that points to your JDK install folder (could be JAVA_HOME) and make sure that run the command as an administrator.
