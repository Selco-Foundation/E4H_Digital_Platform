# Legacy Ticket Ingestion API - Developer Documentation

## Overview

The `upload_legacy_ticket_excel_sheet` endpoint is designed to ingest legacy ticket data from Excel files into the Incident Management (IM) system. This endpoint processes historical ticket data and creates new incidents in the system while maintaining traceability through legacy IDs.

## API Endpoint

```
POST /legacy_ticket_ingestion
```

## Request Parameters

### Form Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `legacy_ticket_file` | UploadFile | Yes | - | Excel file containing legacy ticket data |
| `legacy_ticket_sheet_name` | String | No | "Legacy Tickets" | Name of the sheet containing legacy tickets |
| `request_info` | String | No | "" | JSON string containing request information for authorization |
| `im_services_url` | String | No | Environment variable | URL for IM services API |
| `facility_service_url` | String | No | Environment variable | URL for facility service API |
| `hrms_service_url` | String | No | Environment variable | URL for HRMS service API |
| `mdms_url` | String | No | Environment variable | URL for MDMS service API |

## Excel File Requirements

### Required Columns

The Excel file must contain the following columns:

| Column Name | Description | Data Type | Required |
|-------------|-------------|-----------|----------|
| `State` | State/tenant ID | String | Yes |
| `NIN_HFR ID` | Healthcare facility ID | String | Yes |
| `code` | Employee username/code | String | Yes |
| `Ticket Type` | Type of incident | String | Yes |
| `Ticket Sub Type` | Subtype of incident | String | Yes |
| `Is the solar system working?` | System functional status | String | Yes |
| `Comments` | Additional comments | String | No |
| `Unique_ID` | Legacy ticket ID | String | No |
| `Actual_Reported_Date (mm/dd/yyyy)` | Date when ticket was reported | Date | No |

### Optional Columns

- Any additional columns will be preserved in the output file but not processed

## Step-by-Step Process

### 1. Request Validation and Setup

```python
# Parse and validate request info
request_info_obj = request_info_from_json(request_info)
get_authorized_request_info(request_info_obj)

# Generate unique migration ID for batch tracking
migration_id = str(uuid.uuid4())

# Fetch tenant mapping for PHC subtypes
tenant_mapping = get_tenant_mapping(request_info_obj, mdms_url)
```

### 2. File Processing Setup

```python
# Create temporary files for input and output
input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")

# Read Excel file
df = pd.read_excel(excel_file_path, sheet_name=legacy_ticket_sheet_name)

# Add status tracking columns
df['status'] = ''
df['error'] = ''
df['ticket_id'] = ''
df['healthcare_center_info'] = ''
df['employee_info'] = ''
```

### 3. Data Processing Loop

For each row in the Excel file:

#### 3.1 Employee Validation
```python
# Fetch employee information from HRMS
employee_info = get_hrms_employee_info(code, request_info_obj, hrms_service_url)
if not employee_info:
    # Mark as failed - employee not found
    df.at[idx, 'status'] = 'failed'
    df.at[idx, 'error'] = f'Employee not found for code: {code}'
    continue
```

#### 3.2 Healthcare Center Validation
```python
# Fetch healthcare center information
hc_info = get_healthcare_center_info(nin_hfr_id, state, request_info_obj, facility_service_url)
if not hc_info:
    # Mark as failed - healthcare center not found
    df.at[idx, 'status'] = 'failed'
    df.at[idx, 'error'] = f'Healthcare center not found for NIN_HFR ID: {nin_hfr_id}'
    continue
```

#### 3.3 Payload Construction
```python
# Build incident payload
incident_payload = {
    "incidentType": str(row.get("Ticket Type", "")).strip(),
    "incidentSubType": str(row.get("Ticket Sub Type", "")).strip(),
    "systemFunctional": str(row.get("Is the solar system working?", "")).strip(),
    "comments": str(row.get("Comments", "")).strip(),
    "tenantId": state,
    "migrationId": migration_id,
    "district": hc_info.get("district", ""),
    "block": hc_info.get("block", ""),
    "phcType": hc_info.get("facilityType", ""),
    "phcSubType": phc_subtype or nin_hfr_id,
    "reporter": {
        "uuid": employee_info.get("uuid"),
        "name": employee_info.get("name"),
        "mobileNumber": employee_info.get("mobileNumber")
    }
}

# Add legacy ID if present
if pd.notnull(row.get("Unique_ID")):
    incident_payload["legacyId"] = str(row.get("Unique_ID")).strip()

# Add reported date if present
if pd.notnull(row.get("Actual_Reported_Date (mm/dd/yyyy)")):
    # Convert to epoch timestamp
    dt = pd.to_datetime(reported_date, errors='coerce')
    if pd.notnull(dt):
        incident_payload["filedDate"] = int(dt.timestamp() * 1000)
```

#### 3.4 API Call
```python
# Prepare final payload
payload = {
    "RequestInfo": request_info_obj,
    "incident": incident_payload
}

# Call IM services API
response = requests.post(im_services_url, json=payload)

# Handle response
if response.status_code in (200, 201):
    resp_json = response.json()
    incident_id = resp_json.get("incident", {}).get("incidentId") or resp_json.get("incidentId")
    df.at[idx, 'status'] = 'success'
    df.at[idx, 'ticket_id'] = incident_id or ''
else:
    df.at[idx, 'status'] = 'failed'
    error_msg = response.json().get('Errors', [{}])[0].get('message', response.text)
    df.at[idx, 'error'] = error_msg
```

