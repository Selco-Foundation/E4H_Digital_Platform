from typing import List

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

    def process_data(self) -> List[Vendor]:
        """Process and validate vendor data"""
        # Load data
        if not self.data_loader.load_data():
            return []

        # Get the dataframe from the loader
        if isinstance(self.data_loader, ExcelDataLoader):
            vendor_df = self.data_loader.get_vendor_data()
        else:
            print("Data loader is not compatible")
            return []

        # Filter failed records if status column exists
        if "status" in vendor_df.columns:
            vendor_df = vendor_df[vendor_df["status"] == "fail"].copy()

        # Initialize status and error columns
        vendor_df["status"] = None
        vendor_df["error"] = ""

        # Run all validators
        has_error = False
        for validator in self.validators:
            vendor_df = validator.validate(vendor_df)
            if (vendor_df["status"] == "fail").any():
                has_error = True

        # Collect validation errors
        self.validation_errors = []
        for idx, row in vendor_df[vendor_df["status"] == "fail"].iterrows():
            self.validation_errors.append({
                'row': idx + 2,  # +2 because Excel is 1-indexed and has a header row
                'vendor_name': row.get('Vendor Name (Mandatory)', 'Unknown'),
                'errors': [row.get('error', '')]
            })

        # Write results back to Excel
        self.data_writer.write_data(vendor_df)

        # Process valid vendors
        self.vendors = []
        for _, row in vendor_df[vendor_df["status"] != "fail"].iterrows():
            try:
                vendor = Vendor(
                    country_boundary_code=str(row.get('Country Boundary Code', '')).strip() if not pd.isna(
                        row.get('Country Boundary Code', None)) else None,
                    vendor_name=str(row.get('Vendor Name (Mandatory)', '')).strip(),
                    vendor_code=str(row.get('Vendor Code (Mandatory)', '')).strip(),
                    vendor_type=str(row.get('Vendor Type (Mandatory)', '')).strip(),
                    vendor_subtype=str(row.get('Vendor Subtype ', '')).strip() if not pd.isna(
                        row.get('Vendor Subtype ', None)) else None,
                    identifier_type=str(row.get('Identifier Type (Mandatory)', '')).strip(),
                    identifier_value=str(row.get('Identifier Value (Mandatory)', '')).strip(),
                    hq_address=str(row.get('HQ Address (Mandatory)', '')).strip(),
                    pincode=str(row.get('Pincode (Mandatory)', '')).strip(),
                    poc_phone=str(row.get('PoC Phone (Mandatory)', '')).strip(),
                    poc_name=str(row.get('PoC Name (Mandatory)', '')).strip()
                )
                self.vendors.append(vendor)
            except Exception as e:
                print(f"Error creating vendor object: {e}")

        print(f"Processed {len(vendor_df)} vendors")
        print(f"Found {len(self.validation_errors)} vendors with validation errors")
        print(f"Successfully validated {len(self.vendors)} vendors")

        return self.vendors