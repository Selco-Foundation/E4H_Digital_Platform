# RMS Service

The RMS (Remote Monitoring System) Service is responsible for collecting telemetry data from RMS devices (Solar, Inverter, Battery, Grid), applying anomaly detection rules, and automatically generating tickets in Saura eMitra (IM Service) when issues are detected.

## Architecture

```
Device Telemetry → Data Collector → Rule Engine → Deduplication Manager → Payload Generator → Saura eMitra Connector → IM Service
```

### Key Components

1. **Data Collector Layer**: Fetches telemetry data from RMS APIs
   - Panel data (solar vs grid consumption)
   - Inverter data (signal status, voltage)
   - Battery data (voltage readings)
   - Grid data (voltage readings)

2. **Rule Engine Layer**: Applies anomaly detection rules
   - Panel: Solar consumption < 10% for 7 consecutive days
   - Inverter: No signal for 2+ days, High voltage > 250V
   - Battery: Voltage = 0 (burnt/disconnected)
   - Grid: Voltage < 200V (low) or > 250V (high)

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

## API Endpoints

### POST /rms-service/v1/trigger
Manually triggers the RMS workflow execution.

## Scheduled Jobs

1. **Rule Engine**: Executes every 15 minutes (configurable)
   - Collects data from all RMS APIs
   - Applies all rule types
   - Creates tickets for new alerts

2. **Solar Daily Analysis**: Executes daily at 1 AM (configurable)
   - Focuses on panel-level analysis (requires 7 days of data)

## Alert Types and Subtypes

### Panel
- **Low Generation**: Solar consumption < 10% for 7 consecutive days

### Inverter
- **Shutdown**: No signal for 2+ consecutive days
- **High Voltage**: PCU voltage > 250V

### Battery
- **Burnt/Disconnected**: Battery voltage = 0

### Grid
- **Voltage Variation - Low**: Grid voltage < 200V
- **Voltage Variation - High**: Grid voltage > 250V

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

## Notes

- The service requires valid RMS API access token
- Facility Registry must have hfrId mapping for facilities
- IM Service must be configured with appropriate incident types/subtypes
- System user UUID must be configured in application.properties

