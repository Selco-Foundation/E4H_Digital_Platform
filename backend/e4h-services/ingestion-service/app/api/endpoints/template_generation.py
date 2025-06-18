import os
import tempfile
from datetime import datetime
from typing import Optional, List, Dict, Any

import pandas as pd
from fastapi import APIRouter, Form, HTTPException, Depends
from fastapi.responses import FileResponse

from app.core.logging import AppLogger
from app.decorators.rbac_validator import get_authorized_request_info
from app.ingest.facility_template_service import FacilityTemplateService
from app.ingest.project_service import ProjectService
from app.utils.convertor import request_info_from_json
from app.utils.excel_utils import add_dropdowns_to_excel
from app.utils.facility_service_client import FacilityServiceClient
from app.utils.file_utils import create_temp_file, cleanup_temp_file
from app.utils.mdms_client import MDMSClient
from app.utils.project_service_client import ProjectServiceClient

router = APIRouter()
logger = AppLogger().get_logger()

from dotenv import load_dotenv

load_dotenv()
mdms_url = os.getenv("MDMS_URL")
project_service_url = os.getenv("PROJECT_SERVICE_URL")
facility_service_url = os.getenv("FACILITY_SERVICE_URL")

@router.get('/facilityIngestion',
            summary='Generate facility ingestion template Excel file with schema and boundary codes',
            response_description="Returns Excel template with facility schema and boundary codes")
async def get_facility_ingestion_template(
        facility_service: FacilityTemplateService = Depends(),
        request_info: str = Form(default="")
):
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)
    mdms_client = MDMSClient(mdms_url)
    try:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_ingestion_template_{timestamp}.xlsx"
        output_file_path = create_temp_file(suffix=".xlsx")
        try:
            facility_schema = mdms_client.get_column_definitions_with_metadata(request_info, 'data-ingestion.FacilityIngestionSchema')
            boundary_data = facility_service.get_all_boundaries(request_info)
        except Exception as e:
            logger.error(f"Error fetching data from external services: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=502, detail=f"External service error: {str(e)}")

        try:
            facility_service.generate_template_file(
                output_path=output_file_path,
                facility_schema=facility_schema,
                boundary_data=boundary_data
            )
            logger.info(f"Successfully created facility ingestion template at {output_file_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=500, detail=f"Template generation error: {str(e)}")

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Unhandled error in get_facility_ingestion_template: {e}")
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")

@router.get('/facilityWithStaff',
            summary='Generate facility ingestion template with staff Excel file',
            response_description="Returns Excel template with facility schema")
async def get_facility_ingestion_template_with_staff(
        parent_id: str = Form(default=""),
        request_info: str = Form(default="")
):
    temp_dir = tempfile.gettempdir()
    ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
    output_filename = f"facility_staff_template_{parent_id}_{ts}.xlsx"
    output_file_path = os.path.join(temp_dir, output_filename)

    try:
        request_info = request_info_from_json(request_info)
        get_authorized_request_info(request_info)

        project_service = ProjectService()
        facilities = project_service.get_facilities(request_info, parent_id, "Staff")
        facility_template_service = FacilityTemplateService()

        try:
            original_df = pd.DataFrame(facilities)
            df = facility_template_service.add_supervisor_columns_to_dataframe(original_df)

            with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
                df.to_excel(writer, index=False, sheet_name='Facilities_Staff')
                worksheet = writer.sheets['Facilities_Staff']
                for i, col in enumerate(df.columns):
                    column_width = max(df[col].astype(str).map(len).max(), len(col)) + 2
                    worksheet.column_dimensions[chr(65 + i)].width = column_width

            dropdowns_map = {'Role (Mandatory) ?': ['Staff', 'Field Planner']}
            add_dropdowns_to_excel(
                file_path=output_file_path,
                sheet_name="Facilities_Staff",
                dropdowns=dropdowns_map
            )

        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=500, detail=f"Template generation error: {str(e)}")

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Unhandled error in get_facility_ingestion_template_with_staff: {e}")
        cleanup_temp_file(output_file_path)
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")

@router.get('/facilityWithSupervisors',
            summary='Generate facility ingestion template with supervisors Excel file',
            response_description="Returns Excel template with facility schema")
async def get_facility_ingestion_template_with_supervisors(
        parent_id: str = Form(default=""),
        request_info: str = Form(default="")
):
    temp_dir = tempfile.gettempdir()
    ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
    output_filename = f"facility_supervisors_template_{parent_id}_{ts}.xlsx"
    output_file_path = os.path.join(temp_dir, output_filename)

    try:
        request_info = request_info_from_json(request_info)
        get_authorized_request_info(request_info)

        project_service = ProjectService()
        facilities = project_service.get_facilities(request_info, parent_id, "Supervisor")
        facility_template_service = FacilityTemplateService()

        try:
            original_df = pd.DataFrame(facilities)
            df = facility_template_service.add_supervisor_columns_to_dataframe(original_df)

            with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
                df.to_excel(writer, index=False, sheet_name='Facilities_Supervisors')
                worksheet = writer.sheets['Facilities_Supervisors']
                for i, col in enumerate(df.columns):
                    column_width = max(df[col].astype(str).map(len).max(), len(col)) + 2
                    worksheet.column_dimensions[chr(65 + i)].width = column_width

            dropdowns_map = {'Role (Mandatory) ?': ['Supervisor', 'Field Planner']}
            add_dropdowns_to_excel(
                file_path=output_file_path,
                sheet_name="Facilities_Supervisors",
                dropdowns=dropdowns_map
            )

        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=500, detail=f"Template generation error: {str(e)}")

        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Unhandled error in get_facility_ingestion_template_with_supervisors: {e}")
        cleanup_temp_file(output_file_path)
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")

