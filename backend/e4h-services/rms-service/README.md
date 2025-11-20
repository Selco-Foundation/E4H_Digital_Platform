# RMS Service

The RMS (Remote Monitoring System) Service is responsible for collecting telemetry data from RMS devices (Solar, Inverter, Battery, Grid), applying anomaly detection rules, and automatically generating tickets in Saura eMitra (IM Service) when issues are detected.

## Architecture

```
Device Telemetry → Data Collector → Rule Engine → Deduplication Manager → Payload Generator → Saura eMitra Connector → IM Service
```

### Key Components

1. **Data Collector Layer**: Fetches telemetry data from RMS APIs
   - **Currently Working**: Panel data (solar vs grid consumption) - `center_details/graph` ✅
   - **Currently Working**: Inverter data (no signal detection) - `centerDatas/get` ✅
   - **Currently Working**: Inverter data (high voltage) - `center_details/graph` ✅
   - **Disabled**: Battery data (voltage readings) - endpoint not available
   - **Disabled**: Grid data (voltage readings) - endpoint not available

2. **Rule Engine Layer**: Applies anomaly detection rules
   - **Active**: Panel: Solar consumption < 10% for 7 consecutive days ✅
   - **Active**: Inverter: No signal for 2+ days ✅
   - **Active**: Inverter: High voltage > 250V ✅
   - **Disabled**: Battery: Voltage = 0 (burnt/disconnected) (endpoint not available)
   - **Disabled**: Grid: Voltage < 200V (low) or > 250V (high) (endpoint not available)

3. **Deduplication Manager**: Prevents duplicate tickets
   - Maintains active_alerts table
   - Suppression window (default: 24 hours)
   - Prevents repeat tickets until resolved

4. **Payload Generator**: Converts alerts to IM service ticket format
   - Maps alert types to incident types/subtypes
   - Fetches facility details from Facility Registry
   - Builds complete ticket payload

5. **Saura eMitra Connector**: Calls IM service API
   - REST client with retry logic
   - Exponential backoff on failures
   - Updates alert with ticket ID on success

## Configuration

### Application Properties

- `rms.api.base.url`: RMS API base URL
- `rms.api.access.token`: RMS API access token
- `im.service.base.url`: IM service base URL
- `rms.scheduler.rule.engine.cron`: Cron expression for rule engine (default: every 15 minutes)
- `rms.scheduler.solar.daily.cron`: Cron expression for daily solar analysis (default: 1 AM daily)
- `rms.rule.*`: Rule thresholds and configuration
- `rms.deduplication.suppression.window.hours`: Suppression window (default: 24 hours)

## Database Schema

### active_alerts
Stores active alerts for deduplication and tracking.

### telemetry_data
Stores raw telemetry data collected from RMS APIs (optional, for historical analysis).

### alert_history
Tracks all alerts for audit purposes.

### center_id_to_hfr_id_mapping
Stores the mapping between RMS Center ID (Device ID) and HFR ID. This table:
- Maps RMS Center IDs to HFR IDs for facility identification
- Tracks device instance IDs and facility names
- Maintains sync and validation timestamps
- Automatically syncs every 7 days via scheduled job
- Used to enrich facility data with HFR IDs when not provided by RMS APIs

## API Endpoints

### POST /rms-service/v1/trigger
Manually triggers the RMS workflow execution.

### POST /rms-service/v1/mapping/sync
Manually triggers the Center ID to HFR ID mapping sync. This will:
- Collect facility data from RMS APIs
- Update/create mappings in the database
- Enrich facilities with HFR IDs when available

### POST /rms-service/v1/mapping/validate
Manually triggers mapping validation. This will:
- Check if mapped facilities still exist in Facility Registry
- Mark inactive mappings for facilities that no longer exist
- Update validation timestamps

## Scheduled Jobs

1. **Rule Engine**: Executes every 15 minutes (configurable)
   - Collects data from all RMS APIs
   - Applies all rule types
   - Creates tickets for new alerts

2. **Solar Daily Analysis**: Executes daily at 1 AM (configurable)
   - Focuses on panel-level analysis (requires 7 days of data)

3. **Mapping Sync**: Executes weekly on Sunday at 2 AM (configurable)
   - Syncs Center ID to HFR ID mappings from RMS API data
   - Updates existing mappings or creates new ones
   - Enriches facilities with HFR IDs

4. **Mapping Validation**: Executes weekly on Sunday at 3 AM (configurable)
   - Validates mappings older than 7 days
   - Checks if facilities still exist in Facility Registry
   - Marks inactive mappings for obsolete facilities

## Alert Types and Subtypes

### Currently Active

### Panel
- **Low Generation**: Solar consumption < 10% for 7 consecutive days ✅ (WORKING)

### Inverter
- **Shutdown**: No signal for 2+ consecutive days ✅ (WORKING)
- **High Voltage**: UPS/PCU voltage > 250V ✅ (WORKING)

### Disabled (Endpoints Not Available)

### Inverter
- **High Voltage**: PCU voltage > 250V ❌ (endpoint not available)

### Battery
- **Burnt/Disconnected**: Battery voltage = 0 ❌ (endpoint not available)

### Grid
- **Voltage Variation - Low**: Grid voltage < 200V ❌ (endpoint not available)
- **Voltage Variation - High**: Grid voltage > 250V ❌ (endpoint not available)

## Development

### Building
```bash
mvn clean install
```

### Running
```bash
mvn spring-boot:run
```

## Integration Points

1. **RMS APIs**: Fetches telemetry data
2. **Facility Registry**: Fetches facility details by hfrId
3. **IM Service**: Creates tickets via `/im-services/v2/request/_create`
4. **User Service**: Uses system user for ticket creation

## Center ID to HFR ID Mapping

The RMS service maintains a mapping table (`center_id_to_hfr_id_mapping`) that maps RMS Center IDs (Device IDs) to HFR IDs. This mapping is essential because:

1. **RMS APIs use Center IDs**: The RMS APIs return Center IDs (Device IDs) for facilities
2. **Ticket creation requires HFR IDs**: The IM service requires HFR IDs to create tickets
3. **Mapping is not always present**: Some RMS API responses may not include HFR IDs

### How It Works

1. **Data Collection**: When collecting data from RMS APIs, the service extracts Center IDs from responses
2. **Mapping Lookup**: For each Center ID, the service looks up the corresponding HFR ID in the mapping table
3. **Auto-enrichment**: If HFR ID is found, it's automatically added to the facility data
4. **Mapping Sync**: Weekly sync job collects all facilities and updates the mapping table
5. **Validation**: Weekly validation job checks if mapped facilities still exist

### Mapping Sync Process

1. Collects facility data from all working RMS API endpoints
2. Extracts Center IDs and HFR IDs (when available) from responses
3. Creates or updates mappings in the database
4. Tracks sync timestamps for audit purposes

### Handling Missing HFR IDs

- If HFR ID is missing from RMS API response, the service looks it up from the mapping table
- If still not found, the alert is still created but ticket creation may fail if HFR ID is required
- Missing mappings are logged for manual review

## Notes

- The service requires valid RMS API access token
- Facility Registry must have hfrId mapping for facilities
- IM Service must be configured with appropriate incident types/subtypes
- System user UUID must be configured in application.properties
- Center ID to HFR ID mapping is automatically maintained via scheduled jobs
- Mappings are validated every 7 days to ensure they're still valid

