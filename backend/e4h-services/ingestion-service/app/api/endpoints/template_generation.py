from datetime import datetime
from typing import Optional, List
import psycopg2

import pandas as pd
from PIL import ImageDraw, Image, ImageFont
from fastapi import APIRouter, Form, HTTPException, Depends, Body
from fastapi.responses import FileResponse
from fastapi import BackgroundTasks
from openpyxl.reader.excel import load_workbook
from openpyxl.styles import Protection, PatternFill
from openpyxl.utils import get_column_letter

from app.core.logging import AppLogger
from app.decorators.rbac_validator import get_authorized_request_info
from app.ingest.facility_template_service import FacilityTemplateService
from app.ingest.project_service import ProjectService
from app.schemas.boundary import Boundary, flatten_boundaries
from app.utils.amc_scheduler_service_client import AMCSchedulerServiceClient
from app.utils.convertor import request_info_from_json
from app.utils.excel_utils import add_dropdowns_to_excel, autofit_columns, lock_prefilled_rows_in_excel, \
    lock_excel_columns
from app.utils.facility_service_client import FacilityServiceClient
from app.utils.fieldplan_activity_service_client import FieldPlanActivityServiceClient
from app.utils.fieldplan_service_client import FieldPlanServiceClient
from app.utils.file_utils import create_temp_file, cleanup_temp_file
from app.utils.mdms_client import MDMSClient
from app.utils.project_service_client import ProjectServiceClient
import os, tempfile, zipfile, qrcode, shutil

router = APIRouter()
logger = AppLogger().get_logger()

from dotenv import load_dotenv

load_dotenv()
mdms_url = os.getenv("MDMS_URL")
project_service_url = os.getenv("PROJECT_SERVICE_URL")
facility_service_url = os.getenv("FACILITY_SERVICE_URL")
fieldPlan_service_url = os.getenv("FIELDPLAN_SERVICE_URL")
fieldPlan_activity_service_url = os.getenv("FIELDPLAN_ACTIVITY_SERVICE_URL")
amc_scheduler_service_url = os.getenv("AMC_SCHEDULER_SERVICE_URL")
DEFAULT_AMC_ASSET_TYPES = ["INVERTER", "PANEL", "BATTERY"]
DB_CONFIG = {
    "host": os.getenv("DB_HOST"),
    "port": int(os.getenv("DB_PORT", 5432)),
    "database": os.getenv("DB_NAME"),
    "user": os.getenv("DB_USER"),
    "password": os.getenv("DB_PASSWORD")
}

@router.post('/facilityIngestionTemplateWithData',
            summary='Generate facility ingestion template Excel file with schema, already present data and boundary codes',
            response_description="Returns Excel template with facility schema, facility data and boundary codes")
