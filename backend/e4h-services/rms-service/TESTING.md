# Quick Testing Guide for RMS Service

## Currently Working Endpoints

Only 2 RMS API endpoints are available:

1. ✅ **Inverter No Signal**: `POST /selco/cachedData/centerDatas/get`
2. ✅ **Panel Low Generation**: `POST /selco/center_details/graph`

## Quick Start Testing

### 1. Start the Service

```bash
cd backend/e4h-services/rms-service
mvn spring-boot:run
```

### 2. Test Manually via API

```bash
# Trigger the workflow
curl -X POST http://localhost:8885/rms-service/v1/trigger
```

### 3. Check Logs

```bash
# Watch for:
# - Data collection from RMS APIs
# - Alerts generated
# - Tickets created
```

### 4. Verify Database

```sql
-- Check active alerts
SELECT * FROM active_alerts;

-- Check tickets created
SELECT * FROM active_alerts WHERE ticket_id IS NOT NULL;
```

## Test the RMS APIs Directly

### Test Inverter No Signal Endpoint

```bash
curl -k -X 'POST' \
  'https://selco.theiox.com/selco/cachedData/centerDatas/get' \
  -H 'accept: application/json' \
  -H 'Access-Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MTAwMiwidXNlcl9pZCI6InVzZXJfMTAwMDIiLCJzaXRlX2lkIjoiaW5kdXN0cnlfMzQzIiwiY2xpZW50X2lkIjoiY2xpZW50XzIwNyIsImV4cCI6MjU5NzY5NjMzOH0.0VyQMHQl5sbPs2bSXPqijeJXFrUvM57Y0J_CkbdrOeI' \
  -d '{
  "status": [
    {
      "label": "Inactive"
    }
  ],
  "pagination": {
    "page":1,
    "size":10000
  }
}'
```

### Test Panel Data Endpoint

```bash
curl -k -X 'POST' \
  'https://selco.theiox.com/selco/center_details/graph' \
  -H 'accept: application/json' \
  -H 'Access-Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MTAwMiwidXNlcl9pZCI6InVzZXJfMTAwMDIiLCJzaXRlX2lkIjoiaW5kdXN0cnlfMzQzIiwiY2xpZW50X2lkIjoiY2xpZW50XzIwNyIsImV4cCI6MjU5NzY5NjMzOH0.0VyQMHQl5sbPs2bSXPqijeJXFrUvM57Y0J_CkbdrOeI' \
  -H 'Content-Type: application/json' \
  -d '{
"graphType":"solarVsGrid_Eb_Diff",
"time_range":{"time_period":{"label":"Last 7 days","value":"last_seven_days"},
"custom_range":{}},
"frequency":"daily",
"aggregation":"deltaSum",
"filters": {"solarConsumptionPercent":{"compareFunction":"lte", "compareValue": 60}},
"pagination": {"page":1, "size":100}
}'
```

## Expected Behavior

1. **Data Collection**: Service fetches data from both endpoints
2. **Rule Application**: 
   - Panel: Finds facilities with solar < 10% for 7 days
   - Inverter: Finds facilities with no signal for 2+ days
3. **Deduplication**: Removes duplicate alerts
4. **Ticket Creation**: Creates tickets in IM service for new alerts

## Troubleshooting

### No data collected?
- Check RMS API is accessible
- Verify access token in `application.properties`
- Check network connectivity

### No alerts generated?
- Verify data meets rule criteria
- Check rule thresholds in `application.properties`
- Review logs for rule engine execution

### Tickets not created?
- Ensure IM service is running
- Verify Facility Registry has hfrId mappings
- Check system user UUID configuration

## Next Steps

Once RMS team provides additional endpoints:
- Uncomment disabled methods in `RMSOrchestratorService`
- Add tests for new endpoints
- Update configuration

