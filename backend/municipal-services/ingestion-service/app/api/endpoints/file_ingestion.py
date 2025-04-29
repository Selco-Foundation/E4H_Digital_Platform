from fastapi import APIRouter, File, Form, UploadFile, HTTPException, Response, Depends
from fastapi.responses import FileResponse
import tempfile
import os
from datetime import datetime

from app.core.logging import AppLogger
from app.decorators.rbac_validator import get_authorized_request_info
from app.ingest.excel_data_loader import ExcelDataLoader
from app.ingest.excel_data_writer import ExcelDataWriter
from app.ingest.facility_template_service import FacilityTemplateService
from app.processor.factory.vendor_data_processor_factory import VendorDataProcessorFactory
from app.utils.file_utils import create_temp_file, cleanup_temp_file
from app.utils.mdms_client import MDMSClient
from app.utils.organization_service_client import OrganizationServiceClient
from app.utils.convertor import request_info_from_json, create_vendor_request

router = APIRouter()
logger = AppLogger().get_logger()

from dotenv import load_dotenv
load_dotenv()
mdms_url = os.getenv("MDMS_URL")
org_service_url = os.getenv("VENDOR_SERVICE_URL")

@router.post('/ingest_vendors_excel',
             summary='Upload and process vendor Excel file with multiple sheets',
             response_description="Returns processed Excel file with validation results")
async def upload_vendors_excel_sheet(
        vendor_file: UploadFile = File(description="Excel file containing vendor data and boundary codes"),
        vendor_sheet_name: str = Form(default="Vendor Input", description="Name of the sheet containing vendor data"),
        boundary_sheet_name: str = Form(default="Boundary Code", description="Name of the sheet containing boundary codes"),
        request_info: str = Form(default="")
):
    input_temp_file = None
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

        with open(vendor_file_path, 'rb') as src, open(output_file_path, 'wb') as dst:
            dst.write(src.read())

        processor = VendorDataProcessorFactory.create_processor(
            file_path=output_file_path,
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

            # if isinstance(processor.data_loader, ExcelDataLoader):
            #     vendor_df = processor.data_loader.get_vendor_data()
            # else:
            #     logger.error("Data loader is not compatible")
            #     raise HTTPException(status_code=500, detail="Data loader incompatibility")

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
            writer = ExcelDataWriter(output_file_path, output_sheet="Vendor Output")
            writer.write_data(vendor_df)
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


@router.get('/getFacilityIngestionTemplate',
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