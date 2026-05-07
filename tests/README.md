# Banking App - API & Performance Testing

This directory contains SoapUI and JMeter test configurations for the Banking Application.

---

## **📁 Directory Structure**

```
tests/
├── soapui/
│   └── banking-app-soapui-project.xml    # SoapUI REST API tests
├── jmeter/
│   └── banking-app-load-test.jmx         # JMeter performance tests
└── README.md                              # This file
```

---

## **🧪 SoapUI API Tests**

### **Test Suite: Banking App API Tests**

**Location:** `tests/soapui/banking-app-soapui-project.xml`

**Purpose:** Functional REST API testing with assertions

### **Test Cases:**

#### **1. Health Check Test**
- **Endpoint:** `GET /actuator/health`
- **Assertions:**
  - HTTP 200 OK
  - JSON response: `status = "UP"`

#### **2. Account CRUD Test**
- **Endpoints:**
  - `GET /api/accounts` - List all accounts
  - `POST /api/accounts` - Create new account
- **Assertions:**
  - HTTP 200/201
  - JSON response validation
  - Response SLA < 1 second

#### **3. Account Operations Test**
- **Endpoints:**
  - `POST /api/accounts/{id}/deposit` - Deposit money
  - `POST /api/accounts/{id}/withdraw` - Withdraw money
- **Assertions:**
  - HTTP 200
  - Balance updates correctly
  - Response SLA < 1 second

#### **4. User Management Test**
- **Endpoints:**
  - `POST /api/users` - Create user
  - `GET /api/users` - List all users
- **Assertions:**
  - HTTP 200
  - JSON response validation
  - User data correctness

### **Running SoapUI Tests**

**Using Docker:**
```bash
docker run --rm -v $(pwd):/workspace smartbear/soapui-testrunner:latest \
  -e"http://localhost:8080" \
  -f/workspace/soapui-results \
  -j -r \
  /workspace/tests/soapui/banking-app-soapui-project.xml
```

**Parameters:**
- `-e` - Endpoint URL (banking app base URL)
- `-f` - Results output directory
- `-j` - Generate JUnit XML reports
- `-r` - Generate printable reports

**Expected Output:**
```
SoapUI 5.x.x TestCase Runner
...
TestSuite [Banking API Test Suite] finished with status [FINISHED] in 2345ms
```

---

## **⚡ JMeter Performance Tests**

### **Test Plan: Banking App Load Test**

**Location:** `tests/jmeter/banking-app-load-test.jmx`

**Purpose:** Load and performance testing

### **Test Configuration:**

**Variables (customizable):**
- `BANKING_APP_URL` - Base URL (default: `http://banking-app:8080`)
- `NUM_USERS` - Number of concurrent users (default: `50`)
- `RAMP_UP` - Ramp-up time in seconds (default: `10`)
- `DURATION` - Test duration in seconds (default: `60`)

### **Thread Groups:**

#### **1. Read Operations Load Test**
- **Virtual Users:** 50 (configurable)
- **Ramp-Up:** 10 seconds
- **Duration:** 60 seconds
- **Scenarios:**
  - Health check (`GET /actuator/health`)
  - Get all accounts (`GET /api/accounts`)
- **Assertions:**
  - HTTP 200
  - Response time < 500ms

#### **2. Write Operations Load Test**
- **Virtual Users:** 10
- **Ramp-Up:** 5 seconds
- **Duration:** 60 seconds
- **Scenarios:**
  - Create account (`POST /api/accounts`)
  - Deposit money (`POST /api/accounts/{id}/deposit`)
- **Think Time:** 1-3 seconds between requests

### **Performance Thresholds:**

| Metric | Target | Acceptable |
|--------|--------|------------|
| Error Rate | < 1% | < 5% |
| Avg Response Time | < 300ms | < 500ms |
| P95 Response Time | < 500ms | < 1000ms |
| Throughput | > 100 TPS | > 50 TPS |

### **Running JMeter Tests**

**Using Docker:**
```bash
docker run --rm -v $(pwd):/workspace justb4/jmeter:latest \
  -n \
  -t /workspace/tests/jmeter/banking-app-load-test.jmx \
  -JBANKING_APP_URL="http://localhost:8080" \
  -JNUM_USERS=50 \
  -JRAMP_UP=10 \
  -JDURATION=60 \
  -l jmeter-results.jtl \
  -e -o /workspace/jmeter-report
```

