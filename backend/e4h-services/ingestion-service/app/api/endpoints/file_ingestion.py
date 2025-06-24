import json
import os
import tempfile
from datetime import datetime
import uuid
from typing import Optional, Dict, List

import pandas as pd
from fastapi import APIRouter, File, Form, UploadFile, HTTPException
from fastapi.responses import FileResponse
import psycopg2
from starlette.responses import JSONResponse
import requests

from app.core.logging import AppLogger
from app.decorators.rbac_validator import get_authorized_request_info
from app.ingest.excel_data_writer import ExcelDataWriter
from app.processor.factory.boundary_data_processor_factory import BoundaryDataProcessorFactory
from app.processor.factory.vendor_data_processor_factory import VendorDataProcessorFactory
from app.schemas.request_info import RequestInfo
from app.utils.convertor import request_info_from_json, create_vendor_request, create_facility_payload, \
    get_project_creation_payload, get_user_creation_payload, get_staff_creation_payload, create_project_payload, \
    get_installation_spoc_creation_payload
from app.utils.facility_service_client import FacilityServiceClient
from app.utils.mdms_client import MDMSClient
from app.utils.organization_service_client import OrganizationServiceClient
from app.utils.project_service_client import ProjectServiceClient
from app.utils.hrms_service_client import HRMSServiceClient

router = APIRouter()
logger = AppLogger().get_logger()

from dotenv import load_dotenv

load_dotenv()
mdms_url = os.getenv("MDMS_URL")
org_service_url = os.getenv("VENDOR_SERVICE_URL")
project_service_url = os.getenv("PROJECT_SERVICE_URL")
facility_service_url = os.getenv("FACILITY_SERVICE_URL")
hrms_service_url = os.getenv("HRMS_SERVICE_URL")
im_services_url = os.getenv("IM_SERVICES_URL")

DB_CONFIG = {
    "host": os.getenv("DB_HOST"),
    "port": int(os.getenv("DB_PORT", 5432)),
    "database": os.getenv("DB_NAME"),
    "user": os.getenv("DB_USER"),
    "password": os.getenv("DB_PASSWORD")
}

@router.post('/vendors',
             summary='Upload and process vendor Excel file with multiple sheets',
             response_description="Returns processed Excel file with validation results")
async def upload_vendors_excel_sheet(
        vendor_file: UploadFile = File(description="Excel file containing vendor data and boundary codes"),
        vendor_sheet_name: str = Form(default="Vendor Input", description="Name of the sheet containing vendor data"),
        boundary_sheet_name: str = Form(default="Boundary Code",
                                        description="Name of the sheet containing boundary codes"),
        request_info: str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await vendor_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        vendor_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"vendor_validation_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        processor = VendorDataProcessorFactory.create_processor(
            file_path=vendor_file_path,
            vendor_sheet=vendor_sheet_name,
            boundary_sheet=boundary_sheet_name,
            mdms_url=mdms_url,
            request_info=request_info
        )
        tuple_vendors = processor.process_data()
        vendors = tuple_vendors[0]
        vendor_df = tuple_vendors[1]

        if org_service_url and vendors:
            org_client = OrganizationServiceClient(org_service_url)

            for vendor in vendors:
                vendor_payload = create_vendor_request(request_info, vendor)

                try:
                    org_data = org_client.create_vendor(vendor_payload)
                    if org_data and org_data.get("Organisations"):
                        match_mask = (vendor_df["Vendor Code (Mandatory)"] == vendor.vendor_code)
                        if match_mask.any():
                            vendor_df.loc[match_mask, "status"] = "success"
                            vendor_df.loc[match_mask, "error"] = None
                            vendor_df.loc[match_mask, "vendor_id"] = org_data["Organisations"][0].get("id")
                    else:
                        logger.warning(f"Failed to create vendor: {vendor.vendor_name}")
                except Exception as e:
                    logger.error(f"Error creating vendor in org service: {e}")

        with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
            vendor_df.to_excel(writer, sheet_name="Vendor Output", index=False)
            boundary_df = pd.read_excel(vendor_file_path, sheet_name=boundary_sheet_name)
            boundary_df.to_excel(writer, sheet_name=boundary_sheet_name, index=False)

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Error processing vendor data: {e}")
        raise HTTPException(status_code=500, detail=f"Failed to process vendor data: {str(e)}")

    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)


