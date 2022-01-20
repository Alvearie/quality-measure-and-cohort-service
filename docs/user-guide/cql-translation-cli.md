# CQL Translation CLI

The `cohort-cli` shaded jar includes a command-line utility for translating CQL files.

```
Usage: cql-translation [options]
  Options:
    -b, --dependency-directory
      Directory containing additional files necessary for primary file's 
      translation 
  * -f, --files
      Path to cql file
    -h, --help
      Display this help
    -i, --model-info
      Model info file used when translating CQL
```

An example invocation of the cli is as follows:
```bash
java -jar cohort-cli/target/cohort-cli-VERSION-SNAPSHOT-shaded.jar translation-cli -b path/to/deps -f path/to/cql-file -i path/to/custom/model-info
```

If translation is successful, a string representation of the translated library object is displayed. Any errors
encountered during translation will cause the program to halt and display the error message.