# RMS Service Testing Guide

This guide explains how to test the RMS Service with the currently available endpoints.

## Available Endpoints

Currently, only 2 RMS API endpoints are working:

1. **Inverter No Signal**: `POST /selco/cachedData/centerDatas/get`
2. **Panel Low Generation**: `POST /selco/center_details/graph`

## Testing Methods

### 1. Manual Testing via API

#### Test the Manual Trigger Endpoint

```bash
# Trigger the RMS workflow manually
curl -X POST http://localhost:8885/rms-service/v1/trigger
```

This will:
- Collect data from both working endpoints
- Apply rules for panel and inverter anomalies
- Deduplicate alerts
- Generate tickets in IM service

#### Check Logs

Monitor the application logs to see:
- Data collection progress
- Alerts generated
- Ticket creation status

```bash
# View logs
tail -f logs/rms-service.log
```

### 2. Unit Testing

#### Run Unit Tests

```bash
cd backend/e4h-services/rms-service
mvn test
```

#### Test Individual Components

```bash
# Test Data Collector
mvn test -Dtest=DataCollectorServiceTest

# Test Rule Engine
mvn test -Dtest=RuleEngineServiceTest

# Test Deduplication Manager
mvn test -Dtest=DeduplicationManagerTest
```

### 3. Integration Testing

#### Test Complete Workflow

```bash
# Run integration tests
mvn test -Dtest="**/*IntegrationTest"
```

### 4. Testing with Postman

#### Import Postman Collection

1. Create a new collection in Postman
2. Add the following requests:

**Request 1: Trigger RMS Workflow**
```
POST http://localhost:8885/rms-service/v1/trigger
Content-Type: application/json
```

**Request 2: Test RMS API - Inverter No Signal**
```
POST https://selco.theiox.com/selco/cachedData/centerDatas/get
Access-Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MTAwMiwidXNlcl9pZCI6InVzZXJfMTAwMDIiLCJzaXRlX2lkIjoiaW5kdXN0cnlfMzQzIiwiY2xpZW50X2lkIjoiY2xpZW50XzIwNyIsImV4cCI6MjU5NzY5NjMzOH0.0VyQMHQl5sbPs2bSXPqijeJXFrUvM57Y0J_CkbdrOeI
Content-Type: application/json

{
  "status": [
    {
      "label": "Inactive"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10000
  }
}
```

**Request 3: Test RMS API - Panel Data**
```
POST https://selco.theiox.com/selco/center_details/graph
Access-Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MTAwMiwidXNlcl9pZCI6InVzZXJfMTAwMDIiLCJzaXRlX2lkIjoiaW5kdXN0cnlfMzQzIiwiY2xpZW50X2lkIjoiY2xpZW50XzIwNyIsImV4cCI6MjU5NzY5NjMzOH0.0VyQMHQl5sbPs2bSXPqijeJXFrUvM57Y0J_CkbdrOeI
Content-Type: application/json

{
  "graphType": "solarVsGrid_Eb_Diff",
  "time_range": {
    "time_period": {
      "label": "Last 7 days",
      "value": "last_seven_days"
    },
    "custom_range": {}
  },
  "frequency": "daily",
  "aggregation": "deltaSum",
  "filters": {
    "solarConsumptionPercent": {
      "compareFunction": "lte",
      "compareValue": 60
    }
  },
  "pagination": {
    "page": 1,
    "size": 100
  }
}
```

### 5. Database Testing

#### Check Active Alerts

```sql
-- View all active alerts
SELECT * FROM active_alerts WHERE status = 'ACTIVE';

-- View alerts by type
SELECT * FROM active_alerts WHERE alert_type = 'INVERTER' AND alert_sub_type = 'SHUTDOWN';

-- View alerts with tickets created
SELECT * FROM active_alerts WHERE status = 'TICKET_CREATED';
```

#### Check Alert History

```sql
-- View alert history
SELECT * FROM alert_history ORDER BY detected_at DESC LIMIT 100;
```

#### Test Deduplication

```sql
-- Check if alert is suppressed
SELECT * FROM active_alerts 
WHERE facility_id = 'YOUR_FACILITY_ID' 
AND alert_type = 'INVERTER' 
AND alert_sub_type = 'SHUTDOWN'
AND last_suppressed_at > NOW() - INTERVAL '24 hours';
```

### 6. Testing Scenarios

#### Scenario 1: Test Inverter No Signal Detection

1. Ensure there are facilities with status "Inactive" and last_sync_time > 2 days ago
2. Trigger the workflow:
   ```bash
   curl -X POST http://localhost:8885/rms-service/v1/trigger
   ```
