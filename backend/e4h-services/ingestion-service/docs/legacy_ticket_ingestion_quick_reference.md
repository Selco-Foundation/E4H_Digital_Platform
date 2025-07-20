# Legacy Ticket Ingestion API - Quick Reference Guide

## 🚀 Quick Start

**Endpoint:** `POST /legacy_ticket_ingestion`

**Purpose:** Ingest legacy ticket data from Excel files into the Incident Management (IM) system

## 📋 Required Excel Columns

| Column | Required | Description |
|--------|----------|-------------|
| `State` | ✅ | State/tenant ID |
| `NIN_HFR ID` | ✅ | Healthcare facility ID |
| `code` | ✅ | Employee username/code |
| `Ticket Type` | ✅ | Type of incident |
| `Ticket Sub Type` | ✅ | Subtype of incident |
| `Is the solar system working?` | ✅ | System functional status |
| `Comments` | ❌ | Additional comments |
| `Unique_ID` | ❌ | Legacy ticket ID |
| `Actual_Reported_Date (mm/dd/yyyy)` | ❌ | Date when ticket was reported |

## 🔄 Processing Flow

1. **Validate Request** → Parse `request_info` and authorize
2. **Setup Files** → Create temporary input/output files
3. **Process Each Row**:
   - ✅ Validate employee exists in HRMS
   - ✅ Validate healthcare center exists in Facility Service
   - ✅ Build incident payload
   - ✅ Call IM Services API
   - ✅ Update status in Excel
4. **Generate Output** → Return processed Excel file

## 🔧 Key Helper Functions

- `get_hrms_employee_info()` - Fetch employee from HRMS
- `get_healthcare_center_info()` - Fetch facility info
- `get_tenant_mapping()` - Get PHC subtype mapping

## 📤 Response Format

Returns Excel file with additional columns:
- `status` - 'success' or 'failed'
- `error` - Error message if failed
- `ticket_id` - New incident ID
- `healthcare_center_info` - HC lookup status
- `employee_info` - Employee lookup status

## ⚙️ Environment Variables

```bash
IM_SERVICES_URL=http://localhost:8080/im-services/request/_create
FACILITY_SERVICE_URL=http://localhost:8080/facility-service
HRMS_SERVICE_URL=http://localhost:8080/egov-hrms
MDMS_URL=http://localhost:8080/egov-mdms-v1
```

## 🧪 Sample Request

```bash
curl -X POST "http://localhost:8000/legacy_ticket_ingestion" \
  -F "legacy_ticket_file=@legacy_tickets.xlsx" \
  -F "legacy_ticket_sheet_name=Legacy Tickets" \
  -F "request_info={\"apiId\":\"org.egov.pt\",\"ver\":\"1.0\",\"ts\":1234567890,\"action\":\"POST\",\"did\":\"4354648646\",\"key\":\"xyz\",\"msgId\":\"654654\",\"requesterId\":\"61\",\"authToken\":\"null\"}"
```

## 🚨 Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `Employee not found for code: {code}` | Employee doesn't exist in HRMS | Verify employee code |
| `Healthcare center not found for NIN_HFR ID: {id}` | Facility doesn't exist | Verify NIN_HFR ID |
| HTTP 400/500 | API service error | Check service availability |

## 📊 Sample Excel Data

| State | NIN_HFR ID | code | Ticket Type | Ticket Sub Type | Is the solar system working? | Comments | Unique_ID | Actual_Reported_Date |
|-------|------------|------|-------------|-----------------|------------------------------|----------|-----------|---------------------|
| pb | HC001 | user1 | Technical | Solar Panel | Yes | Working fine | LEG001 | 01/15/2023 |
| pb | HC002 | user2 | Maintenance | Battery | No | Battery dead | LEG002 | 02/20/2023 |

## 🔍 Troubleshooting Checklist

- [ ] Excel file is valid .xlsx format
- [ ] Column names match exactly (case-sensitive)
- [ ] All required columns are present
- [ ] `request_info` contains valid auth token
- [ ] All service URLs are accessible
- [ ] Employee codes exist in HRMS
- [ ] NIN_HFR IDs exist in Facility Service

## 📚 Full Documentation

For detailed implementation guide, see: `legacy_ticket_ingestion_developer_guide.md` 