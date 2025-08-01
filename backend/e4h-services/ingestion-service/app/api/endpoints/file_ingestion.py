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
from app.producer.producer import Producer
from app.utils.convertor import request_info_from_json, create_vendor_request, create_facility_payload, \
    get_project_creation_payload, get_user_creation_payload_staff, get_user_creation_payload_supervisors, get_staff_creation_payload, create_project_payload, \
    get_installation_spoc_creation_payload, get_staff_search_payload, check_role_mismatch_for_user_type
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
ENVIRONMENT = os.getenv("ENVIRONMENT", "uat").lower()
base_path = os.path.dirname(os.path.abspath(__file__))
config_path = os.path.abspath(os.path.join(base_path, "..", "..", "config"))

with open(os.path.join(config_path, "tenant_creator_mapping.json"), 'r') as f:
    TENANT_CREATOR_MAPPING = json.load(f).get(ENVIRONMENT, {})

with open(os.path.join(config_path, "user_profiles.json"), 'r') as f:
    USER_PROFILE = json.load(f).get(ENVIRONMENT, {})

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

            for index, vendor in enumerate(vendors):
                vendor_payload = create_vendor_request(request_info, vendor)

                try:
                    org_data = org_client.create_vendor(vendor_payload)
                    if org_data and org_data.get("organisations"):
                        vendor_df.at[index, "status"] = "success"
                        vendor_df.at[index, "error"] = None
                        vendor_df.at[index, "vendor_id"] = org_data["organisations"][0].get("id")
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


@router.post('/facilityWithStaff',
             summary='Upload and process facility with staff Excel file',
             response_description="Returns processed Excel file with validation results")