@router.post('/boundaries',
             summary='Upload and process boundary Excel file',
             response_description="Returns processed Excel file with validation results")
async def upload_boundaries_excel_sheet(
        boundary_file: UploadFile = File(description="Excel file containing boundary data"),
        boundary_sheet_name: str = Form(default="Boundary Data",
                                        description="Name of the sheet containing boundary data"),
        request_info: str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await boundary_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        boundary_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"boundary_validation_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        with open(boundary_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        processor = BoundaryDataProcessorFactory.create_processor(
            file_path=output_file_path,
            boundary_sheet=boundary_sheet_name,
            mdms_url=mdms_url,
            request_info=request_info
        )
        boundary_df = processor.process_data()

        writer = ExcelDataWriter(output_file_path, output_sheet="Boundary Data")
        writer.write_data(boundary_df)

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Error processing boundary data: {e}")
        raise HTTPException(
            status_code=500,
            detail="Failed to process boundary data"
        ) from e


    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)

@router.post('/facilities',
             summary='Upload and process facility Excel file',
             response_description='Returns processed Excel file with validations results')
async def upload_facilities_excel_sheet(
        facility_file: UploadFile = File(description="Excel file containing facility data"),
        facility_sheet_name: str = Form(default="FacilityIngestionTemplate",
                                        description="Name of the sheet containing facility data"),
        request_info: str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)
    mdms_client = MDMSClient(mdms_url)

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await facility_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        facility_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_ingestion_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        with open(facility_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        df = pd.read_excel(facility_file_path, sheet_name=facility_sheet_name)

        if 'status' not in df.columns:
            df['status'] = ''
        if 'error' not in df.columns:
            df['error'] = ''

        if facility_service_url and not df.empty:
            facility_client = FacilityServiceClient(facility_service_url)
            facility_schema = mdms_client.get_column_definitions_with_metadata(request_info,'data-ingestion.FacilityIngestionSchema')
            for index, row in df[df['status'] != 'success'].iterrows():
                try:
                    facility_data_payload = create_facility_payload(request_info, row, facility_schema)
                    response = facility_client.create_facility(facility_data_payload)
                    if response.status_code in (200, 201):
                        df.at[index, 'status'] = 'success'
                        df.at[index, 'error'] = ''
                    elif response.status_code == 400:
                        error_data = response.json()
                        error_message = error_data.get('Errors', [{}])[0].get('message', 'Unknown error')
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = error_message
                    else:
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = f'{response.status_code}: {response.text}'
                except Exception as e:
                    df.at[index, 'status'] = 'failed'
                    df.at[index, 'error'] = f'Exception: {str(e)}'

        writer = ExcelDataWriter(output_file_path, output_sheet=facility_sheet_name)
        writer.write_data(df)

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    except Exception as e:
        logger.error(f"Error processing facility data: {e}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to process facility data: {str(e)}"
        )
    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)

@router.post('/workStreamWithFacilities',
             summary='Upload and process workstream with facilities excel file.',
             response_description='Returns processed Excel file with validation results')
