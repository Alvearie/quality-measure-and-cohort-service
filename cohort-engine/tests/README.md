Mac OS setup steps.
Install Taurus.
    brew install bzt
    brew update bzt
Install python-javabridge (openblas may not be necessary on non-mac systems)
    brew install openblas
    OPENBLAS="$(brew --prefix openblas)" /usr/local/opt/python@3.7/bin/pip3 install numpy
    /usr/local/opt/python@3.7/bin/pip3 install numpy
    /usr/local/opt/python@3.7/bin/pip3 install python-javabridge