### 4. Output Generation

```python
# Write results to output Excel file
with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
    df.to_excel(writer, sheet_name=legacy_ticket_sheet_name, index=False)

# Return processed file
return FileResponse(
    path=output_file_path,
    filename=output_filename,
    media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
)
```

## Helper Functions

### get_healthcare_center_info()
Fetches healthcare center information from the facility service using NIN_HFR ID and state.

**Parameters:**
- `nin_hfr_id`: Healthcare facility ID
- `state`: State/tenant ID
- `request_info`: Request information for authorization
- `facility_service_url`: Facility service URL

**Returns:** Healthcare center information dictionary or None

### get_hrms_employee_info()
Fetches employee information from HRMS using employee code.

**Parameters:**
- `code`: Employee username/code
- `request_info`: Request information for authorization
- `hrms_service_url`: HRMS service URL

**Returns:** Employee information dictionary or None

### get_tenant_mapping()
Fetches tenant mapping from MDMS for PHC subtypes.

**Parameters:**
- `request_info`: Request information for authorization
- `mdms_url`: MDMS service URL

**Returns:** Tenant mapping dictionary

## Response Format

The API returns a processed Excel file with the following additional columns:

| Column | Description |
|--------|-------------|
| `status` | Processing status: 'success' or 'failed' |
| `error` | Error message if processing failed |
| `ticket_id` | New incident ID if successfully created |
| `healthcare_center_info` | Status of healthcare center lookup |
| `employee_info` | Status of employee lookup |

## Error Handling

### Common Error Scenarios

1. **Employee Not Found**
   - Error: `Employee not found for code: {code}`
   - Action: Verify employee code exists in HRMS

2. **Healthcare Center Not Found**
   - Error: `Healthcare center not found for NIN_HFR ID: {nin_hfr_id} in state: {state}`
   - Action: Verify NIN_HFR ID exists in facility service

3. **API Service Errors**
   - Error: HTTP status code and error message from service
   - Action: Check service availability and payload format

4. **Data Validation Errors**
   - Error: Specific validation error from service
   - Action: Verify data format and required fields

## Environment Variables

The following environment variables are used:

| Variable | Description | Default |
|----------|-------------|---------|
| `IM_SERVICES_URL` | IM services API URL | `http://localhost:8080/im-services/request/_create` |
| `FACILITY_SERVICE_URL` | Facility service URL | `http://localhost:8080/facility-service` |
| `HRMS_SERVICE_URL` | HRMS service URL | `http://localhost:8080/egov-hrms` |
| `MDMS_URL` | MDMS service URL | `http://localhost:8080/egov-mdms-v1` |

## Security Considerations

1. **Authorization**: All requests must include valid `request_info` for authorization
2. **File Validation**: Only Excel files (.xlsx) are accepted
3. **Temporary Files**: All temporary files are cleaned up after processing
4. **Error Logging**: Sensitive information is not logged in error messages

## Performance Considerations

1. **Batch Processing**: Each row is processed sequentially
2. **API Calls**: Multiple API calls per row (HRMS, Facility, IM services)
3. **Memory Usage**: Entire Excel file is loaded into memory
4. **Timeout**: Consider API timeout settings for large files

## Testing

### Sample Request

```bash
curl -X POST "http://localhost:8000/legacy_ticket_ingestion" \
  -H "Content-Type: multipart/form-data" \
  -F "legacy_ticket_file=@legacy_tickets.xlsx" \
  -F "legacy_ticket_sheet_name=Legacy Tickets" \
  -F "request_info={\"apiId\":\"org.egov.pt\",\"ver\":\"1.0\",\"ts\":1234567890,\"action\":\"POST\",\"did\":\"4354648646\",\"key\":\"xyz\",\"msgId\":\"654654\",\"requesterId\":\"61\",\"authToken\":\"null\"}"
```

### Sample Excel Structure

| State | NIN_HFR ID | code | Ticket Type | Ticket Sub Type | Is the solar system working? | Comments | Unique_ID | Actual_Reported_Date (mm/dd/yyyy) |
|-------|------------|------|-------------|-----------------|------------------------------|----------|-----------|-----------------------------------|
| pb | HC001 | user1 | Technical | Solar Panel | Yes | Working fine | LEG001 | 01/15/2023 |
| pb | HC002 | user2 | Maintenance | Battery | No | Battery dead | LEG002 | 02/20/2023 |

## Troubleshooting

### Common Issues

1. **File Upload Errors**
   - Ensure file is valid Excel format (.xlsx)
   - Check file size limits

2. **Authentication Errors**
   - Verify `request_info` contains valid authorization token
   - Check API permissions

3. **Data Mapping Errors**
   - Verify column names match expected format
   - Check data types and required fields

4. **Service Connectivity**
   - Verify all service URLs are accessible
   - Check network connectivity and firewall settings

## Dependencies

- `pandas`: Excel file processing
- `requests`: HTTP API calls
- `fastapi`: Web framework
- `openpyxl`: Excel file handling
- `uuid`: Unique ID generation
- `tempfile`: Temporary file management 