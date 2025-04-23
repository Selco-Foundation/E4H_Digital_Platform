from fastapi import APIRouter, File, Form, UploadFile, HTTPException, Response
from fastapi.responses import FileResponse
import tempfile
import os
from datetime import datetime

from app.core.logging import AppLogger
from app.ingest.excel_data_loader import ExcelDataLoader
from app.ingest.excel_data_writer import ExcelDataWriter
from app.processor.factory.vendor_data_processor_factory import VendorDataProcessorFactory
from app.utils.organization_service_client import OrganizationServiceClient

router = APIRouter()
logger = AppLogger().get_logger()

from dotenv import load_dotenv
load_dotenv()
mdms_url = os.getenv("MDMS_URL")
org_service_url = os.getenv("ORGANIZATION_URL")

@router.post('/ingest_vendors_excel',
             summary='Upload and process vendor Excel file with multiple sheets',
             response_description="Returns processed Excel file with validation results")
async def upload_vendors_excel_sheet(
        vendor_file: UploadFile = File(description="Excel file containing vendor data and boundary codes"),
        vendor_sheet_name: str = Form(default="Vendor Input", description="Name of the sheet containing vendor data"),
        boundary_sheet_name: str = Form(default="Boundary Code", description="Name of the sheet containing boundary codes"),
):
    input_temp_file = None
    output_temp_file = None

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
            org_service_url=org_service_url
        )

        # Process the data
        vendors = processor.process_data()

        if org_service_url and vendors:
            org_client = OrganizationServiceClient(org_service_url)

            if isinstance(processor.data_loader, ExcelDataLoader):
                vendor_df = processor.data_loader.get_vendor_data()
            else:
                logger.error("Data loader is not compatible")
                raise HTTPException(status_code=500, detail="Data loader incompatibility")

            for vendor in vendors:
                create_vendor_payload = {
                    "countryBoundaryCode": vendor.country_boundary_code,
                    "vendorName": vendor.vendor_name,
                    "vendorCode": vendor.vendor_code,
                    "vendorType": vendor.vendor_type,
                    "vendorSubtype": vendor.vendor_subtype,
                    "identifierType": vendor.identifier_type,
                    "identifierValue": vendor.identifier_value,
                    "hqAddress": vendor.hq_address,
                    "pincode": vendor.pincode,
                    "pocPhone": vendor.poc_phone,
                    "pocName": vendor.poc_name,
                    "tenantId": "in"
                }

                try:
                    org_data = org_client.create_vendor(create_vendor_payload)
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
