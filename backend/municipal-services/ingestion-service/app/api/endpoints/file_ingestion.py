import os
import tempfile
from datetime import datetime

import pandas as pd
from fastapi import APIRouter, File, Form, UploadFile, HTTPException
from fastapi.responses import FileResponse

from app.core.logging import AppLogger
from app.decorators.rbac_validator import get_authorized_request_info
from app.ingest.excel_data_writer import ExcelDataWriter
from app.processor.factory.boundary_data_processor_factory import BoundaryDataProcessorFactory
from app.processor.factory.vendor_data_processor_factory import VendorDataProcessorFactory
from app.utils.convertor import request_info_from_json, create_vendor_request, create_facility_payload, \
    get_project_creation_payload, get_user_creation_payload, get_staff_creation_payload
from app.utils.facility_service_client import FacilityServiceClient
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
            for index, row in df[df['status'] != 'success'].iterrows():
                try:
                    facility_data_payload = create_facility_payload(request_info, row)
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

@router.post('/facilityWithSupervisors',
             summary='Upload and process facility with supervisors Excel file',
             response_description="Returns processed Excel file with validation results")
async def upload_facility_with_supervisors_excel_sheet(
        facility_with_supervisors: UploadFile = File(
            description="Excel file containing facility with supervisors data"),
        facility_sheet: str = Form(default="Facilities_Supervisors",
                                    description="Name of the sheet containing facility data"),
        request_info: str = Form(default="")
):
    input_temp_file = None
    output_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await facility_with_supervisors.read()
        input_temp_file.write(content)
        input_temp_file.close()
        facility_with_supervisors_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_with_supervisor_ingestion_results_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        with open(facility_with_supervisors_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        df = pd.read_excel(facility_with_supervisors_file_path, sheet_name=facility_sheet)

        if 'status' not in df.columns:
            df['status'] = ''
        if 'error' not in df.columns:
            df['error'] = ''

        if project_service_url and not df.empty:
            project_client = ProjectServiceClient(project_service_url)
            hrms_client = HRMSServiceClient(hrms_service_url)
            for index, row in df.iterrows():
                try:
                    # Create work stream
                    work_stream_creation_payload = get_project_creation_payload(request_info, "Work Stream "+row.get('Health Centre Name (Mandatory)', ''), "Work Stream")
                    work_stream_creation_response = project_client.create_project(work_stream_creation_payload)
                    if work_stream_creation_response.status_code == 200 or work_stream_creation_response.status_code == 201:
                        df.at[index, 'status'] = 'success'
                        # Create User
                        user_creation_payload = get_user_creation_payload(request_info, row)
                        user_creation_response = hrms_client.create_user(user_creation_payload)
                        if user_creation_response.status_code == 200 or user_creation_response.status_code == 201:
                            df.at[index, 'status'] = 'success'
                            # Create staff
                            staff_creation_payload = get_staff_creation_payload(request_info,user_creation_response["uuid"], work_stream_creation_response["projectId"])
                            staff_creation_response = project_client.create_project_staff(staff_creation_payload)
                            if staff_creation_response.status_code == 200 or staff_creation_response.status_code ==201:
                                df.at[index,'status'] = 'success'
                            else:
                                df.at[index, 'status'] = 'failed'
                                df.at[index, 'error'] = f"Staff Creation Error: {staff_creation_response.status_code} - {staff_creation_response.text}"
                        else:
                            df.at[index, 'status'] = 'failed'
                            df.at[index, 'error'] = f"User Creation Error: {user_creation_response.status_code} - {user_creation_response.text}"

                    else:
                        df.at[index, 'status'] = 'failed'
                        df.at[index, 'error'] = f"Workstream Creation Error: {work_stream_creation_response.status_code} - {work_stream_creation_response.text}"
                except Exception as e:
                    df.at[index, 'status'] = 'failed'
                    df.at[index, 'error'] = f"Processing Error: {str(e)}"

        writer = ExcelDataWriter(output_file_path, output_sheet=facility_sheet)
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