async def upload_facilities_with_workstream(
        project_id_with_type_field_plan: str = Form(default="Project id of the project with type field plan"),
        request_info: str = Form(default=""),
        installation_spoc_user_name:str = Form(default=""),
        installation_spoc_user_mobile_number:str = Form(default=""),
        installation_spoc_user_email:str = Form(default="")
)->JSONResponse:
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        # Fetch project of type Field Plan using project_id
        if project_service_url and hrms_service_url:
            project_client = ProjectServiceClient(project_service_url)
            hrms_client = HRMSServiceClient(hrms_service_url)
            field_plan_project = project_client.search_project(request_info, project_id_with_type_field_plan)
            project = field_plan_project["Project"][0]
            if not project:
                raise Exception("Field plan id is not correct.")
            field_plan_project_facilities = project_client.search_project_facility(request_info,
                                                                                   project_id_with_type_field_plan)
            work_stream_creation_payload = get_project_creation_payload(
                request_info,
                project['name'] + "_work_stream",
                "Work Stream",
                project_id_with_type_field_plan,
                project["startDate"],
                project["endDate"],
                "Installation"
            )
            work_stream_creation_response = json.loads(project_client.create_project(work_stream_creation_payload).text)
            work_stream = work_stream_creation_response['Project'][0]

            # Link work stream project to facilities
            facilities = field_plan_project_facilities["ProjectFacilities"]
            for facility in facilities:
                project_client.create_project_facility(request_info,
                    work_stream["id"],
                    facility["facilityId"]
                )
            # Create a installation spoc user
            installation_spoc_creation_payload = get_installation_spoc_creation_payload(request_info, installation_spoc_user_name, installation_spoc_user_mobile_number,
                                                   installation_spoc_user_email)
            user_creation_response = json.loads(hrms_client.create_user(installation_spoc_creation_payload).text)
            user = user_creation_response['Employees'][0]
            staff_creation_payload = get_staff_creation_payload(request_info, user["uuid"], work_stream["id"])
            staff_creation_response = json.loads(project_client.create_project_staff(staff_creation_payload).text)
            staff = staff_creation_response['ProjectStaff']
            return JSONResponse(
                status_code=200,
                content={"staff": staff, "user": user, "work_stream": work_stream}
            )
        return JSONResponse(
            status_code=500,
            content="Connection failed with project service and hrms service."
        )

    except Exception as e:
        logger.error(f"Error processing facility data: {e}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to process facility data: {str(e)}"
        )




@router.post('/facilityWithSupervisors',
             summary='Upload and process facility with supervisors Excel file',
             response_description="Returns processed Excel file with validation results")
