import os
import tempfile
from datetime import datetime

import pandas as pd
from fastapi import APIRouter, Form, HTTPException, Depends
from fastapi.responses import FileResponse

from app.core.logging import AppLogger
from app.decorators.rbac_validator import get_authorized_request_info
from app.ingest.facility_template_service import FacilityTemplateService
from app.ingest.project_service import ProjectService
from app.utils.convertor import request_info_from_json
from app.utils.file_utils import create_temp_file, cleanup_temp_file
from app.utils.mdms_client import MDMSClient

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
            facility_schema = mdms_client.fetch_facility_schema(request_info=request_info)
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


@router.get('/facilityWithSupervisors',
            summary='Generate facility ingestion template with supervisors Excel file',
            response_description="Returns Excel template with facility schema")
async def get_facility_ingestion_template_with_supervisors(
        parent_id: str = Form(default=""),
        request_info: str = Form(default="")
):
    temp_dir = tempfile.gettempdir()
    output_filename = f"facility_supervisors_template_{parent_id}.xlsx"
    output_file_path = os.path.join(temp_dir, output_filename)

    try:
        request_info = request_info_from_json(request_info)
        get_authorized_request_info(request_info)

        project_service = ProjectService()
        facilities = project_service.get_facilities(request_info, parent_id)

        try:
            df = pd.DataFrame(facilities)

            with pd.ExcelWriter(output_file_path, engine='openpyxl') as writer:
                df.to_excel(writer, index=False, sheet_name='Facilities_Supervisors')
                worksheet = writer.sheets['Facilities_Supervisors']
                for i, col in enumerate(df.columns):
                    column_width = max(df[col].astype(str).map(len).max(), len(col)) + 2
                    worksheet.column_dimensions[chr(65 + i)].width = column_width

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

