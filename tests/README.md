# Mac OS setup steps.

## Install Taurus

```bash
brew install bzt
brew update bzt
```

## Install python-javabridge (openblas may not be necessary on non-mac systems)

```bash
brew install openblas
OPENBLAS="$(brew --prefix openblas)" /usr/local/opt/python@3.7/bin/pip3 install numpy
/usr/local/opt/python@3.7/bin/pip3 install numpy
/usr/local/opt/python@3.7/bin/pip3 install python-javabridge
```

## Build and run the Docker image

To build a docker image that  will run in the same cluster as the FHIR server you will use for testing, run:

```bash
docker build -t taurus-javabridge -f {Your path to}/quality-measure-and-cohort-service/tests/docker/taurus-bridge.dockerfile .
```

If you are building an image that will use port forwarding to access the FHIR server, you must instead build an image
using the `taurus-bridge-portForwarding.dockerfile` dockerfile:

```bash
docker build -t taurus-javabridge-portforwarding -f {Your path to}/quality-measure-and-cohort-service/tests/docker/taurus-bridge-portForwarding.dockerfile .
```

After building the docker image, you can run tests with:

```bash
docker run -it --rm --env-file {Your path to}/quality-measure-and-cohort-service/tests/docker/Env.txt -v {Your path to}/quality-measure-and-cohort-service:/bzt-configs -v {Your path to}/quality-measure-and-cohort-service/tests/results:/tmp/artifacts {docker-image-name} ./tests/scenarios/{test-scenarios}
```

Where {docker-image-name} is `taurus-javabridge` or `taurus-javabridge-portForwarding` in the examples above and {test-scenarios} is a yaml file
containing the tests to run.

### Environemnt file for docker runs

Running Docker involves passing in a file containing environment variables to use during the run with the
`--env-file` argument. `quality-measure-and-cohort-service/tests/docker/Env.txt` contains the expected environment
variables along with a comment of what each variable is for. As at least one of the variables is for an API key, it is
recommended to populate a separate file outside of git to use during your Docker runs rather than updating the provided
`Env.txt` file directly.