3. Check logs for:
   - Data collection from centerDatas/get
   - Alerts generated for inactive devices
   - Tickets created in IM service

#### Scenario 2: Test Panel Low Generation

1. Ensure there are facilities with solar consumption < 10% for 7 days
2. Trigger the workflow
3. Check logs for:
   - Data collection from center_details/graph
   - Panel alerts generated
   - Tickets created

#### Scenario 3: Test Deduplication

1. Trigger workflow twice within 24 hours
2. Verify only one ticket is created per alert
3. Check `active_alerts` table for suppression timestamps

#### Scenario 4: Test Ticket Creation

1. Mock IM service response (or ensure IM service is running)
2. Trigger workflow
3. Verify:
   - Alert status updated to 'TICKET_CREATED'
   - Ticket ID stored in `active_alerts.ticket_id`
   - Ticket created in IM service

## Test Configuration

### Test Properties

Create `src/test/resources/application-test.properties`:

```properties
# Test Database (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Disable Flyway for tests
flyway.enabled=false

# Mock RMS API (use WireMock or MockServer)
rms.api.base.url=http://localhost:8089

# Mock IM Service
im.service.base.url=http://localhost:8090

# Disable schedulers in tests
rms.scheduler.data.collector.enabled=false
rms.scheduler.rule.engine.enabled=false
rms.scheduler.solar.daily.enabled=false
```

## Troubleshooting

### Issue: No data collected

**Check:**
- RMS API is accessible
- Access token is valid
- Network connectivity

**Solution:**
```bash
# Test RMS API directly
curl -k -X POST 'https://selco.theiox.com/selco/cachedData/centerDatas/get' \
  -H 'Access-Token: YOUR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{"status":[{"label":"Inactive"}],"pagination":{"page":1,"size":10}}'
```

### Issue: No alerts generated

**Check:**
- Data meets rule criteria
- Rule thresholds in application.properties
- Logs for rule engine execution

**Solution:**
- Adjust thresholds in `application.properties`:
  ```properties
  rms.rule.solar.threshold.percent=10
  rms.rule.inverter.no.signal.days=2
  ```

### Issue: Tickets not created

**Check:**
- IM service is running
- Facility Registry has hfrId mappings
- System user UUID is configured

**Solution:**
```bash
# Test IM service
curl -X POST http://localhost:8880/im-services/v2/request/_create \
  -H 'Content-Type: application/json' \
  -d '{"RequestInfo":{...},"incident":{...}}'
```

## Expected Test Results

### Successful Execution

1. **Data Collection**: Logs show facilities collected from RMS APIs
2. **Rule Engine**: Logs show alerts generated
3. **Deduplication**: Logs show duplicate alerts filtered
4. **Ticket Creation**: Logs show tickets created successfully
5. **Database**: `active_alerts` table has new entries

### Sample Log Output

```
INFO  - Starting RMS workflow execution
INFO  - Collecting panel data for solar vs grid consumption analysis
INFO  - Collected panel data for 25 facilities
INFO  - Applying panel-level anomaly rules to 25 facilities
INFO  - Generated 5 panel-level alerts
INFO  - Deduplicating 5 alerts
INFO  - After deduplication: 3 unique alerts
INFO  - Creating tickets for 3 alerts
INFO  - Successfully created ticket KA-2025-01-20-0001 for alert: alert-id-123
INFO  - Ticket creation completed: 3 succeeded, 0 failed
INFO  - Completed RMS workflow execution
```

## Next Steps

Once RMS team provides additional endpoints, you can:

1. Uncomment the disabled methods in `RMSOrchestratorService`
2. Add tests for new endpoints
3. Update this testing guide

## Test Data

For testing, you can use:

```json
{
  "facilityId": "device_instance_13688_0a:74:6a:85:7e:59",
  "hfrId": "KA-YAD-PH-3946",
  "facilityName": "PHC Test Facility",
  "statusOfDevice": "Inactive",
  "lastSyncTime": "2025-01-15T06:39:00Z",
  "solarPercent": [4, 6, 7, 5, 6, 8, 6]
}
```

## Performance Testing

### Load Testing

```bash
# Test with multiple concurrent requests
ab -n 100 -c 10 http://localhost:8885/rms-service/v1/trigger
```

### Memory Testing

Monitor memory usage during execution:
```bash
# Using jstat
jstat -gc <pid> 1000
```

## Continuous Testing

### Run Tests in CI/CD

```yaml
# Example GitHub Actions
- name: Run Tests
  run: |
    cd backend/e4h-services/rms-service
    mvn clean test
```

### Test Coverage

```bash
# Generate coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