async def upload_facility_with_supervisors_excel_sheet(
        facility_with_supervisors: UploadFile = File(
            description="Excel file containing facility with supervisors data"),
        facility_sheet: str = Form(default="Facilities_Supervisors",
                                    description="Name of the sheet containing facility data"),
        request_info: str = Form(default=""),
        work_stream_project_id:str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        # Create input temporary file
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await facility_with_supervisors.read()
        input_temp_file.write(content)
        input_temp_file.close()
        facility_with_supervisors_file_path = input_temp_file.name

        # Create output file with timestamp
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_with_supervisor_ingestion_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        # Copy input to output file first
        with open(facility_with_supervisors_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        # Read the Excel file
        df = pd.read_excel(facility_with_supervisors_file_path, sheet_name=facility_sheet)

        # Add status and error columns if they don't exist
        if 'status' not in df.columns:
            df['status'] = ''
        if 'error' not in df.columns:
            df['error'] = ''

        # Process each row if services are available
        if project_service_url and not df.empty:
            project_client = ProjectServiceClient(project_service_url)
            hrms_client = HRMSServiceClient(hrms_service_url)
            work_stream_project = project_client.search_project(request_info, work_stream_project_id)
            work_stream = work_stream_project["Project"][0]
            for index, row in df.iterrows():
                if row.get("status", "") != "success":
                    try:
                        # Create project of type facility
                        facility_creation_payload = get_project_creation_payload(request_info, row.get('Health Centre Name (Mandatory)', ''), "Facility",
                                                                                 work_stream_project_id, work_stream["startDate"],work_stream["endDate"],"")
                        facility_creation_response = project_client.create_project(facility_creation_payload)
                        facility = json.loads(facility_creation_response.text)
                        if facility_creation_response.status_code in [200, 201, 202]:
                            df.at[index, 'status'] = 'success'
                            # Create User
                            user_creation_payload = get_user_creation_payload(request_info, row)
                            user_creation_response = hrms_client.create_user(user_creation_payload)
                            user = json.loads(user_creation_response.text)
                            if user_creation_response.status_code in [200, 201, 202]:
                                df.at[index, 'status'] = 'success'
                                # Create staff
                                staff_creation_payload = get_staff_creation_payload(request_info, user["Employees"][0]["uuid"], facility["Project"][0]["id"])
                                staff_creation_response = project_client.create_project_staff(staff_creation_payload)
                                if staff_creation_response.status_code in [200, 201, 202]:
                                    df.at[index,'status'] = 'success'
                                    df.at[index, 'error'] = ''
                                else:
                                    df.at[index, 'status'] = 'failed'
                                    df.at[index, 'error'] = f"Staff Creation Error: {staff_creation_response.status_code} - {staff_creation_response.text}"
                            else:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = f"User Creation Error: {user_creation_response.status_code} - {user['Errors']}"
                        else:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = f"Facility Creation Error: {facility_creation_response.status_code} - {facility_creation_response.text}"
                    except Exception as e:
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = f"Processing Error: {str(e)}"

        # Write data to the same sheet name that was read
        with pd.ExcelWriter(output_file_path, engine='openpyxl', mode='a') as writer:
            # Remove the existing sheet if it exists
            if facility_sheet in writer.book.sheetnames:
                idx = writer.book.sheetnames.index(facility_sheet)
                writer.book.remove(writer.book.worksheets[idx])
            # Write data to the sheet
            df.to_excel(writer, sheet_name=facility_sheet, index=False)

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    except Exception as e:
        logger.error(f"Error processing facility data: {e}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to process facility data: {str(e)}"
        )
    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)

@router.post('/projects',
             summary='Upload and process project Excel file',
             response_description='Returns processed Excel file with validations results')
async def upload_projects_excel_sheet(
        project_file: UploadFile = File(description="Excel file containing project data"),
        project_sheet_name: str = Form(default="Project Data",
                                        description="Name of the sheet containing project data"),
        request_info: str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await project_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        project_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"project_ingestion_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        with open(project_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        df = pd.read_excel(project_file_path, sheet_name=project_sheet_name)

        if 'status' not in df.columns:
            df['status'] = ''
        if 'error' not in df.columns:
            df['error'] = ''
        if 'Project ID' not in df.columns:
            df['Project ID'] = ''

        if project_service_url and not df.empty:
            project_client = ProjectServiceClient(project_service_url)
            for index, row in df[df['status'] != 'success'].iterrows():
                try:
                    project_data_payload = create_project_payload(request_info, row)
                    response = project_client.create_project(project_data_payload)
                    response_data = response.json()

                    if response.status_code in [200, 201, 202] and isinstance(response_data.get('Project'), list) and response_data[
                        'Project']:
                        project_id = response_data['Project'][0].get('id')
                        df.at[index, 'status'] = 'success'
                        df.at[index, 'error'] = ''
                        df.at[index, 'Project ID'] = project_id
                    elif response.status_code == 400:
                        error_data = response.json()
                        error_message = error_data.get('Errors', [{}])[0].get('message', 'Unknown error')
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = error_message
                    else:
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = f'{response.status_code}: {response.text}'
                except Exception as e:
                    df.at[index, 'status'] = 'failed'
                    df.at[index, 'error'] = f'Exception: {str(e)}'

        writer = ExcelDataWriter(output_file_path, output_sheet=project_sheet_name)
        writer.write_data(df)

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    except Exception as e:
        logger.error(f"Error processing project data: {e}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to process project data: {str(e)}"
        )
    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)

@router.post('/facilitySelection',
             summary='Upload and process facility selection Excel file',
             response_description='Returns processed Excel file with validations results')
async def upload_facility_selection_excel_sheet(
        facility_selection_file: UploadFile = File(description="Excel file containing facility selection data"),
        project_id: str = Form(...),
        facility_selection_sheet_name: str = Form(default="Facility Selection Template",
                                        description="Name of the sheet containing facility selection data"),
        request_info: str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await facility_selection_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        project_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"project_facility_ingestion_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        with open(project_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        df = pd.read_excel(project_file_path, sheet_name=facility_selection_sheet_name)

        if 'status' not in df.columns:
            df['status'] = ''
        if 'error' not in df.columns:
            df['error'] = ''

        if project_service_url and not df.empty:
            project_client = ProjectServiceClient(project_service_url)
            for index, row in df[(df['status'] != 'success') & (df['Selection?'] == 'Yes')].iterrows():
                try:
                    facility_id = row.get("HC ID")
                    if pd.isna(facility_id):
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = 'HC ID must not be null.'
                        continue
                    response = project_client.create_project_facility(request_info, project_id, facility_id)
                    if response.status_code in (200, 201, 202):
                        df.at[index, 'status'] = 'success'
                        df.at[index, 'error'] = ''
                    elif response.status_code == 400:
                        error_data = response.json()
                        error_message = error_data.get('Errors', [{}])[0].get('message', 'Unknown error')
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = error_message
                    else:
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = f'{response.status_code}: {response.text}'
                except Exception as e:
                    df.at[index, 'status'] = 'failed'
                    df.at[index, 'error'] = f'Exception: {str(e)}'

        writer = ExcelDataWriter(output_file_path, output_sheet=facility_selection_sheet_name)
        writer.write_data(df)

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Error processing facility selection data: {e}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to process facility selection data: {str(e)}"
        )
    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)
            