@router.get('/facilitySelection',
            summary='Generate facility selection template Excel file',
            response_description="Returns Excel template with facility data")
async def get_facility_selection_template(
        facility_service: FacilityTemplateService = Depends(),
        parent_project_id: Optional[str] = Form(default=None),
        boundary_codes: str = Form(...),
        request_info: str = Form(default="")
):
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)
    mdms_client = MDMSClient(mdms_url)

    boundary_code_list: List[str] = [code.strip() for code in boundary_codes.split(",") if code.strip()]

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_filename = f"facility_selection_template_{timestamp}.xlsx"
    output_file_path = create_temp_file(suffix=".xlsx")

    boundary_facilities = []
    project_facilities = []

    facility_client = None

    try:
        facility_selection_schema = mdms_client.fetch_facility_selection_schema(request_info=request_info)
    except Exception as e:
        logger.error(f"Error fetching data from external services: {e}")
        cleanup_temp_file(output_file_path)
        raise HTTPException(status_code=502, detail=f"External service error: {str(e)}")

    if facility_service_url:
        facility_client = FacilityServiceClient(facility_service_url)
        for boundary_code in boundary_code_list:
            try:
                results = facility_client.search_facility(tenant_id='in', boundary_code=boundary_code)
                boundary_facilities.extend(results.get('facilities', []))
            except Exception as e:
                print(f"Error fetching boundary facilities: {e}")

    if project_service_url and parent_project_id:
        project_client = ProjectServiceClient(project_service_url)
        try:
            pf_response = project_client.search_project_facility(
                request_info=request_info,
                project_id=parent_project_id
            )
            raw_project_facilities = pf_response.get("ProjectFacilities", [])
            if raw_project_facilities and facility_client:
                for pf in raw_project_facilities:
                    facility_id = pf.get("facilityId")
                    if facility_id and any(f.get('facility_id') == facility_id for f in boundary_facilities):
                        try:
                            facility_data = facility_client.search_facility(tenant_id='in', facility_id=facility_id)
                            if facility_data:
                                project_facilities.extend(facility_data.get('facilities', []))
                        except Exception as e:
                            print(f"Error fetching facility {facility_id}: {e}")
        except Exception as e:
            print(f"Error fetching project facilities: {e}")

    # Intersect by facility_id
    if parent_project_id:
        intersected_facilities = project_facilities
    else:
        intersected_facilities = boundary_facilities

    try:
        facility_service.generate_selection_template_file(
            output_path=output_file_path,
            facility_selection_schema=facility_selection_schema,
            facility_data=intersected_facilities
        )
        logger.info(f"Successfully created facility ingestion template at {output_file_path}")
    except Exception as e:
        logger.error(f"Error generating template file: {e}")
        cleanup_temp_file(output_file_path)
        raise HTTPException(status_code=500, detail=f"Template generation error: {str(e)}")

    return FileResponse(
        path=output_file_path,
        filename=output_filename,
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )