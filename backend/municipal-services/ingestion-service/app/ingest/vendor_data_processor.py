from typing import List

import pandas as pd

from app.schemas.vendor import Vendor
from app.utils.vendor_validation import validate_mandatory_fields, validate_boundary_code


class VendorDataProcessor:
    def __init__(self, vendor_file_path: str, vendor_sheet_name: str, boundary_sheet_name: str):
        """
        Initialize the processor with paths to the required Excel file and sheet names.
        Args:
            vendor_file_path: Path to the Excel file containing all data.
            vendor_sheet_name: Name of the sheet containing vendor data.
            boundary_sheet_name: Name of the sheet containing boundary codes.
        """
        self.vendor_file_path = vendor_file_path
        self.vendor_sheet_name = vendor_sheet_name
        self.boundary_sheet_name = boundary_sheet_name
        self.vendors = []
        self.validation_errors = []

    def load_data(self):
        """Load data from specific sheets of the Excel file"""
        try:
            self.vendor_df = pd.read_excel(self.vendor_file_path, sheet_name=self.vendor_sheet_name)
            self.boundary_df = pd.read_excel(self.vendor_file_path, sheet_name=self.boundary_sheet_name)
            self.valid_boundary_codes = set(self.boundary_df['Country'].str.strip())

            print(f"Loaded {len(self.vendor_df)} vendor records from sheet '{self.vendor_sheet_name}'")
            print(f"Loaded {len(self.boundary_df)} boundary codes from sheet '{self.boundary_sheet_name}'")
            return True
        except FileNotFoundError as e:
            print(f"Error loading data: File not found - {str(e)}")
            return False
        except ValueError as e:
            print(f"Error loading data: Sheet not found - {str(e)}")
            return False
        except Exception as e:
            print(f"Error loading data: {str(e)}")
            return False

    def process_data(self) -> List[Vendor]:
        """Process and validate the data from the vendor Excel file and return a list of valid Vendor objects"""
        if not hasattr(self, 'vendor_df'):
            print("Data not loaded. Please call load_data() first.")
            return []

        self.vendors = []
        self.validation_errors = []

        for idx, row in self.vendor_df.iterrows():
            row_errors = []
            row_errors.extend(validate_mandatory_fields(row))

            if not pd.isna(row['Country Boundary Code']):
                boundary_error = validate_boundary_code(self, str(row['Country Boundary Code']).strip())
                if boundary_error:
                    row_errors.append(boundary_error)

            if row_errors:
                self.validation_errors.append({
                    'row': idx + 2,  # +2 because Excel is 1-indexed and has a header row
                    'vendor_name': row['Vendor Name'] if not pd.isna(row['Vendor Name']) else 'Unknown',
                    'errors': row_errors
                })
            else:
                try:
                    vendor = Vendor(
                        country_boundary_code=str(row['Country Boundary Code']).strip(),
                        vendor_name=str(row['Vendor Name (Mandatory)']).strip(),
                        vendor_code=str(row['Vendor Code (Mandatory)']).strip(),
                        vendor_type=str(row['Vendor Type (Mandatory)']).strip(),
                        vendor_subtype=str(row['Vendor Subtype ']).strip() if not pd.isna(row['Vendor Subtype ']) else None,
                        identifier_type=str(row['Identifier Type (Mandatory)']).strip(),
                        identifier_value=str(row['Identifier Value (Mandatory)']).strip(),
                        hq_address=str(row['HQ Address (Mandatory)']).strip(),
                        pincode=str(row['Pincode (Mandatory)']).strip(),
                        poc_phone=str(row['PoC Phone (Mandatory)']).strip(),
                        poc_name=str(row['PoC Name (Mandatory)']).strip()
                    )
                    self.vendors.append(vendor)
                except KeyError as e:
                    self.validation_errors.append({
                        'row': idx + 2,
                        'vendor_name': row['Vendor Name'] if not pd.isna(row['Vendor Name']) else 'Unknown',
                        'errors': [f"Missing required column: {e}"]
                    })
                except Exception as e:
                    self.validation_errors.append({
                        'row': idx + 2,
                        'vendor_name': row['Vendor Name'] if not pd.isna(row['Vendor Name']) else 'Unknown',
                        'errors': [f"Error creating Vendor object: {e}"]
                    })

        print(f"Processed {len(self.vendor_df)} vendors")
        print(f"Found {len(self.validation_errors)} vendors with validation errors")
        print(f"Successfully validated {len(self.vendors)} vendors")

        return self.vendors