def get_hrms_employee_info(codes: List[str], db_conn) -> Dict[str, str]:
    try:
        with db_conn.cursor() as cursor:
            sql = "SELECT code, tenantid  FROM eg_hrms_employee WHERE code = ANY (%s)"
            cursor.execute(sql, (codes,))
            rows = cursor.fetchall()
            return {row[0]: row[1] for row in rows}
    except Exception as e:
        logger.error(f"Error fetching HRMS employee info: {e}")
        return {}

def get_tenant_mapping(request_info: RequestInfo, tenant_id: str) -> Dict:
    """
    Fetch tenant mapping from MDMS for PHC subtypes
    """
    print(mdms_url)
    try:
        search_url = f"{mdms_url}/egov-mdms-service/v1/_search"
        search_payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "MdmsCriteria": {
                "tenantId": tenant_id,
                "moduleDetails": [
                    {
                        "moduleName": "tenant",
                        "masterDetails": [
                            {
                                "name": "tenants"
                            }
                        ]
                    }
                ]
            }
        }
        response = requests.post(search_url, json=search_payload)
        if response.status_code == 200:
            data = response.json()
            tenants = data.get("MdmsRes", {}).get("tenant", {}).get("tenants", [])
            return {tenant.get("code"): tenant for tenant in tenants if tenant.get("code")}
        return {}
    except Exception as e:
        logger.error(f"Error fetching tenant mapping from MDMS: {e}")
        return {}

@router.post('/legacy_ticket_ingestion',
             summary='Upload and ingest legacy tickets Excel file',
             response_description="Returns processed Excel file with ingestion results")
