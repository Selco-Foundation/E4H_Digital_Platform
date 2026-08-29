# Legacy Ticket Ingestion - Step-by-Step Implementation Guide

## Overview

This document provides a detailed walkthrough of the `upload_legacy_ticket_excel_sheet` function implementation, explaining each step and the reasoning behind it.

## Function Signature

```python
@router.post('/legacy_ticket_ingestion',
             summary='Upload and ingest legacy tickets Excel file',
             response_description="Returns processed Excel file with ingestion results")
async def upload_legacy_ticket_excel_sheet(
        legacy_ticket_file: UploadFile = File(description="Excel file containing Legacy Tickets"),
        legacy_ticket_sheet_name: str = Form(default="Legacy Tickets", description="Name of the sheet containing Legacy Tickets"),
        request_info: str = Form(default=""),
        im_services_url: str = Form(default=os.getenv("IM_SERVICES_URL", "http://localhost:8080/im-services/request/_create")),
        facility_service_url: str = Form(default=os.getenv("FACILITY_SERVICE_URL", "http://localhost:8080/facility-service")),
        hrms_service_url: str = Form(default=os.getenv("HRMS_SERVICE_URL", "http://localhost:8080/egov-hrms")),
        mdms_url: str = Form(default=os.getenv("MDMS_URL", "http://localhost:8080/egov-mdms-v1"))
):
```

## Step 1: Initial Setup and Validation

```python
# Initialize variables for cleanup
input_temp_file = None
output_temp_file = None

# Parse request info from JSON string
request_info_obj = request_info_from_json(request_info)

# Validate authorization
get_authorized_request_info(request_info_obj)

# Generate unique migration ID for batch tracking
migration_id = str(uuid.uuid4())

# Fetch tenant mapping once for the entire batch
tenant_mapping = get_tenant_mapping(request_info_obj, mdms_url)
```

**Purpose:**
- Parse and validate the request information for authorization
- Generate a unique migration ID to track this batch of legacy tickets
- Fetch tenant mapping from MDMS to map PHC subtypes

## Step 2: File Processing Setup

```python
# Create temporary input file
input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
content = await legacy_ticket_file.read()
input_temp_file.write(content)
input_temp_file.close()
excel_file_path = input_temp_file.name

# Create output file with timestamp
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
output_filename = f"legacy_ticket_ingestion_results_{timestamp}.xlsx"
output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
output_temp_file.close()
output_file_path = output_temp_file.name
```

**Purpose:**
- Save uploaded file to temporary location
- Create output file with timestamp for unique naming
- Prepare file paths for processing

## Step 3: Excel File Reading and Column Setup

```python
# Read Excel file into pandas DataFrame
df = pd.read_excel(excel_file_path, sheet_name=legacy_ticket_sheet_name)

# Add status tracking columns if they don't exist
if 'status' not in df.columns:
    df['status'] = ''
if 'error' not in df.columns:
    df['error'] = ''
if 'ticket_id' not in df.columns:
    df['ticket_id'] = ''
if 'healthcare_center_info' not in df.columns:
    df['healthcare_center_info'] = ''
if 'employee_info' not in df.columns:
    df['employee_info'] = ''
```

**Purpose:**
- Load Excel data into pandas DataFrame for processing
- Add tracking columns to monitor processing status and results

## Step 4: Main Processing Loop

The core processing happens in a loop for each row:

### Step 4.1: Extract Row Data

```python
for idx, row in df.iterrows():
    try:
        # Extract and clean data from row
        state = str(row.get("State", "")).strip()
        nin_hfr_id = str(row.get("NIN_HFR ID", "")).strip()
        code = str(row.get("code", "")).strip()  # username/code from Excel
```

### Step 4.2: Employee Validation

```python
# Fetch employee information from HRMS
employee_info = get_hrms_employee_info(code, request_info_obj, hrms_service_url)
if not employee_info:
    df.at[idx, 'status'] = 'failed'
    df.at[idx, 'error'] = f'Employee not found for code: {code}'
    df.at[idx, 'employee_info'] = 'Not found'
    continue

df.at[idx, 'employee_info'] = 'Found'
```

**Purpose:**
- Validate that the employee code exists in the HRMS system
- If not found, mark row as failed and continue to next row

### Step 4.3: Healthcare Center Validation

```python
# Fetch healthcare center information
hc_info = get_healthcare_center_info(nin_hfr_id, state, request_info_obj, facility_service_url)
if not hc_info:
    df.at[idx, 'status'] = 'failed'
    df.at[idx, 'error'] = f'Healthcare center not found for NIN_HFR ID: {nin_hfr_id} in state: {state}'
    df.at[idx, 'healthcare_center_info'] = 'Not found'
    continue

df.at[idx, 'healthcare_center_info'] = 'Found'
```

**Purpose:**
- Validate that the healthcare facility exists in the facility service
- If not found, mark row as failed and continue to next row

### Step 4.4: PHC Subtype Mapping

```python
# Get PHC subtype mapping from tenant mapping
phc_subtype = tenant_mapping.get(nin_hfr_id, {}).get("subtype")
```

**Purpose:**
- Map the NIN_HFR ID to its corresponding PHC subtype using the tenant mapping

