# SoapUI Open Source Docker Image

Custom Docker image for running SoapUI tests in CI/CD pipelines.

## Build

```bash
docker build -t tejasdesai27/soapui:5.7.0 .
docker tag tejasdesai27/soapui:5.7.0 tejasdesai27/soapui:latest
```

## Push to Docker Hub

```bash
docker push tejasdesai27/soapui:5.7.0
docker push tejasdesai27/soapui:latest
```

## Usage

```bash
# Run tests
docker run --rm -v $(pwd):/workspace tejasdesai27/soapui:latest \
  -e"http://localhost:8080" \
  -f/workspace/soapui-results \
  -j -r \
  /workspace/tests/soapui/banking-app-soapui-project.xml
```

## Parameters

- `-e` - Endpoint URL (base URL for API)
- `-f` - Results output directory
- `-j` - Generate JUnit XML reports
- `-r` - Generate printable reports
- `-a` - Run all test cases
- `-s` - Specific test suite to run
- `-c` - Specific test case to run

## In CloudBees Unify Workflow

```yaml
- name: Run SoapUI API Tests
  uses: docker://tejasdesai27/soapui:latest
  run: |
    testrunner.sh \
      -e"$BASE_URL" \
      -f/cloudbees/workspace/soapui-results \
      -j -r \
      /cloudbees/workspace/tests/soapui/banking-app-soapui-project.xml
```