async def upload_legacy_ticket_excel_sheet(
        legacy_ticket_file: UploadFile = File(description="Excel file containing Legacy Tickets"),
        legacy_ticket_sheet_name: str = Form(default="Legacy Tickets", description="Name of the sheet containing Legacy Tickets"),
        request_info: str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info_obj = request_info_from_json(request_info)
    get_authorized_request_info(request_info_obj)

    # Generate a unique migrationId for this batch
    migration_id = str(uuid.uuid4())

    # Fetch tenant mapping once for the entire batch
    tenant_mapping = get_tenant_mapping(request_info_obj, "pg")

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await legacy_ticket_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        excel_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"legacy_ticket_ingestion_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        df = pd.read_excel(excel_file_path, sheet_name=legacy_ticket_sheet_name)
        df.columns = df.columns.str.strip()
        df = df.reindex(columns=df.columns.tolist() + ['status', 'error', 'ticket_id', 'employee_info'], fill_value='')


        codes = []

        for index, row in df.iterrows():
            code = str(row.get("NIN_HFR ID", "")).strip()
            if code:
                codes.append(code)

        conn = psycopg2.connect(**DB_CONFIG)

        employee_info = get_hrms_employee_info(codes, conn)


        for idx, row in df.iterrows():
            try:
                if df.at[idx, 'status'] in ['duplicate', 'error']:
                    continue

                employee_code = str(row.get("NIN_HFR ID", "")).strip()
                tenant_id = employee_info.get(employee_code)
                if not tenant_id:
                    df.at[idx, 'status'] = 'failed'
                    df.at[idx, 'error'] = f'Employee not found for code: {employee_code}'
                    df.at[idx, 'employee_info'] = 'Not found'
                    continue

                df.at[idx, 'employee_info'] = 'Found'

                employee_tenant_mapping = tenant_mapping.get(tenant_id,{})
                if not employee_tenant_mapping:
                    dynamic_mapping = get_tenant_mapping(request_info_obj, tenant_id)
                    if dynamic_mapping:
                        tenant_mapping.update(dynamic_mapping)
                        employee_tenant_mapping = tenant_mapping.get(tenant_id, {})
                    else :
                        df.at[idx, 'status'] = 'failed'
                        df.at[idx, 'error'] = f'Tenant mapping not found for tenant ID: {tenant_id}'
                        continue

                # Extract tenant-based fields
                phc_subtype = employee_tenant_mapping.get("centreType", "")
                block = employee_tenant_mapping.get("city", {}).get("districtName", "")
                tenant_id = employee_tenant_mapping.get("code", "")
                district = employee_tenant_mapping.get("city", {}).get("districtCode", "")

                incident_payload = {
                    "incidentType": str(row.get("Ticket Type", "")).strip(),
                    "incidentSubtype": str(row.get("Ticket Sub Type", "")).strip(),
                    "comments": str(row.get("Comments", "")).strip(),
                    "tenantId": tenant_id,
                    "migrationId": migration_id,
                    "district": district,
                    "block": block,
                    "phcType": tenant_id,
                    "phcSubType": phc_subtype,
                    "additionalDetail": {
                        "fileStoreId": [],
                        "reopenreason": [],
                        "rejectReason": [],
                        "sendBackReason": [],
                        "sendBackSubReason": []
                    },
                    "source": "web",
                    "reporter": {
                        "uuid": "cd831d19-3799-4e73-a52a-237930f1e450",
                        "tenantId": "pg"
                    }
                }

                # Optional: Set legacyId if present
                unique_id = row.get("Unique_ID", None)
                if pd.notnull(unique_id):
                    incident_payload["legacyId"] = str(unique_id).strip()

                # Optional: Convert Actual_Reported_Date to epoch
                reported_date = row.get("Actual_Reported_Date", None)

                if pd.notnull(reported_date):
                        if isinstance(reported_date, str):
                            dt = pd.to_datetime(reported_date, format="%d/%m/%Y", errors='coerce')
                        else:
                            dt = pd.to_datetime(reported_date, errors='coerce')
                        if pd.notnull(dt):
                            incident_payload["filedDate"] = int(dt.timestamp() * 1000)

                request_info = {
                    "apiId": "Rainmaker",
                    "authToken": "79967889-fbf5-42c6-9bd3-4adc0dbe7692",
                    "userInfo": {
                        "id": 95,
                        "uuid": "cd831d19-3799-4e73-a52a-237930f1e450",
                        "userName": employee_code,
                        "name": "Akhila",
                        "mobileNumber": "9901224633",
                        "emailId": None,
                        "locale": None,
                        "type": "EMPLOYEE",
                        "roles": [
                            {
                                "name": "Complainant",
                                "code": "COMPLAINANT",
                                "tenantId": "pg"
                            },
                            {
                                "name": "Employee",
                                "code": "EMPLOYEE",
                                "tenantId": "pg"
                            },
                            {
                                "name": "Complaint Assessor",
                                "code": "COMPLAINT_ASSESSOR",
                                "tenantId": "pg"
                            },
                            {
                                "name": "Super User",
                                "code": "SUPERUSER",
                                "tenantId": "pg"
                            }
                        ],
                        "active": True,
                        "tenantId": "pg",
                        "permanentCity": None
                    },
                    "msgId": "1744021633700|en_IN",
                    "plainAccessRequest": {}
                }

                payload = {
                    "RequestInfo": request_info,
                    "incident": incident_payload,
                    "workflow": {
                        "action": "APPLY",
                        "verificationDocuments": []
                    }
                }

                # Call the im-services create API
                create_incident_url = f"{im_services_url}/im-services/v2/request/_create"
                headers = {"Content-Type": "application/json"}
                response = requests.post(create_incident_url, json=payload, headers=headers)
                if response.status_code in (200, 201):
                    resp_json = response.json()
                    # Try to extract ticket/incident id from response
                    incident = resp_json.get("IncidentWrappers", {})[0].get("incident")
                    incident_id = incident.get("incidentId")
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
            except Exception as e:
                df.at[idx, 'status'] = 'failed'
                df.at[idx, 'error'] = str(e)

        # Write results to output Excel
        with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
            df.to_excel(writer, sheet_name=legacy_ticket_sheet_name, index=False)

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    except Exception as e:
        logger.error(f"Error processing legacy ticket data: {e}")
        raise HTTPException(status_code=500, detail=f"Failed to process legacy ticket data: {str(e)}")
    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)


