# Ingestion Service

A FastAPI-based microservice for handling data ingestion in the E4H Digital Platform. This service provides endpoints for uploading, validating, and processing various types of data through Excel files.

## Features

- **Vendor Data Ingestion**
  - Upload and process vendor data from Excel files
  - Validate vendor information and boundary codes
  - Handle multiple sheets in a single Excel file

- **Boundary Data Processing**
  - Process hierarchical boundary data (Country/State/District/Block)
  - Validate boundary codes and hierarchies
  - Ensure unique boundary combinations

- **Facility Template Management**
  - Generate and manage facility ingestion templates
  - Integration with MDMS for master data

## API Endpoints

### 1. Vendor Ingestion
```
POST /ingestion-service/ingest_vendors_excel
```
- Upload and process vendor Excel files
- Parameters:
  - `vendor_file`: Excel file containing vendor data
  - `vendor_sheet_name`: Name of vendor data sheet (default: "Vendor Input")
  - `boundary_sheet_name`: Name of boundary code sheet (default: "Boundary Code")

### 2. Facility Template
```
POST /ingestion-service/get_facility_ingestion_template
```
- Generate facility ingestion templates

### 3. Boundary Data
```
POST /ingestion-service/upload_boundaries_excel_sheet
```
- Upload and process boundary data
- Parameters:
  - `boundary_file`: Excel file containing boundary data
  - `boundary_sheet_name`: Name of boundary data sheet (default: "Boundary Data")

## Configuration

Environment variables required:
- `MDMS_URL`: URL for Master Data Management System
- `VENDOR_SERVICE_URL`: URL for Vendor Service
- `BOUNDARY_SERVICE_URL`: URL for Boundary Service

## Dependencies

- Python 3.6+
- FastAPI
- Pandas (for Excel processing)
- Python-dotenv (for environment variables)

## Data Validation

The service includes several validators:
- Boundary hierarchy validation
- Identifier validation (GSTIN, PAN)
- Data format validation
- Duplicate entry checks

## Error Handling

- Detailed error messages for validation failures
- Status tracking for each record
- Error logging with AppLogger

## Security

- RBAC (Role-Based Access Control) validation
- Request info validation
- CORS middleware enabled

## Known Issues and TODOs

1. Boundary codes need case-sensitivity handling and possible truncation rules
2. Spelling variations in hierarchy names need to be addressed
3. Identifier types should be configured in MDMS instead of hardcoding
4. Additional documentation needed for class and function purposes

## Installation

1. Clone the repository
2. Navigate to the ingestion-service directory:
```bash
cd backend/municipal-services/ingestion-service
```

3. Create a virtual environment (recommended):
```bash
python -m venv venv
source venv/bin/activate  # On Windows use: venv\Scripts\activate
```

4. Install dependencies:
```bash
pip install -r requirements.txt
```

## Running the Service

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

The service will be available at http://localhost:8000
