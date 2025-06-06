from typing import List, Tuple, Optional

import pandas as pd

from app.ingest.excel_data_loader import ExcelDataLoader
from app.ingest.service.data_loader import DataLoader
from app.ingest.service.data_writer import DataWriter
from app.ingest.service.validator import Validator
from app.schemas.vendor import Vendor


class VendorDataProcessor:
    def __init__(self, data_loader: DataLoader, validators: List[Validator], data_writer: DataWriter):
        self.data_loader = data_loader
        self.validators = validators
        self.data_writer = data_writer
        self.vendors = []
        self.validation_errors = []

    def process_data(self) -> Tuple[List[Vendor], pd.DataFrame]:
        if not self.data_loader.load_data():
            return [], pd.DataFrame()

        if isinstance(self.data_loader, ExcelDataLoader):
            vendor_df = self.data_loader.get_vendor_data()
            print(vendor_df.head(2))
        else:
            print("Data loader is not compatible")
            return [], pd.DataFrame()

        vendor_df["status"] = "success"
        vendor_df["error"] = ""

        for validator in self.validators:
            vendor_df = validator.validate(vendor_df)

        self.validation_errors = []
        for idx, row in vendor_df[vendor_df["status"] == "fail"].iterrows():
            self.validation_errors.append({
                'row': idx + 2,
                'vendor_name': row.get('Vendor Name', 'Unknown'),
                'errors': [err for err in row.get('error', '').split(';') if err]
            })
        self.data_writer.write_data(vendor_df)

        self.vendors = []
        for _, row in vendor_df[vendor_df["status"] != "fail"].iterrows():
            try:
                vendor = self._create_vendor_from_row(row)
                if vendor:
                    self.vendors.append(vendor)
            except Exception as e:
                print(f"Error creating vendor object: {e}")
                vendor_df.at[_, "status"] = "fail"
                vendor_df.at[_, "error"] = f"{vendor_df.at[_, 'error']};Exception: {str(e)}" if vendor_df.at[_, "error"] else f"Exception: {str(e)}"

        print(f"Processed {len(vendor_df)} vendors")
        print(f"Found {len(self.validation_errors)} vendors with validation errors")
        print(f"Successfully validated {len(self.vendors)} vendors")

        return self.vendors, vendor_df

    def _create_vendor_from_row(self, row) -> Optional[Vendor]:
        try:
            return Vendor(
                country_boundary_code=str(row.get('Country Boundary Code', '')).strip() if not pd.isna(
                    row.get('Country Boundary Code', None)) else None,
                vendor_name=str(row.get('Vendor Name (Mandatory)', '')).strip(),
                vendor_code=str(row.get('Vendor Code (Mandatory)', '')).strip(),
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
            print(f"Error in vendor creation: {e}")
            return None