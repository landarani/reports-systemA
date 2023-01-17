# reports-systemA
Please note that Java17 is the minimum version of java for this app

The application scans the files in the provided input folder and generates trade reports for each given date in the provided output folder

# Execution
## dev
starting the server spring
```
mvn spring-boot:run -f app/pom.xml

```

### packaging
set a version to an arbitrary number. Usually your build script does that
```
mvn -B versions:set -DnewVersion=20230118
mvn -B package
```

### Building Docker Image
The version which is set in packaging should be speciified as ARTIFACT_VERSION
```
docker build -t cotalent/reporting --build-arg ARTIFACT_VERSION=1.0.0-SNAPSHOT -f docker/Dockerfile .
docker compose -p reporting -f docker/docker-compose.yaml up
```

## prod
```
java -jar app/target/systemA-reporting-app-<version>.jar
```
# Usage
Rest EP Examples:
```
http://localhost:8080/reports/2023-01-18
http://localhost:8080/reports/2023-01-18/csv
```
# Configuration
```
reports.base-folder.input=<input base folder>
reports.base-folder.output=<outpur base folder>
```
 
## Folder structure
```
<input base folder>/<dates in format of yyyyMMdd>/Input.txt
<output base foldet>/<dates in format of yyyyMMdd>/Output.csv
```
example
```
── systemAInput
    ├── 20230115
    │   └── Input.txt
    ├── 20230116
    │   └── Input.txt
    ├── 20230117
    │   └── Input.txt
    └── 20230118
        └── Input.txt
── systemAOutput
    ├── 20230115
    ├── 20230116
    ├── 20230117
    └── 20230118

reports.base-folder.input=/systemAInput
reports.base-folder.output=/systemAOutput

Output folder after startting program:
── systemAOutput
    ├── 20230115
    │   └── Output.csv
    ├── 20230116
    │   └── Output.csv
    ├── 20230117
    │   └── Output.csv
    └── 20230118
        └── Output.csv

```
