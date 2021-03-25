For the rest tests, we use the swagger-codegen. As seen here. https://github.com/swagger-api/swagger-codegen#

When new API's are added to swagger perform the following.
1. Delete (or archive) the extract folder under /python/rest/tests
2. Start swagger on localhost.'kubectl -n dev port-forward service/cohort-services-cohort-cohort 9443:9443'
3. Pull the swagger JSON. 'curl -k https://localhost:9443/services/cohort/api/swagger/swagger.json > swagger.json'
4. Run generator 'swagger-codegen generate -i swagger.json -l python -o ***/python/rest/tests/extract/' Use correct path.
5. Note, if old api's have been updated this may cause some tests to fail.
Never change any file in the "extract" folder. This has been designed that the extract folder is fully generated.