### Step 4.5: Build Incident Payload

```python
# Map Excel columns to API fields
incident_payload = {
    "incidentType": str(row.get("Ticket Type", "")).strip(),
    "incidentSubType": str(row.get("Ticket Sub Type", "")).strip(),
    "systemFunctional": str(row.get("Is the solar system working?", "")).strip(),
    "comments": str(row.get("Comments", "")).strip(),
    "tenantId": state,
    "migrationId": migration_id,
    # Add healthcare center information
    "district": hc_info.get("district", ""),
    "block": hc_info.get("block", ""),
    "phcType": hc_info.get("facilityType", ""),
    "phcSubType": phc_subtype or nin_hfr_id,
    # Add employee information
    "reporter": {
        "uuid": employee_info.get("uuid"),
        "name": employee_info.get("name"),
        "mobileNumber": employee_info.get("mobileNumber")
    }
}
```

**Purpose:**
- Map Excel data to the incident payload structure expected by the IM services API
- Include healthcare center and employee information

### Step 4.6: Handle Optional Fields

```python
# Set Unique_ID as legacyId
unique_id = row.get("Unique_ID", None)
if pd.notnull(unique_id):
    incident_payload["legacyId"] = str(unique_id).strip()

# Handle Actual_Reported_Date (convert to epoch if present)
reported_date = row.get("Actual_Reported_Date (mm/dd/yyyy)", None)
if pd.notnull(reported_date):
    if isinstance(reported_date, str):
        try:
            dt = datetime.strptime(reported_date, "%m/%d/%Y")
        except Exception:
            dt = pd.to_datetime(reported_date, errors='coerce')
    else:
        dt = pd.to_datetime(reported_date, errors='coerce')
    if pd.notnull(dt):
        incident_payload["filedDate"] = int(dt.timestamp() * 1000)
```

**Purpose:**
- Handle optional legacy ID for traceability
- Convert reported date to epoch timestamp format required by the API

### Step 4.7: API Call to IM Services

```python
# Prepare final payload with RequestInfo
payload = {
    "RequestInfo": request_info_obj,
    "incident": incident_payload
}

# Call the im-services create API
response = requests.post(im_services_url, json=payload)
```

### Step 4.8: Handle API Response

```python
if response.status_code in (200, 201):
    resp_json = response.json()
    # Try to extract ticket/incident id from response
    incident_id = resp_json.get("incident", {}).get("incidentId") or resp_json.get("incidentId")
    df.at[idx, 'status'] = 'success'
    df.at[idx, 'error'] = ''
    df.at[idx, 'ticket_id'] = incident_id or ''
else:
    df.at[idx, 'status'] = 'failed'
    try:
        error_msg = response.json().get('Errors', [{}])[0].get('message', response.text)
    except Exception:
        error_msg = response.text
    df.at[idx, 'error'] = error_msg
```

**Purpose:**
- Handle successful API responses by extracting the incident ID
- Handle failed responses by extracting error messages
- Update the DataFrame with processing results

### Step 4.9: Exception Handling

```python
except Exception as e:
    df.at[idx, 'status'] = 'failed'
    df.at[idx, 'error'] = str(e)
```

**Purpose:**
- Catch any unexpected errors during processing
- Mark row as failed with error details

## Step 5: Output Generation

```python
# Write results to output Excel
with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
    df.to_excel(writer, sheet_name=legacy_ticket_sheet_name, index=False)

# Return processed file
return FileResponse(
    path=output_file_path,
    filename=output_filename,
    media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
)
```

**Purpose:**
- Write the processed DataFrame back to Excel format
- Return the file as a downloadable response

## Step 6: Cleanup and Error Handling

```python
except Exception as e:
    logger.error(f"Error processing legacy ticket data: {e}")
    raise HTTPException(status_code=500, detail=f"Failed to process legacy ticket data: {str(e)}")
finally:
    if input_temp_file and os.path.exists(input_temp_file.name):
        os.unlink(input_temp_file.name)
```

**Purpose:**
- Handle any errors during the overall process
- Ensure temporary files are cleaned up regardless of success or failure

## Key Design Decisions

1. **Batch Processing**: Each row is processed sequentially to avoid overwhelming the APIs
2. **Validation First**: Employee and healthcare center validation happens before API calls
3. **Error Tracking**: Each row's status and errors are tracked in the output file
4. **Temporary Files**: Input and output files are created as temporary files for security
5. **Migration ID**: Unique migration ID helps track and group related legacy tickets
6. **Date Conversion**: Reported dates are converted to epoch timestamps for API compatibility
7. **Graceful Degradation**: If one row fails, processing continues with other rows

## Performance Considerations

- **Sequential Processing**: Rows are processed one by one to avoid API rate limits
- **Memory Usage**: Entire Excel file is loaded into memory
- **API Calls**: Multiple API calls per row (HRMS, Facility, IM services)
- **Timeout Handling**: Consider API timeout settings for large files

## Security Considerations

- **File Validation**: Only Excel files are accepted
- **Temporary Files**: All temporary files are cleaned up after processing
- **Authorization**: All API calls include proper authorization headers
- **Error Logging**: Sensitive information is not logged in error messages 

## Will be adding an additional step for mapping ticket and ticket sub types...In progress