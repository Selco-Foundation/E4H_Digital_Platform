import os
import tempfile
from typing import List

from fastapi import APIRouter, UploadFile, File, Form, HTTPException, Depends
from starlette.responses import JSONResponse

from app.core.logging import AppLogger
from app.ingest.vendor_data_processor import VendorDataProcessor
from app.schemas.vendor import Vendor
from app.services.vendor_service import create_vendor_in_vendor_service

router = APIRouter()
logger = AppLogger().get_logger()

@router.post('/ingest_vendors_excel', response_model=List[Vendor], summary='Upload and process vendor Excel file with multiple sheets')
async def upload_excel_list(
    vendor_file: UploadFile = File(description="Excel file containing vendor data and boundary codes"),
    vendor_sheet_name: str = Form(default="Vendor Input", description="Name of the sheet containing vendor data"),
    boundary_sheet_name: str = Form(default="Boundary Code", description="Name of the sheet containing boundary codes"),
):
    temp_file = None
    try:
        temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xlsx")
        content = await vendor_file.read()
        temp_file.write(content)
        temp_file.close()
        vendor_file_path = temp_file.name

        processor = VendorDataProcessor(
            vendor_file_path=vendor_file_path,
            vendor_sheet_name=vendor_sheet_name,
            boundary_sheet_name=boundary_sheet_name
        )

        if processor.load_data():
            valid_vendors = processor.process_data()
            create_vendor_in_vendor_service(valid_vendors)
            return valid_vendors
        else:
            return JSONResponse(status_code=500, content={"message": "Error loading data from the uploaded file or sheets"})

    finally:
        if temp_file and os.path.exists(temp_file.name):
            try:
                os.unlink(temp_file.name)
            except Exception as e:
                logger.error(f"Error deleting temporary file {temp_file.name}: {e}")