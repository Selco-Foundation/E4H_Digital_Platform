from typing import Set
from typing import Optional, Dict, Any

# CodeReview: Consider adding type validation for Excel files to prevent security issues
# CodeReview: Add memory usage optimization for large files
import pandas as pd

from app.core.logging import AppLogger
from app.ingest.service.data_loader import DataLoader
from app.utils.file_utils import validate_excel_file  # TODO: Create this utility


class ExcelDataLoader(DataLoader):
    def __init__(self, file_path: str, vendor_sheet: str, boundary_sheet: str):
        self.file_path = file_path
        self.vendor_sheet = vendor_sheet
        self.boundary_sheet = boundary_sheet
        self.vendor_df = None
        self.boundary_df = None
        self.valid_boundary_codes = set()

    def load_data(self) -> bool:
        try:
            self.vendor_df = pd.read_excel(self.file_path, sheet_name=self.vendor_sheet)
            self.boundary_df = pd.read_excel(self.file_path, sheet_name=self.boundary_sheet)
            self.valid_boundary_codes = set(self.boundary_df['Country'].str.strip())
            if "status" not in self.vendor_df.columns:
                self.vendor_df["status"] = None
            if "error" not in self.vendor_df.columns:
                self.vendor_df["error"] = ""

            print(f"Loaded {len(self.vendor_df)} vendor records from sheet '{self.vendor_sheet}'")
            print(f"Loaded {len(self.boundary_df)} boundary codes from sheet '{self.boundary_sheet}'")
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

    def get_vendor_data(self) -> pd.DataFrame:
        return self.vendor_df.copy() if self.vendor_df is not None else pd.DataFrame()

    def get_boundary_codes(self) -> Set[str]:
        return self.valid_boundary_codes

    def set_vendor_data(self, df: pd.DataFrame) -> None:
        """Allow updating the vendor data with a new DataFrame"""
        self.vendor_df = df
