import os
import tempfile
from datetime import datetime

import openpyxl
from fastapi import APIRouter, Form, HTTPException, Depends, File, UploadFile
from fastapi.responses import FileResponse

from app.core.logging import AppLogger
from app.decorators.rbac_validator import get_authorized_request_info
from app.ingest.facility_template_service import FacilityTemplateService
from app.utils.convertor import request_info_from_json
from app.utils.file_utils import create_temp_file, cleanup_temp_file
from app.utils.mdms_client import MDMSClient

router = APIRouter()
logger = AppLogger().get_logger()

from dotenv import load_dotenv

load_dotenv()
mdms_url = os.getenv("MDMS_URL")

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
        facility_selection_file: UploadFile = File(description="Excel file containing facilities"),
        facility_output: str = Form(default="Facility Output Template",
                                    description="Name of the sheet containing facility data"),
        facility_service: FacilityTemplateService = Depends(),
        request_info: str = Form(default="")
):
    input_temp_file = None
    request_info = request_info_from_json(request_info)
    get_authorized_request_info(request_info)

    try:
        input_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await facility_selection_file.read()
        input_temp_file.write(content)
        input_temp_file.close()
        facility_file_path = input_temp_file.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"facility_selection_template_with_supervisors_{timestamp}.xlsx"
        output_temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        output_temp_file.close()
        output_file_path = output_temp_file.name

        # Process the Excel file
        facility_service.add_supervisor_columns_to_facility_template(facility_file_path, output_file_path, facility_output)

        # Return the file as a response
        return FileResponse(
            path=output_file_path,
            filename=output_filename,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )

    except Exception as e:
        logger.error(f"Error processing facility data: {e}")
        raise HTTPException(status_code=500, detail=f"Failed to process facility data: {str(e)}")

    finally:
        if input_temp_file and os.path.exists(input_temp_file.name):
            os.unlink(input_temp_file.name)