async def upload_facility_with_staff_excel_sheet(
        facility_with_staff: UploadFile = File(
            description="Excel file containing facility with staff data"),
        facility_sheet: str = Form(default="Facilities_Staff",
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
        content = await facility_with_staff.read()
        input_temp_file.write(content)
        input_temp_file.close()
        facility_with_staff_file_path = input_temp_file.name

        # Create output file with timestamp
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_with_staff_ingestion_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        # Copy input to output file first
        with open(facility_with_staff_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        # Read the Excel file
        df = pd.read_excel(facility_with_staff_file_path, sheet_name=facility_sheet)

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
                            # Check if user already exists and validate roles
                            user_search_payload = get_user_creation_payload_staff(request_info, row)
                            existing_user_response = hrms_client.search_user(user_search_payload)
                            existing_user = None
                            if existing_user_response.status_code == 200:
                                response_data = existing_user_response.json()
                                employees = response_data.get("Employees", [])
                                if employees:
                                    existing_user = employees[0]
                            
                            if existing_user:
                                # Check for role mismatch
                                role_check = check_role_mismatch_for_user_type(existing_user, "staff")
                                if role_check["has_mismatch"]:
                                    df.at[index, 'status'] = 'error'
                                    df.at[index, 'error'] = f"Role mismatch detected: {role_check['mismatch_details']}. Current roles: {', '.join(role_check['current_roles'])}. Expected roles: {', '.join(role_check['expected_roles'])}"
                                    continue
                                else:
                                    # Use existing user
                                    user_uuid = existing_user.get("uuid")
                                    df.at[index, 'status'] = 'success'
                            else:
                                # Create new user
                                user_creation_payload = get_user_creation_payload_staff(request_info, row)
                                user_creation_response = hrms_client.create_user(user_creation_payload)
                                user = json.loads(user_creation_response.text)
                                if user_creation_response.status_code in [200, 201, 202]:
                                    user_uuid = user["Employees"][0]["uuid"]
                                    df.at[index, 'status'] = 'success'
                                else:
                                    df.at[index, 'status'] = 'failed'
                                    df.at[index, 'error'] = f"User Creation Error: {user_creation_response.status_code} - {user.get('Errors', [{}])[0].get('message', 'Unknown error')}"
                                    continue
                            
                            # Validate user_uuid before staff creation
                            if not user_uuid:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = "User UUID is required for staff creation but was not obtained"
                                continue
                            
                            # Create staff
                            staff_creation_payload = get_staff_creation_payload(request_info, user_uuid, facility["Project"][0]["id"])
                            staff_creation_response = project_client.create_project_staff(staff_creation_payload)
                            if staff_creation_response.status_code in [200, 201, 202]:
                                df.at[index,'status'] = 'success'
                                df.at[index, 'error'] = ''
                            else:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = f"Staff Creation Error: {staff_creation_response.status_code} - {staff_creation_response.text}"
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
                        existing_facility = project_client.search_project_facility(request_info, work_stream_project_id)
                        facility_list = existing_facility.get('ProjectFacilities', [])

                        facility_created = False
                        if facility_list:
                            facility = facility_list[0]
                            facility_created = False  # existing, not newly created
                        else:
                            # Create facility if not found
                            facility_creation_payload = get_project_creation_payload(request_info, row.get('Health Centre Name (Mandatory)', ''), "Facility",
                                                                                 work_stream_project_id, work_stream["startDate"],work_stream["endDate"],"")
                            facility_creation_response = project_client.create_project(facility_creation_payload)
                            if facility_creation_response.status_code not in [200, 201, 202]:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = (
                                    f"Facility Creation Error: {facility_creation_response.status_code} - {facility_creation_response.text}"
                                )
                                continue

                            facility = json.loads(facility_creation_response.text)
                            facility_created = True

                        # 🧠 Correctly extract project ID based on the structure
                        if facility_created:
                            project_id = facility["Project"][0]["id"]
                        else:
                            project_id = facility["projectId"]
                        # Check if user already exists and validate roles
                        user_search_payload = get_user_creation_payload_supervisors(request_info, row)
                        existing_user_response = hrms_client.search_user(user_search_payload)
                        existing_user = None
                        if existing_user_response.status_code == 200:
                            response_data = existing_user_response.json()
                            employees = response_data.get("Employees", [])
                            if employees:
                                existing_user = employees[0]
                        
                        if existing_user:
                            # Check for role mismatch
                            role_check = check_role_mismatch_for_user_type(existing_user, "supervisor")
                            if role_check["has_mismatch"]:
                                df.at[index, 'status'] = 'error'
                                df.at[index, 'error'] = f"Role mismatch detected: {role_check['mismatch_details']}. Current roles: {', '.join(role_check['current_roles'])}. Expected roles: {', '.join(role_check['expected_roles'])}"
                                continue
                            else:
                                # Use existing user
                                user_uuid = existing_user.get("uuid")
                                df.at[index, 'status'] = 'success'
                        else:
                            # Create new user
                            user_creation_payload = get_user_creation_payload_supervisors(request_info, row)
                            user_creation_response = hrms_client.create_user(user_creation_payload)
                            user = json.loads(user_creation_response.text)
                            if user_creation_response.status_code in [200, 201, 202]:
                                user_uuid = user["Employees"][0]["uuid"]
                                df.at[index, 'status'] = 'success'
                            else:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = f"User Creation Error: {user_creation_response.status_code} - {user.get('Errors', [{}])[0].get('message', 'Unknown error')}"
                                continue
                        
                        # Create staff
                        staff_creation_payload = get_staff_creation_payload(request_info, user_uuid, project_id)
                        staff_creation_response = project_client.create_project_staff(staff_creation_payload)
                        if staff_creation_response.status_code in [200, 201, 202]:
                            df.at[index,'status'] = 'success'
                            df.at[index, 'error'] = ''
                        else:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = f"Staff Creation Error: {staff_creation_response.status_code} - {staff_creation_response.text}"
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

@router.post('/facilityWithSupervisorUpdateWorkflowState',
             summary='Upload and process facility with supervisors Excel file',
             response_description="Returns processed Excel file with validation results")
async def upload_facility_with_supervisors_workflow_state_excel_sheet(
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
                        existing_facility = project_client.search_project_facility(request_info, work_stream_project_id)
                        facility_list = existing_facility.get('ProjectFacilities', [])

                        facility_created = False
                        if facility_list:
                            facility = facility_list[0]
                            facility_created = False  # existing, not newly created
                        else:
                            # Create facility if not found
                            facility_creation_payload = get_project_creation_payload(request_info, row.get('Health Centre Name (Mandatory)', ''), "Facility",
                                                                                 work_stream_project_id, work_stream["startDate"],work_stream["endDate"],"")
                            facility_creation_response = project_client.create_project(facility_creation_payload)
                            if facility_creation_response.status_code not in [200, 201, 202]:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = (
                                    f"Facility Creation Error: {facility_creation_response.status_code} - {facility_creation_response.text}"
                                )
                                continue

                            facility = json.loads(facility_creation_response.text)
                            facility_created = True

                        # 🧠 Correctly extract project ID based on the structure
                        if facility_created:
                            project_id = facility["Project"][0]["id"]
                        else:
                            project_id = facility["projectId"]
                        # Check if user already exists and validate roles
                        # Determine user type based on Role column
                        user_type = "supervisor"  # default
                        if 'Role' in df.columns:
                            role_value = df.at[index, 'Role']
                            if role_value and str(role_value).strip().lower() == 'supervisor':
                                user_type = "supervisor"
                            else:
                                user_type = "staff"
                        
                        # Create search payload based on user type
                        if user_type == "supervisor":
                            user_search_payload = get_user_creation_payload_supervisors(request_info, row)
                        else:
                            user_search_payload = get_user_creation_payload_staff(request_info, row)
                        
                        existing_user_response = hrms_client.search_user(user_search_payload)
                        existing_user = None
                        if existing_user_response.status_code == 200:
                            response_data = existing_user_response.json()
                            employees = response_data.get("Employees", [])
                            if employees:
                                existing_user = employees[0]
                        
                        if existing_user:
                            # Check for role mismatch
                            role_check = check_role_mismatch_for_user_type(existing_user, user_type)
                            if role_check["has_mismatch"]:
                                df.at[index, 'status'] = 'error'
                                df.at[index, 'error'] = f"Role mismatch detected: {role_check['mismatch_details']}. Current roles: {', '.join(role_check['current_roles'])}. Expected roles: {', '.join(role_check['expected_roles'])}"
                                continue
                            else:
                                # Use existing user
                                user_uuid = existing_user.get("uuid")
                        else:
                            # Create new user based on role type
                            if user_type == "supervisor":
                                user_creation_payload = get_user_creation_payload_supervisors(request_info, row)
                            else:
                                user_creation_payload = get_user_creation_payload_staff(request_info, row)
                            
                            user_creation_response = hrms_client.create_user(user_creation_payload)
                            user = json.loads(user_creation_response.text)
                            if user_creation_response.status_code in [200, 201, 202]:
                                user_uuid = user["Employees"][0]["uuid"]
                            else:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = f"User Creation Error: {user_creation_response.status_code} - {user.get('Errors', [{}])[0].get('message', 'Unknown error')}"
                                continue
                        
                        # Validate user_uuid before staff creation
                        if not user_uuid:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = "User UUID is required for staff creation but was not obtained"
                            continue
                        
                        # Create staff
                        staff_creation_payload = get_staff_creation_payload(request_info, user_uuid, project_id)
                        staff_creation_response = project_client.create_project_staff(staff_creation_payload)
                        if staff_creation_response.status_code in [200, 201, 202]:
                            # Validate Role column exists
                            if 'Role' not in df.columns:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = "Role column is required for workflow state updates"
                                continue
                            # update workflow state
                            role_value = df.at[index,'Role']
                            if role_value and str(role_value).strip().lower() == 'supervisor':
                                update_workflow_state_response = project_client.update_workflow(request_info, work_stream_project_id, 'ASSIGN_FIELD_SUPERVISOR')
                            else:
                                update_workflow_state_response = project_client.update_workflow(request_info, work_stream_project_id,
                                                                                                'ASSIGN_FIELD_STAFF')
                            if update_workflow_state_response.status_code in [200, 201, 202]:
                                df.at[index,'status'] = 'success'
                                df.at[index, 'error'] = ''
                            else:
                                df.at[index, 'status'] = 'failed'
                                df.at[
                                    index, 'error'] = f"Update Workflow state Error: {update_workflow_state_response.status_code} - {update_workflow_state_response.text}"
                        else:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = f"Staff Creation Error: {staff_creation_response.status_code} - {staff_creation_response.text}"
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

        if project_service_url and hrms_service_url and not df.empty:
            hrms_client = HRMSServiceClient(hrms_service_url)
            project_client = ProjectServiceClient(project_service_url)
            for index, row in df[df['status'] != 'success'].iterrows():
                try:

                    project_data_payload = create_project_payload(request_info, row)
                    response = project_client.create_project(project_data_payload)
                    response_data = response.json()

                    if df.at[index, 'Project Type'] == 'Field Plan':
                        name = df.at[index, 'Name']
                        mobile_number_raw = df.at[index, 'Mobile Number']
                        email_value = df.at[index, 'Email']
                        if pd.isna(email_value) or not email_value:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = 'Email is required for Field Plan projects'
                            continue
                        if pd.isna(mobile_number_raw) or not mobile_number_raw:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = 'Mobile Number is required for Field Plan projects'
                            continue


                        mobile_number = str(int(mobile_number_raw))
                        email = str(email_value).strip()

                        spoc_payload = get_installation_spoc_creation_payload(request_info, name, mobile_number, email)

                        user_response = hrms_client.search_user(spoc_payload)

                        if user_response.status_code not in [200, 201, 202]:
                            df.at[index, 'status'] = 'failed'
                            df.at[
                                index, 'error'] = f"User search failed with status: {user_response.status_code} - {user_response.text}"
                            continue

                        response_body = json.loads(user_response.text)
                        employee_list = response_body.get("Employees", [])

                        # Filter for matching email
                        matched_user = None
                        for emp in employee_list:
                            if emp["user"]["emailId"].strip() == email:
                                matched_user = emp["user"]
                                break

                        if not matched_user:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = f"No matching user found for email: {email}"
                            continue

                    if response.status_code in [200, 201, 202] and isinstance(response_data.get('Project'), list) and response_data[
                        'Project']:
                        if df.at[index, 'Project Type'] == 'Field Plan':

                            user_uuid = matched_user.get("uuid")
                            project_id = response_data['Project'][0].get('id')

                            staff_payload = get_staff_creation_payload(request_info, user_uuid, project_id)
                            staff_response = project_client.create_project_staff(staff_payload)

                            if staff_response.status_code in [200, 201, 202]:

                                staff_search_payload = get_staff_search_payload(request_info, user_uuid)
                                staff_search_response = project_client.search_project_staff_by_id(staff_search_payload)
                                if staff_search_response.status_code in [200, 201]:
                                    print(staff_search_response.text)

                                    staff_list = staff_search_response.json().get("ProjectStaff", [])
                                    print(len(staff_list))

                                    if len(staff_list) == 1:
                                        sms_request = {
                                            "mobileNumber": mobile_number,
                                            "message": "Yor are assigned to the field plan",
                                            "expiryTime": None
                                        }
                                        producer = Producer()
                                        producer.send("egov.core.notification.sms", sms_request)
                                        producer.close()


                                df.at[index, 'status'] = 'success'
                                df.at[index, 'error'] = ''
                                df.at[index, 'Project ID'] = project_id
                            else:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = (
                                    f"Staff Creation Error: {staff_response.status_code} - {staff_response.text}")
                        else:
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

def get_tenant_mapping(request_info: RequestInfo, tenant_ids: List[str]) -> Dict:
    """
    Fetch tenant mapping from MDMS for PHC subtypes
    """
    all_tenant_data = {}

    for tenant_id in tenant_ids:
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
                all_tenant_data.update({t["code"]: t for t in tenants if t.get("code") and t["code"] not in all_tenant_data})
        except Exception as e:
            logger.error(f"Error fetching tenant mapping from MDMS: {e}")

    return all_tenant_data


def get_block_mapping_from_mdms(request_info: RequestInfo, tenant_ids: List[str]) -> Dict[str, dict]:
    """
    Fetch block mapping from MDMS where moduleName is 'Incident' and masterDetails name is 'Block'.
    Returns a dictionary with 'code' from each 'data' object as the key.
    """
    block_mapping = {}

    for tenant_id in tenant_ids:
        try:
            search_url = f"{mdms_url}/egov-mdms-service/v1/_search"
            search_payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "MdmsCriteria": {
                    "tenantId": tenant_id,
                    "moduleDetails": [
                        {
                            "moduleName": "Incident",
                            "masterDetails": [
                                {
                                    "name": "Block"
                                }
                            ]
                        }
                    ]
                }
            }

            response = requests.post(search_url, json=search_payload)
            if response.status_code == 200:
                data = response.json()
                mdms_blocks = data.get("MdmsRes", {}).get("Incident", {}).get("Block", [])

                for block in mdms_blocks:
                    code = block.get("code")
                    if code and code not in block_mapping:
                        block_mapping[code] = block

        except Exception as e:
            logger.error(f"Error fetching block mapping from MDMS for tenant {tenant_id}: {e}")

    return block_mapping

def create_mapping_dicts(mapping_file: UploadFile, sheet_name: str):
    with tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx") as temp_file:
        temp_file.write(mapping_file.file.read())
        temp_file_path = temp_file.name

    mapping_df = pd.read_excel(temp_file_path, sheet_name=sheet_name)
    mapping_df.columns = mapping_df.columns.str.strip()
    mapping_df = mapping_df.astype(str).apply(lambda x: x.str.strip())

    os.unlink(temp_file_path)

    subtype_mapping = {
        (row['Existing Issue Type'], row['Existing Ticket Sub Type ( Saure eMitra)']):
        (row['New Issue Type'], row['New Ticket Sub type'])
        for _, row in mapping_df.iterrows()
    }

    return subtype_mapping

def get_user_info_for_mizoram(usernames: List[str], db_conn) -> Dict[str, str]:
    try:
        with db_conn.cursor() as cursor:
            sql = "SELECT username, tenantid FROM eg_user WHERE username = ANY (%s)"
            cursor.execute(sql, (usernames,))
            rows = cursor.fetchall()
            return {row[0]: row[1] for row in rows}
    except Exception as e:
        logger.error(f"Error fetching user info for Mizoram: {e}")
        return {}


@router.post("/legacy_ticket_ingestion", summary="Upload and ingest legacy tickets Excel file")
async def upload_legacy_ticket_excel_sheet(
    legacy_ticket_file: UploadFile = File(...),
    legacy_ticket_sheet_name: str = Form(default="Legacy Tickets"),
    mapping_type_subtype_file: UploadFile = File(...),
    mapping_type_subtype_sheet_name: str = Form(default="Mapping Old_New_v1.0"),
    request_info: str = Form(default="")
):
    migration_id = str(uuid.uuid4())
    request_info_obj = request_info_from_json(request_info)
    get_authorized_request_info(request_info_obj)

    subtype_mapping = create_mapping_dicts(mapping_type_subtype_file, mapping_type_subtype_sheet_name)
    tenant_creator_mapping = TENANT_CREATOR_MAPPING

    with tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx") as input_temp_file:
        input_temp_file.write(await legacy_ticket_file.read())
        excel_file_path = input_temp_file.name

    df = pd.read_excel(excel_file_path, sheet_name=legacy_ticket_sheet_name)
    df.columns = df.columns.str.strip()
    df = df.reindex(columns=df.columns.tolist() + ['ticket_id', 'employee_info'], fill_value='')

    unique_states = df["State"].dropna().str.strip().unique()
    tenant_ids = [tenant_creator_mapping.get(state, {}).get("tenantId") for state in unique_states]

    tenant_mapping = get_tenant_mapping(request_info_obj, tenant_ids)
    block_mapping = get_block_mapping_from_mdms(request_info_obj, tenant_ids)

    conn = psycopg2.connect(**DB_CONFIG)
    codes = [str(row.get("NIN_HFR ID", "")).strip() for i, row in df.iterrows()
             if str(df.at[i, 'status']).strip().lower() not in ['duplicate', 'error']]
    employee_info = get_hrms_employee_info(codes, conn)

    usernames = [str(row.get("Actual User Name", "")).strip() for i, row in df.iterrows()
                 if str(row.get("State", "")).strip() == "Mizoram" and str(df.at[i, 'status']).strip().lower() not in ['duplicate', 'error']]
    user_info = get_user_info_for_mizoram(usernames, conn)

    for idx, row in df.iterrows():
        try:
            status = str(df.at[idx, 'status']).strip().lower()
            if status in ['duplicate', 'error']:
                continue

            state = str(row.get("State", "")).strip()
            if state == "Mizoram":
                identifier = str(row.get("Actual User Name", "")).strip()
                tenant_id = user_info.get(identifier)
            else:
                identifier = str(row.get("NIN_HFR ID", "")).strip()
                tenant_id = employee_info.get(identifier)

            if not tenant_id:
                df.at[idx, 'status'] = 'failed'
                df.at[idx, 'error'] = f'Employee not found for code: {identifier}'
                df.at[idx, 'employee_info'] = 'Not found'
                continue

            df.at[idx, 'employee_info'] = 'Found'

            tenant_details = tenant_mapping.get(tenant_id, {})
            if not tenant_details:
                df.at[idx, 'status'] = 'failed'
                df.at[idx, 'error'] = f'Tenant mapping not found for tenant ID: {tenant_id}'
                continue

            incident_payload = build_incident_payload(row, identifier, tenant_details, block_mapping, migration_id,
                                                      tenant_creator_mapping.get(state, {}), subtype_mapping)
            response = submit_incident_payload(incident_payload, tenant_creator_mapping.get(state, {}))
            process_response(response, df, idx, identifier)

        except Exception as e:
            df.at[idx, 'status'] = 'failed'
            df.at[idx, 'error'] = str(e)

    return write_and_return_excel(df, legacy_ticket_sheet_name)

def build_incident_payload(row, identifier, tenant_details, block_mapping, migration_id, creator_info, subtype_mapping):
    ticket_type = str(row.get("Ticket Type", "")).strip()
    ticket_subtype = str(row.get("Ticket Sub Type", "")).strip()
    system_functional = {"Yes": "FUNCTIONAL", "No": "NON_FUNCTIONAL"}.get(
        str(row.get("Is the solar system working?", "")).strip(), "")
    comments = str(row.get("Comments", "")).strip()[:256]
    mapped_pair = subtype_mapping.get((ticket_type, ticket_subtype))

    if not ticket_type or not ticket_subtype or not mapped_pair:
        raise ValueError("Missing or invalid Ticket Type/Sub Type")

    block_code = tenant_details.get("city", {}).get("blockCode", "")
    block = block_mapping.get(block_code, {}).get("name", "")

    incident_payload = {
        "incidentType": mapped_pair[0],
        "incidentSubtype": mapped_pair[1],
        "comments": comments,
        "systemFunctional": system_functional,
        "tenantId": tenant_details.get("code", ""),
        "migrationId": migration_id,
        "district": tenant_details.get("city", {}).get("districtCode", ""),
        "block": block,
        "phcType": tenant_details.get("code", ""),
        "phcSubType": tenant_details.get("centreType", ""),
        "additionalDetail": {"fileStoreId": [], "reopenreason": [], "rejectReason": [],
                              "sendBackReason": [], "sendBackSubReason": []},
        "source": "web",
        "reporter": {
            "uuid": creator_info.get("uuid"),
            "tenantId": creator_info.get("tenantId")
        }
    }

    if pd.notnull(row.get("Unique_ID")):
        incident_payload["legacyId"] = str(row.get("Unique_ID")).strip()

    reported_date = row.get("Actual_Reported_Date (mm/dd/yyyy)", None)
    if pd.notnull(reported_date):
        dt = pd.to_datetime(reported_date, format="%d/%m/%Y", errors='coerce') if isinstance(reported_date, str) else pd.to_datetime(reported_date, errors='coerce')
        if pd.notnull(dt):
            incident_payload["filedDate"] = int(dt.timestamp() * 1000)

    return incident_payload

def submit_incident_payload(payload, creator):
    profile = USER_PROFILE

    return requests.post(
        f"{im_services_url}/im-services/v2/request/_create",
        json={
            "RequestInfo": {
                "apiId": "Rainmaker",
                "authToken": "79967889-fbf5-42c6-9bd3-4adc0dbe7692",
                "userInfo": {
                    "id": creator.get("id"),
                    "uuid": creator.get("uuid"),
                    "userName": profile["userName"],
                    "name": profile["name"],
                    "mobileNumber": creator.get("mobileNumber"),
                    "emailId": None,
                    "locale": None,
                    "type": "EMPLOYEE",
                    "roles": [
                        {
                            "name": "Complainant",
                            "code": "COMPLAINANT",
                            "tenantId": creator.get("tenantId")
                        },
                        {
                            "name": "Employee",
                            "code": "EMPLOYEE",
                            "tenantId": creator.get("tenantId")
                        }
                    ],
                    "active": True,
                    "tenantId": creator.get("tenantId"),
                    "permanentCity": None
                },
                "msgId": "1744021633700|en_IN",
                "plainAccessRequest": {}
            },
            "incident": payload,
            "workflow": {"action": "APPLY", "verificationDocuments": []}
        },
        headers={"Content-Type": "application/json"}
    )

def process_response(response, df, idx, identifier):
    if response.status_code in [200, 201]:
        incident = response.json().get("IncidentWrappers", [{}])[0].get("incident")
        df.at[idx, 'status'] = 'success'
        df.at[idx, 'error'] = ''
        df.at[idx, 'ticket_id'] = incident.get("incidentId", '')
    else:
        error_msg = response.json().get('Errors', [{}])[0].get('message', response.text)
        df.at[idx, 'status'] = 'failed'
        df.at[idx, 'error'] = error_msg

def write_and_return_excel(df, sheet_name):
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_path = f"/tmp/legacy_ticket_ingestion_results_{timestamp}.xlsx"
    df.to_excel(output_path, sheet_name=sheet_name, index=False)
    return FileResponse(output_path, filename=os.path.basename(output_path), media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")


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