@router.post("/check_duplicates")
async def check_duplicate_tickets(
        legacy_ticket_file: UploadFile = File(...),
        legacy_ticket_sheet_name: str = Form(default="Duplication Template"),
):
    input_temp_file = None
    try:
        # Save uploaded file temporarily
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await legacy_ticket_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        excel_path = input_temp_file.name

        # Read Excel file
        df = pd.read_excel(excel_path, sheet_name=legacy_ticket_sheet_name)
        df.columns = df.columns.str.strip()
        df = df.reindex(columns=df.columns.tolist() + ['status', 'error'], fill_value='')

        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()

        # Step 1: Fetch NIN_HFR ID -> tenantId mapping
        nin_hfr_ids = df["NIN_HFR ID"].dropna().astype(str).str.strip().unique().tolist()
        cursor.execute("SELECT code, tenantid FROM eg_hrms_employee WHERE code IN %s", (tuple(nin_hfr_ids),))
        code_tenant_map = dict(cursor.fetchall())

        # Step 2: Check each row for incident duplication
        for idx, row in df.iterrows():
            code = str(row.get("NIN_HFR ID", "")).strip()
            ticket_type = str(row.get("Ticket Type", "")).strip()
            ticket_subtype = str(row.get("Ticket Sub Type", "")).strip()

            tenant_id = code_tenant_map.get(code)
            if not tenant_id:
                df.at[idx, 'status'] = 'error'
                df.at[idx, 'error'] = 'Invalid NIN_HFR ID (not in eg_hrms_employee)'
                continue

            # Step 3: Check for matching incidents
            cursor.execute("""
                SELECT 1 FROM eg_incident_v2 
                WHERE tenantid = %s 
                AND incidenttype = %s 
                AND incidentsubtype = %s 
                AND applicationstatus NOT IN ('CLOSEDAFTERRESOLUTION', 'RESOLVED', 'REJECTED')
                LIMIT 1
            """, (tenant_id, ticket_type, ticket_subtype))
            exists = cursor.fetchone()

            if exists:
                df.at[idx, 'status'] = 'duplicate'

        conn.close()

        # Save updated Excel
        df.to_excel(excel_path, index=False, sheet_name=legacy_ticket_sheet_name)

        return FileResponse(
            path=excel_path,
            filename=legacy_ticket_file.filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error during duplicate check: {str(e)}")

    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            pass