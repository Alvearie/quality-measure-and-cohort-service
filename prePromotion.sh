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
else
  echo "This is not an Cohorting release for development. Moving on..."
fi