**Parameters:**
- `-n` - Non-GUI mode
- `-t` - Test plan file
- `-J<name>=<value>` - Set JMeter property
- `-l` - Results log file (JTL format)
- `-e -o` - Generate HTML report

**Expected Output:**
```
summary = 15000 in 60.1s = 249.6/s Avg: 42 Min: 5 Max: 523 Err: 0 (0.00%)
```

---

## **🔄 CloudBees Unify Workflow Integration**

### **Workflow:** `ci-with-api-tests.yaml`

**Pipeline Flow:**
```
1. Unit Tests (Smart Tests)
   ↓
2. Build Docker Image
   ↓
3. Deploy to QA
   ↓
4. SoapUI API Tests (parallel)
5. JMeter Performance Tests (parallel)
   ↓
6. QA Test Summary
```

**Evidence Published:**
- Unit test results
- SoapUI test results with assertions
- JMeter performance metrics
- QA sign-off summary

---

## **📊 Test Results**

### **SoapUI Results:**
- **Format:** JUnit XML
- **Location:** `soapui-results/TEST-*.xml`
- **Published:** CloudBees Unify Evidence

### **JMeter Results:**
- **Format:** JTL (CSV-like)
- **Location:** `jmeter-results.jtl`
- **HTML Report:** `jmeter-report/index.html`
- **Published:** CloudBees Unify Evidence

---

## **🎯 Test Scenarios**

### **Functional Testing (SoapUI):**
1. ✅ Health endpoint availability
2. ✅ Account CRUD operations
3. ✅ Deposit/Withdraw validation
4. ✅ User management
5. ✅ Response time SLA
6. ✅ JSON response structure

### **Performance Testing (JMeter):**
1. ✅ Concurrent user load (50 users)
2. ✅ Read operation performance
3. ✅ Write operation performance
4. ✅ Error rate under load
5. ✅ Response time distribution
6. ✅ System stability (60-second duration)

---

## **🔧 Local Testing**

### **Prerequisites:**
- Banking app running locally or in Kubernetes
- Docker installed (for test runners)

### **Quick Test:**

**1. Start Banking App:**
```bash
# Option A: Local
mvn spring-boot:run

# Option B: Kubernetes
kubectl port-forward -n tejas-qa svc/banking-app 8080:80
```

**2. Run SoapUI Tests:**
```bash
cd tests
docker run --rm -v $(pwd):/workspace smartbear/soapui-testrunner:latest \
  -e"http://host.docker.internal:8080" \
  -f/workspace/soapui-results \
  -j -r \
  /workspace/soapui/banking-app-soapui-project.xml
```

**3. Run JMeter Tests:**
```bash
docker run --rm -v $(pwd):/workspace justb4/jmeter:latest \
  -n -t /workspace/jmeter/banking-app-load-test.jmx \
  -JBANKING_APP_URL="http://host.docker.internal:8080" \
  -JNUM_USERS=10 \
  -JDURATION=30 \
  -l jmeter-results.jtl
```

---

## **📚 Resources**

### **SoapUI:**
- Documentation: https://www.soapui.org/docs/
- Docker Image: https://hub.docker.com/r/smartbear/soapui-testrunner

### **JMeter:**
- Documentation: https://jmeter.apache.org/usermanual/
- Docker Image: https://hub.docker.com/r/justb4/jmeter

### **CloudBees Unify:**
- Actions: https://docs.cloudbees.com/docs/cloudbees-platform/latest/actions/
- Evidence: https://docs.cloudbees.com/docs/cloudbees-platform/latest/evidence/

---

## **🚀 Next Steps**

### **For IGSL Implementation:**
1. ✅ Adapt SoapUI tests for IGSL services
2. ✅ Adapt JMeter tests for IGSL performance requirements
3. ✅ Configure thresholds for IGSL SLAs
4. ✅ Integrate with IGSL CI/CD pipeline
5. ✅ Add security tests (AiKido, Zap)

---

**Status:** ✅ Ready for testing!

**Run the new workflow:** `ci-with-api-tests.yaml`
