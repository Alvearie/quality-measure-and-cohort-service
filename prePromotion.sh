#Print commands and their arguments as they are executed.
set -x

function readd_tests(){
  echo "./tests/run-tests.sh fvttest.xml" >>run-fvttests.sh
  echo "./tests/run-tests.sh ivttest.xml" >>run-ivttests.sh
  echo "./tests/run-tests.sh regtest.xml" >>run-regrtests.sh
}

if [ "$UMBRELLA_REPO_PATH" == "https://github.ibm.com/watson-health-cohorting/wh-cohorting-umbrella" ]; then
  if [ -f ./application.version ]; then
    APPLICATION_VERSION="$(cat application.version)"
  else
    echo "There is not an application.version file"
    exit 1
  fi

  echo "Validating versions are updated in the appropriate files"
  APP_NAME='cohort-services'
  COMPONENTNAME='cohort'
  PATH_TO_COMPONENT="${APP_NAME}/${COMPONENTNAME}"
  CHART_VERSION=$(cat "$PATH_TO_COMPONENT/chart/cohort/Chart.yaml" | grep 'version:' | sed -E 's/^version: (.+)$/\1/g')
  if [ "$APPLICATION_VERSION" != "$CHART_VERSION" ]; then
    echo "The chart version '$CHART_VERSION' does not match the application version '$APPLICATION_VERSION' you are releasing. Please make sure you have updated the helm chart version and the application.version file to match."
    exit 1
  fi

  echo "Removing all unnecessary files except for smoke tests"
  rm -f README.md
#  rm -f mkdocs.yml
  echo "Make README for wh-cohorting-deploy"
  echo "# wh-cohorting-deploy
  This repo is to assist teams in deployment to both CDT and CSP. For more information on deployments, see [(https://github.com/Alvearie/quality-measure-and-cohort-service#readme) Github readme." >>README.md
  cd ./$PATH_TO_COMPONENT/tests/
  find . -not -name '*smoketest*' -not -name '*run-tests*' -not -name '*authenticate*' -not -path "./tests" -not -path "." >>remove_tests.txt
  cat remove_tests.txt
  xargs rm -rf <remove_tests.txt
  readd_tests
  chmod +x run-tests.sh
else
  echo "This is not an Cohorting release for development. Moving on..."
fi

  echo "Promoting Spark image that doesn't have helm chart"
# CDT login
ibmcloud login -a "https://cloud.ibm.com/" --apikey "${CDTKEY}" -r us-south
ibmcloud target --unset-resource-group
ibmcloud cr login
ibmcloud cr region-set dallas

echo "Pulling spark image: ${SPARK_IMG}"
docker pull ${SPARK_IMG}

# CSP login
ibmcloud login -a "https://cloud.ibm.com/" --apikey "${CSPKEY}" -r us-south
ibmcloud target --unset-resource-group
ibmcloud cr login
ibmcloud cr region-set dallas

src_ns=$(echo "${SPARK_IMG}" | awk -F'/' '{print $2}')
dstimg=$(echo "${SPARK_IMG}" | sed "s/${src_ns}/${DST_NAMESPACE}/")

if ibmcloud cr image-inspect $dstimg 2>&1 | grep "The image was not found."; then
  echo "DSTIMG NOT found in OPS registry: $dstimg Image will now be tagged and pushed"
  docker tag ${SPARK_IMG} $dstimg
  docker push $dstimg
else
  echo "DSTIMG FOUND in OPS registry, not promoting: $dstimg"
fi