async def get_facility_ingestion_template_with_data(
        background_tasks: BackgroundTasks,
        facility_service: FacilityTemplateService = Depends(),
    payload: dict = Body(..., description="Payload object")
):
    request_info = request_info_from_json(payload.get("RequestInfo", {}))
    boundary_data = payload.get("boundary_data", {})
    project_id = payload.get("project_id")
    mdms_client = MDMSClient(mdms_url)
    try:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_ingestion_template_{timestamp}.xlsx"
        output_file_path = create_temp_file(suffix=".xlsx")
        try:
            facility_schema = mdms_client.get_column_definitions_with_metadata(request_info, 'data-ingestion.FacilityIngestionSchema')
            boundary_list: List[Boundary] = flatten_boundaries(boundary_data)
        except Exception as e:
            logger.error(f"Error fetching data from external services: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=502, detail=f"External service error: {str(e)}")

        all_facilities = []
        if facility_service_url:
            facility_client = FacilityServiceClient(facility_service_url)
            for boundary in boundary_list:
                try:
                    results = facility_client.search_facility(tenant_id='in', boundary_code=boundary.code)
                    facilities = results.get('facilities', [])
                    all_facilities.extend(facilities)
                except Exception as e:
                    print(f"Error fetching boundary facilities: {e}")

        # Fetch project-linked facilities if project_id is provided
        project_linked_facility_ids = set()
        project_facilities_data = []
        if project_id and project_service_url:
            try:
                project_client = ProjectServiceClient(project_service_url)
                project_facilities_response = project_client.search_project_facility(request_info, project_id)
                project_facilities = project_facilities_response.get("ProjectFacilities", [])
                project_linked_facility_ids = {pf.get("facilityId") for pf in project_facilities if pf.get("facilityId")}
                logger.info(f"Found {len(project_linked_facility_ids)} facilities linked to project {project_id}")

                # Fetch full facility data for project-linked facilities
                for pf in project_facilities:
                    facility_id = pf.get("facilityId")
                    if facility_id:
                        try:
                            facility_data = facility_client.search_facility(tenant_id='in', facility_id=facility_id)
                            if facility_data and facility_data.get('facilities'):
                                project_facilities_data.extend(facility_data.get('facilities', []))
                        except Exception as e:
                            logger.error(f"Error fetching facility {facility_id}: {e}")

            except Exception as e:
                logger.error(f"Error fetching project facilities: {e}")
                # Continue without project facility data if there's an error

        # Combine boundary facilities with project facilities (avoid duplicates)
        # Only include project facilities that belong to the current boundary codes
        existing_facility_ids = {f.get('facility_id') for f in all_facilities}
        valid_boundary_codes = {boundary.code for boundary in boundary_list}
        
        for pf_facility in project_facilities_data:
            facility_id = pf_facility.get('facility_id')
            facility_boundary_code = pf_facility.get('boundary_code') or pf_facility.get('boundaryCode')
            
            # Only add if not already present and belongs to current boundary codes
            if (facility_id not in existing_facility_ids and 
                facility_boundary_code in valid_boundary_codes):
                all_facilities.append(pf_facility)
                logger.info(f"Added project facility {facility_id} to template (boundary: {facility_boundary_code})")
            elif facility_boundary_code not in valid_boundary_codes:
                logger.info(f"Skipped project facility {facility_id} - boundary code {facility_boundary_code} not in current boundary list")

        logger.info(f"Total facilities in template: {len(all_facilities)} (boundary: {len(existing_facility_ids)}, project: {len(project_facilities_data)})")

        # Mark facilities as included in project if they are already linked
        if project_id:
            for facility in all_facilities:
                facility_id = facility.get("facility_id")
                if facility_id in project_linked_facility_ids:
                    facility["include_in_project"] = "Yes"
                    logger.info(f"Facility {facility_id} is linked to project - marking as Yes")
                else:

                    facility["include_in_project"] = "No"
                    logger.info(f"Facility {facility_id} is NOT linked to project - marking as No")
        else:
            # If no project_id provided, set all facilities to "No"
            for facility in all_facilities:
                facility["include_in_project"] = "No"
                logger.info(f"No project_id provided - marking facility {facility.get('facility_id')} as No")

        try:
            facility_service.generate_template_file_with_data(
                output_path=output_file_path,
                facility_schema=facility_schema,
                boundary_list=boundary_list,
                facility_data=all_facilities,
                type="project",
                extra_append_rows=1000
            )
            logger.info(f"Successfully created facility ingestion template at {output_file_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=500, detail=f"Template generation error: {str(e)}")
        background_tasks.add_task(cleanup_temp_file, output_file_path)
        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Unhandled error in get_facility_ingestion_template: {e}")
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")

@router.get('/boundaryIngestionTemplate',
            summary='Generate empty boundary ingestion template Excel file',
            response_description="Returns an empty Excel template for boundary ingestion")
async def get_boundary_ingestion_template(
        background_tasks: BackgroundTasks,
        request_info: str = Form(default="")
):
    """
    Generate an empty boundary ingestion template with the required columns.
    Sheet name: 'Boundary Data'
    Columns: Country, State, District, Block, BoundaryCode
    """
    output_file_path = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"boundary_ingestion_template_{timestamp}.xlsx"
        output_file_path = create_temp_file(suffix=".xlsx")

        # Create an empty DataFrame with the expected boundary columns
        df = pd.DataFrame(columns=["Country", "State", "District", "Block", "BoundaryCode"])
        df.to_excel(output_file_path, sheet_name="Boundary Data", index=False)

        logger.info(f"Successfully created boundary ingestion template at {output_file_path}")

        background_tasks.add_task(cleanup_temp_file, output_file_path)
        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    except Exception as e:
        logger.error(f"Unhandled error in get_boundary_ingestion_template: {e}")
        if output_file_path:
            cleanup_temp_file(output_file_path)
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")


@router.post('/fieldplanFacilityIngestionTemplate',
            summary='Generate facility ingestion template Excel file with schema, already present data and boundary codes',
            response_description="Returns Excel template with facility schema, facility data and boundary codes")
async def get_facility_ingestion_template_with_data(
        background_tasks: BackgroundTasks,
        facility_service: FacilityTemplateService = Depends(),
    payload: dict = Body(..., description="Payload object")
):
    request_info = request_info_from_json(payload.get("RequestInfo", {}))
    # RequestInfo = request_info_from_json(payload.get("RequestInfo", {}))
    boundary_data = payload.get("boundary_data", {})
    fieldplan_id = payload.get("fieldplan_id")
    project_id = payload.get("project_id")
    mdms_client = MDMSClient(mdms_url)
    fieldplan_activity_client = FieldPlanActivityServiceClient(fieldPlan_activity_service_url)
    try:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_ingestion_template_{timestamp}.xlsx"
        output_file_path = create_temp_file(suffix=".xlsx")
        try:
            facility_schema = mdms_client.get_column_definitions_with_metadata(request_info, 'data-ingestion.FieldPlanFacilityIngestionSchema')
            boundary_list: List[Boundary] = flatten_boundaries(boundary_data)
        except Exception as e:
            logger.error(f"Error fetching data from external services: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=502, detail=f"External service error: {str(e)}")

        # Get all project-linked facilities
        project_client = ProjectServiceClient(project_service_url)
        project_facilities_response = project_client.search_project_facility(request_info, project_id)
        project_facilities = project_facilities_response.get("ProjectFacilities", [])
        project_linked_facility_ids = {pf.get("facilityId") for pf in project_facilities if pf.get("facilityId")}
        logger.info(f"Found {len(project_linked_facility_ids)} facilities currently linked to project {project_id}")

        all_facilities = []
        # fieldplan_client = None
        if facility_service_url and project_linked_facility_ids:
            facility_client = FacilityServiceClient(facility_service_url)
            for boundary in boundary_list:
                for facilityId in project_linked_facility_ids:
                    try:
                        results = facility_client.search_facility(facility_id=facilityId, tenant_id='in', boundary_code=boundary.code)
                        facilities = results.get('facilities', [])
                        all_facilities.extend(facilities)
                    except Exception as e:
                        print(f"Error fetching boundary facilities: {e}")

        # Fetch fieldplan-linked facilities if fieldplan_id is provided
        fieldplan_linked_facility_ids = set()
        fieldplan_facilities_data = []
        if fieldplan_id and fieldPlan_service_url:
            try:
                fieldplan_client = FieldPlanServiceClient(fieldPlan_service_url)
                fieldplan_facilities_response = fieldplan_client.search_fieldplan_facility(request_info, fieldplan_id)
                fieldplan_facilities = fieldplan_facilities_response.get("FieldPlanFacilities", [])
                fieldplan_linked_facility_ids = {pf.get("facilityId") for pf in fieldplan_facilities if
                                               pf.get("facilityId")}
                logger.info(
                    f"Found {len(fieldplan_linked_facility_ids)} facilities linked to fieldplan {fieldplan_id}")

                # Fetch full facility data for fieldplan-linked facilities
                for pf in fieldplan_facilities:
                    facility_id = pf.get("facilityId")
                    if facility_id:
                        try:
                            facility_data = facility_client.search_facility(tenant_id='in',
                                                                            facility_id=facility_id)
                            if facility_data and facility_data.get('facilities'):
                                fieldplan_facilities_data.extend(facility_data.get('facilities', []))
                        except Exception as e:
                            logger.error(f"Error fetching facility {facility_id}: {e}")

            except Exception as e:
                logger.error(f"Error fetching fieldplan facilities: {e}")
                # Continue without fieldplan facility data if there's an error

        # Combine boundary facilities with fieldplan facilities (avoid duplicates)
        # Only include fieldplan facilities that belong to the current boundary codes
        existing_facility_ids = {f.get('facility_id') for f in all_facilities}
        valid_boundary_codes = {boundary.code for boundary in boundary_list}

        for pf_facility in fieldplan_facilities_data:
            facility_id = pf_facility.get('facility_id')
            facility_boundary_code = pf_facility.get('boundary_code') or pf_facility.get('boundaryCode')

            # Only add if not already present and belongs to current boundary codes
            if (facility_id not in existing_facility_ids and facility_id in project_linked_facility_ids and facility_boundary_code in valid_boundary_codes):
                all_facilities.append(pf_facility)
                logger.info(
                    f"Added fieldplan facility {facility_id} to template (boundary: {facility_boundary_code})")
            elif (facility_id not in project_linked_facility_ids): # In case the facility is no longer mapped to project, so unlink the facility
                fieldPlan_facility_data = next(
                    (pf for pf in fieldplan_facilities if pf.get("facilityId") == facility_id),
                    None)
                fieldplan_client.unlink_fieldplan_facility(
                    request_info=request_info,
                    fieldplan_id=fieldplan_id,
                    facility_id=facility_id,
                    fieldplan_facility_data=fieldPlan_facility_data
                )

                facilities_activity_response = fieldplan_activity_client.search_facility_activity(
                    request_info, fieldplan_id, facility_id)
                facilities_activity = facilities_activity_response.get("FacilityActivities", [])
                facility_activity_ids = list({fa.get("activityFacility").get("id") for fa in facilities_activity if
                                              fa.get("activityFacility").get("id")})
                fieldplan_activity_client.delete_facility_activity(request_info=request_info,
                                                                   facility_activity_id=facility_activity_ids)
            elif facility_boundary_code not in valid_boundary_codes:
                logger.info(
                    f"Skipped fieldplan facility {facility_id} - boundary code {facility_boundary_code} not in current boundary list")

        logger.info(
            f"Total facilities in template: {len(all_facilities)} (boundary: {len(existing_facility_ids)}, fieldplan: {len(fieldplan_facilities_data)})")

        # Mark facilities as included in fieldplan if they are already linked
        if fieldplan_id:
            for facility in all_facilities:
                facility_id = facility.get("facility_id")
                if facility_id in fieldplan_linked_facility_ids:
                    facility["include_in_fieldplan"] = "Yes"
                    logger.info(f"Facility {facility_id} is linked to fieldplan - marking as Yes")
                else:

                    facility["include_in_fieldplan"] = "No"
                    logger.info(f"Facility {facility_id} is NOT linked to fieldplan - marking as No")
        else:
            # If no fieldplan_id provided, set all facilities to "No"
            for facility in all_facilities:
                facility["include_in_fieldplan"] = "No"
                logger.info(f"No fieldplan_id provided - marking facility {facility.get('facility_id')} as No")

        try:
            facility_service.generate_template_file_with_data(
                output_path=output_file_path,
                facility_schema=facility_schema,
                boundary_list=boundary_list,
                facility_data=all_facilities,
                type="fieldplan",
                extra_append_rows=0
            )
            logger.info(f"Successfully created facility ingestion template at {output_file_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=500, detail=f"Template generation error: {str(e)}")
        background_tasks.add_task(cleanup_temp_file, output_file_path)
        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Unhandled error in get_facility_ingestion_template: {e}")
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")


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
            boundary_data = facility_service.get_all_boundaries()
            vendor_data = facility_service.get_all_vendor_codes(request_info)
        except Exception as e:
            logger.error(f"Error fetching data from external services: {e}")
            cleanup_temp_file(output_file_path)
            raise HTTPException(status_code=502, detail=f"External service error: {str(e)}")

        try:
            facility_service.generate_template_file(
                output_path=output_file_path,
                facility_schema=facility_schema,
                boundary_data=boundary_data,
                vendor_data=vendor_data
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

@router.post('/facilityWithStaff',
            summary='Generate facility ingestion template with staff Excel file',
            response_description="Returns Excel template with facility schema")
async def get_facility_ingestion_template_with_staff(
        parent_id: str = Form(..., description="Parent project ID"),
        request_info: str = Form(..., description="Serialized RequestInfo JSON")
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
                    worksheet.column_dimensions[get_column_letter(i + 1)].width = column_width

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

@router.post('/facilityWithSupervisors',
            summary='Generate facility ingestion template with supervisors Excel file',
            response_description="Returns Excel template with facility schema")
async def get_facility_ingestion_template_with_supervisors(
        parent_id: str = Form(..., description="Parent project ID"),
        request_info: str = Form(..., description="Serialized RequestInfo JSON")
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
                    worksheet.column_dimensions[get_column_letter(i + 1)].width = column_width

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


@router.post('/facilityQRGeneration', summary='Generate QR for facility',
             response_description="Returns zip with QR codes")
async def get_facility_QR_for_autologin(
        request_info: str = Form(default="")
):
    request_info_obj = request_info_from_json(request_info)

    try:
        mdms_client = MDMSClient(mdms_url=mdms_url)
        mdms_content = mdms_client.get_tenant_mapping(request_info_obj, ["as", "gj", "ml", "mn", "mz", "nl", "or", "pg", "sk"])


        base_url = "https://saura-emitra-uat.selcofoundation.org"
        password = "Health@2026"

        temp_dir = tempfile.mkdtemp()

        for tenant_id, health_facility_data in mdms_content.items():

            state = health_facility_data.get("address")
            district = health_facility_data.get("city", {}).get("districtName")
            block = health_facility_data.get("city", {}).get("blockCode")
            facility_name = health_facility_data.get("name")

            if not all([state, district, block, facility_name]):
                print(f"Skipping tenant {tenant_id}: Missing state/district/block/facility_name.")
                continue

            qr_folder = os.path.join(temp_dir, state, district, block, facility_name)
            os.makedirs(qr_folder, exist_ok=True)

            conn = psycopg2.connect(**DB_CONFIG)
            with conn.cursor() as cursor:
                sql = "SELECT code FROM eg_hrms_employee WHERE tenantid = %s"
                cursor.execute(sql, (health_facility_data["code"],))
                rows = cursor.fetchall()

            if not rows:
                print(f"Skipping tenant {tenant_id} ({facility_name}): No HRMS employee code found for tenantid {health_facility_data['code']}")
                continue

            username = rows[0][0]


            if state=='Karnataka':
                url_state_name = 'digit-ui'
            else:
                url_state_name = state.lower()

            login_url = f"{base_url}/{url_state_name}/employee/user/login?tenantid={tenant_id}&username={username}&passwd={password}"

            qr = qrcode.make(login_url).convert("RGB")

            # Create a new image (taller) to hold QR and text
            width, height = qr.size
            font_size = 30
            padding = 10

            try:
                font = ImageFont.truetype("DejaVuSans-Bold.ttf", font_size)
            except:
                font = ImageFont.load_default()

            # Create a new image with extra space for text
            new_height = height + font_size + 2 * padding
            combined = Image.new("RGB", (width, new_height), "white")
            combined.paste(qr, (0, 0))

            # Draw the facility name
            draw = ImageDraw.Draw(combined)
            text = facility_name
            bbox = draw.textbbox((0, 0), text, font=font)
            text_width = bbox[2] - bbox[0]
            text_position = ((width - text_width) // 2, height + padding)
            draw.text(text_position, text, font=font, fill="black")

            # Save the combined image
            qr_filename = f"{username}.png"
            img_path = os.path.join(qr_folder, qr_filename)
            combined.save(img_path)

        # Create ZIP
        zip_path = os.path.join(tempfile.gettempdir(), "facility_qr_codes.zip")
        with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zipf:
            for root, _, files in os.walk(temp_dir):
                for file in files:
                    abs_file = os.path.join(root, file)
                    rel_path = os.path.relpath(abs_file, temp_dir)
                    zipf.write(abs_file, arcname=rel_path)

        shutil.rmtree(temp_dir)

        return FileResponse(
            path=zip_path,
            filename="facility_qr_codes.zip",
            media_type="application/zip"
        )
    except Exception as e:
        return {"status": "error", "message": str(e)}

@router.post('/amcConfigurationTemplate',
            summary='Generate AMC configuration ingestion template',
            response_description="Returns Excel template with facility asset metadata for AMC configurations")
async def get_amc_configuration_template(
        background_tasks: BackgroundTasks,
        payload: dict = Body(..., description="Payload containing RequestInfo, boundary_data")
):
    request_info = request_info_from_json(payload.get("RequestInfo", {}))

    boundary_data = payload.get("boundary_data", {})
    project_id = payload.get("project_id")  

    if not boundary_data:
        raise HTTPException(status_code=400, detail="boundary_data is required")

    if not facility_service_url:
        raise HTTPException(status_code=500, detail="Facility service is not configured")

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_filename = f"amc_configuration_template_{timestamp}.xlsx"
    output_file_path = create_temp_file(suffix=".xlsx")

    sheet_name = "amc-configurations"
    columns = [
        "Facility Id",
        "NIN/HFR ID",
        "BoundaryCode",
        "Health Facility Name",
        "Vendor",
        "AMC-Frequency",
        "AMC-Duration"
    ]

    try:
        # Flatten boundaries from boundary_data
        boundary_list: List[Boundary] = flatten_boundaries(boundary_data)

        # Get all project-linked facilities if project_id is provided
        project_linked_facility_ids = set()
        if project_id and project_service_url:
            try:
                project_client = ProjectServiceClient(project_service_url)
                project_facilities_response = project_client.search_project_facility(request_info, project_id)
                project_facilities = project_facilities_response.get("ProjectFacilities", [])
                project_linked_facility_ids = {pf.get("facilityId") for pf in project_facilities if pf.get("facilityId")}
                logger.info(f"Found {len(project_linked_facility_ids)} facilities currently linked to project {project_id}")
            except Exception as e:
                logger.error(f"Error fetching project facilities: {e}")
                # Continue without project facility filtering if there's an error

        # Fetch facilities by boundary codes and project (if project_id provided)
        facility_client = FacilityServiceClient(facility_service_url)
        all_facilities = []

        if project_id and project_linked_facility_ids:
            # Filter facilities by both project and boundary
            for boundary in boundary_list:
                for facilityId in project_linked_facility_ids:
                    try:
                        results = facility_client.search_facility(facility_id=facilityId, tenant_id='in', boundary_code=boundary.code)
                        facilities = results.get('facilities', [])
                        all_facilities.extend(facilities)
                    except Exception as e:
                        logger.error(f"Error fetching boundary facilities for {boundary.code} and facility {facilityId}: {e}")
        else:
            # If no project_id provided, fetch all facilities by boundary codes
            for boundary in boundary_list:
                try:
                    results = facility_client.search_facility(tenant_id='in', boundary_code=boundary.code)
                    facilities = results.get('facilities', [])
                    all_facilities.extend(facilities)
                except Exception as e:
                    logger.error(f"Error fetching boundary facilities for {boundary.code}: {e}")

        logger.info(f"Total facilities in AMC template: {len(all_facilities)} (project_id: {project_id}, boundaries: {len(boundary_list)})")

        # Fetch approved vendors for dropdown
        facility_service = FacilityTemplateService()
        vendor_data = facility_service.get_all_vendor_codes(request_info)
        vendor_names = [v.get("Vendor Name", "") for v in vendor_data if v.get("Vendor Name")]

        # Initialize AMC scheduler client to check for existing configurations
        amc_client = None
        if amc_scheduler_service_url and project_id:
            amc_client = AMCSchedulerServiceClient(amc_scheduler_service_url)

        # Create rows for AMC configuration template - one row per facility
        # Asset types ["INVERTER","PANEL","BATTERY"] will be used as default for each configuration during processing
        rows = []
        rows_with_existing_amc = []  # Track row indices that have existing AMC configurations

        def convert_frequency_to_display(frequency_months):
            """Convert frequency in months to display format"""
            if frequency_months == 6:
                return "Every 6 Months"
            elif frequency_months == 12:
                return "Every 1 Year"
            return ""

        def convert_duration_to_display(duration_months):
            """Convert duration in months to display format"""
            if duration_months == 12:
                return "1 Year"
            elif duration_months == 36:
                return "3 Years"
            elif duration_months == 60:
                return "5 Years"
            return ""

        for idx, facility in enumerate(all_facilities):
            facility_details = facility.get("facility_details", {}) or {}
            nin_id = facility_details.get("nin_id", "")
            hfr_id = facility_details.get("hfr_id", "")
            # Use NIN ID if available, otherwise HFR ID, otherwise empty
            nin_hfr_id = nin_id if nin_id else (hfr_id if hfr_id else "")
            facility_name = facility.get("facility_name", "")
            boundary_code = facility.get("boundary_code") or facility.get("boundaryCode", "")
            facility_id = facility.get("facility_id", "")

            # Initialize row with empty values
            vendor_value = ""
            frequency_value = ""
            duration_value = ""

            # Check for existing AMC configuration if project_id and facility_id are available
            if amc_client and project_id and facility_id:
                try:
                    existing_configs = amc_client.search_amc_configurations(
                        request_info,
                        facility_id=facility_id,
                        project_id=project_id
                    )
                    # Check if any configurations exist
                    configs = existing_configs.get("AmcConfigurations", [])
                    if configs:
                        # Use the first configuration found
                        existing_config = configs[0]
                        vendor_value = existing_config.get("vendor", "")
                        frequency_months = existing_config.get("frequency")
                        duration_months = existing_config.get("duration")

                        if vendor_value:
                            frequency_value = convert_frequency_to_display(frequency_months)
                            duration_value = convert_duration_to_display(duration_months)
                            rows_with_existing_amc.append(idx)
                            logger.info(f"Found existing AMC config for facility {facility_id}: vendor={vendor_value}, frequency={frequency_value}, duration={duration_value}")
                except Exception as e:
                    logger.warning(f"Error checking existing AMC config for facility {facility_id}: {e}")
                    # Continue without existing config data

            # Create one row per facility (asset types are handled internally during processing)
            rows.append({
                "Facility Id": facility_id,
                "NIN/HFR ID": nin_hfr_id,
                "BoundaryCode": boundary_code,
                "Health Facility Name": facility_name,
                "Vendor": vendor_value,
                "AMC-Frequency": frequency_value,
                "AMC-Duration": duration_value
            })

        # Create DataFrame and write to Excel
        df = pd.DataFrame(rows, columns=columns)
        with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
            df.to_excel(writer, index=False, sheet_name=sheet_name)

            # Add Boundary Data Sheet
            boundary_records = []
            for boundary in boundary_list:
                boundary_records.append({
                    "Country": boundary.get("country", ""),
                    "State": boundary.get("state", ""),
                    "District": boundary.get("district", ""),
                    "Block": boundary.get("block", ""),
                    "BoundaryCode": boundary.get("code", "")
                })
            if not boundary_records:
                boundary_records.append({
                    "Country": "",
                    "State": "",
                    "District": "",
                    "Block": "",
                    "BoundaryCode": ""
                })

            df_boundary = pd.DataFrame(boundary_records)
            df_boundary.to_excel(writer, index=False, sheet_name="BoundaryCodes")

        # Lock the boundary sheet
        lock_excel_columns(
            file_path=output_file_path,
            sheet_name="BoundaryCodes",
            column_headers_to_unlock=[]
        )

        # Add dropdowns for vendor, amc-frequency, and amc-duration
        dropdowns_map = {}

        # Vendor dropdown with approved vendor names
        if vendor_names:
            dropdowns_map["Vendor"] = vendor_names

        # AMC Frequency dropdown
        dropdowns_map["AMC-Frequency"] = ["Every 6 Months", "Every 1 Year"]

        # AMC Duration dropdown
        dropdowns_map["AMC-Duration"] = ["1 Year", "3 Years", "5 Years"]

        allow_blank_map = {
            "Vendor": True,
            "AMC-Frequency": True,
            "AMC-Duration": True
        }

        add_dropdowns_to_excel(
            file_path=output_file_path,
            sheet_name=sheet_name,
            dropdowns=dropdowns_map,
            allow_blank_map=allow_blank_map,
            max_extra_rows=500
        )

        # Lock prefilled rows except editable columns
        # BoundaryCode is pre-filled and should be locked
        lock_prefilled_rows_in_excel(
            file_path=output_file_path,
            sheet_name=sheet_name,
            editable_columns=["Vendor", "AMC-Frequency", "AMC-Duration"],
            total_rows=len(rows),
            total_columns=len(columns),
            always_locked_columns=["BoundaryCode"],
            extra_append_rows=500
        )

        # Lock Vendor, AMC-Frequency, and AMC-Duration for rows with existing AMC configurations
        if rows_with_existing_amc:
            wb = load_workbook(output_file_path)
            ws = wb[sheet_name]
            grey_fill = PatternFill(start_color="D3D3D3", end_color="D3D3D3", fill_type="solid")

            # Find column indices for Vendor, AMC-Frequency, and AMC-Duration
            header_row = [cell.value for cell in ws[1]]
            vendor_col_idx = None
            frequency_col_idx = None
            duration_col_idx = None

            for idx, header in enumerate(header_row, start=1):
                if header == "Vendor":
                    vendor_col_idx = idx
                elif header == "AMC-Frequency":
                    frequency_col_idx = idx
                elif header == "AMC-Duration":
                    duration_col_idx = idx

            # Lock cells for rows with existing AMC configurations
            # Note: rows_with_existing_amc contains 0-based indices, Excel rows are 1-based
            # Header is row 1, so data starts at row 2
            for row_idx_0based in rows_with_existing_amc:
                excel_row = row_idx_0based + 2  # +2 because header is row 1, and 0-based to 1-based

                if vendor_col_idx:
                    cell = ws.cell(row=excel_row, column=vendor_col_idx)
                    cell.protection = Protection(locked=True)
                    cell.fill = grey_fill

                if frequency_col_idx:
                    cell = ws.cell(row=excel_row, column=frequency_col_idx)
                    cell.protection = Protection(locked=True)
                    cell.fill = grey_fill

                if duration_col_idx:
                    cell = ws.cell(row=excel_row, column=duration_col_idx)
                    cell.protection = Protection(locked=True)
                    cell.fill = grey_fill

            # Re-enable sheet protection
            ws.protection.sheet = True
            ws.protection.enable()
            wb.save(output_file_path)
            logger.info(f"Locked AMC fields for {len(rows_with_existing_amc)} rows with existing AMC configurations")

        # Autofit columns
        autofit_columns(
            file_path=output_file_path,
            sheet_name=sheet_name
        )
        autofit_columns(
            file_path=output_file_path,
            sheet_name="BoundaryCodes"
        )

        background_tasks.add_task(cleanup_temp_file, output_file_path)
        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    except HTTPException:
        cleanup_temp_file(output_file_path)
        raise
    except Exception as e:
        logger.error(f"Unhandled error in get_amc_configuration_template: {e}")
        cleanup_temp_file(output_file_path)
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")
