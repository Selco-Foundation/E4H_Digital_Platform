from typing import List, Tuple, Optional

import pandas as pd

from app.core.logging import AppLogger
from app.ingest.excel_data_loader import ExcelDataLoader
from app.ingest.service.data_loader import DataLoader
from app.ingest.service.data_writer import DataWriter
from app.ingest.service.validator import Validator
from app.schemas.vendor import Vendor

logger = AppLogger().get_logger()


class VendorDataProcessor:
    def __init__(self, data_loader: DataLoader, validators: List[Validator], data_writer: DataWriter):
        self.data_loader = data_loader
        self.validators = validators
        self.data_writer = data_writer
        self.vendors = []
        self.validation_errors = []

    def process_data(self) -> Tuple[List[Vendor], pd.DataFrame]:
        logger.trace("Starting vendor data processing")
        
        if not self.data_loader.load_data():
            logger.warning("Data loader failed to load data")
            return [], pd.DataFrame()

        if isinstance(self.data_loader, ExcelDataLoader):
            vendor_df = self.data_loader.get_vendor_data()
            logger.debug(f"Loaded vendor data: {len(vendor_df)} rows")
            logger.trace(f"Vendor data sample: {vendor_df.head(2).to_dict()}")
        else:
            logger.error("Data loader is not compatible with ExcelDataLoader")
            return [], pd.DataFrame()

        logger.info("Initializing vendor data processing")
        vendor_df["status"] = "success"
        vendor_df["error"] = ""

        logger.debug(f"Running {len(self.validators)} validators")
        for validator in self.validators:
            logger.trace(f"Running validator: {validator.__class__.__name__}")
            vendor_df = validator.validate(vendor_df)

        logger.info("Collecting validation errors")
        self.validation_errors = []
        for idx, row in vendor_df[vendor_df["status"] == "fail"].iterrows():
            self.validation_errors.append({
                'row': idx + 2,
                'vendor_name': row.get('Vendor Name', 'Unknown'),
                'errors': [err for err in row.get('error', '').split(';') if err]
            })
        
        logger.debug(f"Found {len(self.validation_errors)} validation errors")
        logger.trace("Writing validated data")
        self.data_writer.write_data(vendor_df)

        logger.info("Creating vendor objects from validated data")
        self.vendors = []
        for _, row in vendor_df[vendor_df["status"] != "fail"].iterrows():
            try:
                logger.trace(f"Creating vendor object for row {_ + 2}")
                vendor = self._create_vendor_from_row(row)
                if vendor:
                    self.vendors.append(vendor)
            except Exception as e:
                logger.error(f"Error creating vendor object for row {_ + 2}: {e}", exc_info=True)
                vendor_df.at[_, "status"] = "fail"
                vendor_df.at[_, "error"] = f"{vendor_df.at[_, 'error']};Exception: {str(e)}" if vendor_df.at[_, "error"] else f"Exception: {str(e)}"

        logger.info(f"Vendor processing completed: {len(vendor_df)} total, {len(self.validation_errors)} errors, {len(self.vendors)} validated")
        logger.debug(f"Processing summary: total={len(vendor_df)}, errors={len(self.validation_errors)}, validated={len(self.vendors)}")

        return self.vendors, vendor_df

    def _create_vendor_from_row(self, row) -> Optional[Vendor]:
        logger.trace("Creating vendor object from row data")
        try:
            vendor_name = str(row.get('Vendor Name (Mandatory)', '')).strip()
            logger.debug(f"Creating vendor: {vendor_name}")
            return Vendor(
                country_boundary_code=str(row.get('Country Boundary Code', '')).strip() if not pd.isna(
                    row.get('Country Boundary Code', None)) else None,
                vendor_name=vendor_name,
                vendor_type=str(row.get('Vendor Type (Mandatory)', '')).strip(),
                vendor_subtype=str(row.get('Vendor Subtype', '')).strip() if not pd.isna(
                    row.get('Vendor Subtype', None)) else None,
                identifier_type=str(row.get('Identifier Type (Mandatory)', '')).strip(),
                identifier_value=str(row.get('Identifier Value (Mandatory)', '')).strip(),
                hq_address=str(row.get('HQ Address (Mandatory)', '')).strip(),
                pincode=str(row.get('Pincode (Mandatory)', '')).strip(),
                poc_phone=str(row.get('PoC Phone (Mandatory)', '')).strip(),
                poc_name=str(row.get('PoC Name (Mandatory)', '')).strip()
            )
        except Exception as e:
            logger.error(f"Error in vendor creation: {e}", exc_info=True)
            return None