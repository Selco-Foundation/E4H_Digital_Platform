from typing import Set, Dict, Tuple
import pandas as pd

from app.core.logging import AppLogger
from app.ingest.service.data_loader import DataLoader

logger = AppLogger().get_logger()
class BoundaryExcelDataLoader(DataLoader):
    def __init__(self, file_path: str, boundary_sheet: str = "Boundary Data"):
        self.file_path = file_path
        self.boundary_sheet = boundary_sheet
        self.boundary_df = None
        self.unique_boundary_codes = set()

    def load_data(self) -> bool:
        try:
            # Load boundary data
            self.boundary_df = pd.read_excel(
                self.file_path,
                sheet_name=self.boundary_sheet
            )

            # Initialize tracking columns
            if "BoundaryCode" not in self.boundary_df.columns:
                self.boundary_df["BoundaryCode"] = ""
            if "status" not in self.boundary_df.columns:
                self.boundary_df["status"] = None
            if "error" not in self.boundary_df.columns:
                self.boundary_df["error"] = ""

            # Pre-process hierarchy combinations for uniqueness check
            self._preprocess_hierarchy()

            logger.info(f"Loaded {len(self.boundary_df)} boundary records")
            return True

        except Exception as e:
            logger.error(f"Error loading boundary data: {str(e)}")
            raise Exception("Failed to load boundary data from the provided file") from e

    def _preprocess_hierarchy(self):
        # Create BoundaryCode only for rows with all 4 values present
        has_all_values = (
            self.boundary_df["Country"].notna() & self.boundary_df["Country"].str.strip().ne('') &
            self.boundary_df["State"].notna() & self.boundary_df["State"].str.strip().ne('') &
            self.boundary_df["District"].notna() & self.boundary_df["District"].str.strip().ne('') &
            self.boundary_df["Block"].notna() & self.boundary_df["Block"].str.strip().ne('')
        )

        # Initialize BoundaryCode column with empty strings
        self.boundary_df["BoundaryCode"] = ""
        # CodeReview: Convert everything to small case. What happens if this becomes too long? Will we truncate?
        # Only process rows with complete data
        self.boundary_df.loc[has_all_values, "BoundaryCode"] = (
                self.boundary_df["Country"].str.strip() + "_" +
                self.boundary_df["State"].str.strip() + "_" +
                self.boundary_df["District"].str.strip() + "_" +
                self.boundary_df["Block"].str.strip()
        )

        # Create normalized hierarchy combinations for uniqueness checking
        self.unique_boundary_codes = set()
        for _, row in self.boundary_df[has_all_values].iterrows():
            combo = (
                str(row['Country']).strip(),
                str(row['State']).strip(),
                str(row['District']).strip(),
                str(row['Block']).strip()
            )
            self.unique_boundary_codes.add(combo)

    def get_boundary_data(self) -> pd.DataFrame:
        return self.boundary_df.copy() if self.boundary_df is not None else pd.DataFrame()

    def get_unique_boundary_codes(self) -> Set[Tuple[str, str, str, str]]:
        return self.unique_boundary_codes