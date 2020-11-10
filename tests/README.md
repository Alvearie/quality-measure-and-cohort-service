Mac OS setup steps.
Install Taurus.
    brew install bzt
    brew update bzt
Install python-javabridge (openblas may not be necessary on non-mac systems)
    brew install openblas
    OPENBLAS="$(brew --prefix openblas)" /usr/local/opt/python@3.7/bin/pip3 install numpy
    /usr/local/opt/python@3.7/bin/pip3 install numpy
    /usr/local/opt/python@3.7/bin/pip3 install python-javabridge



docker run -it --rm -v {Your path}/CohortEngine/quality-measure-and-cohort-service:/bzt-configs -v {Your path}/CohortEngine/quality-measure-and-cohort-service/tests/results:/tmp/artifacts taurus-javabridge ./tests/scenarios/sampleJson.yaml