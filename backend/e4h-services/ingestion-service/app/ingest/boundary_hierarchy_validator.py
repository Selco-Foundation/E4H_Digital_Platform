import re
from typing import Dict, List, Optional

import pandas as pd

from app.ingest.service.validator import Validator

# Must match BoundaryDataProcessor.to_camel_case / BoundaryExcelDataLoader.to_camel_case
_ALLOWED_COUNTRY_SEGMENT = "India"


def _boundary_segment_code(cell) -> str:
    if cell is None or (isinstance(cell, float) and pd.isna(cell)):
        return ""
    text = str(cell).strip()
    if not text:
        return ""
    cleaned = re.sub(r"[_\-]+", " ", text)
    parts = cleaned.split()
    return "".join(word[:1].upper() + word[1:] for word in parts)


def _country_column_name(df: pd.DataFrame) -> Optional[str]:
    if "Country" in df.columns:
        return "Country"
    if "Country (Mandatory)" in df.columns:
        return "Country (Mandatory)"
    return None


class BoundaryIndiaCountryValidator(Validator):
    """Boundary ingest accepts only India as the country."""

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        col = _country_column_name(result_df)
        if not col:
            return result_df

        for idx, row in result_df.iterrows():
            if row.get("status") == "fail":
                continue
            raw = row.get(col)
            segment = _boundary_segment_code(raw)
            if not segment:
                continue
            if segment != _ALLOWED_COUNTRY_SEGMENT:
                result_df.at[idx, "error"] = (
                    f"{result_df.at[idx, 'error']}"
                    f"Country must be India; '{raw}' is not allowed. "
                )
                result_df.at[idx, "status"] = "fail"

        return result_df


class BoundaryHierarchyValidator(Validator):
    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        """Fails all occurrences of duplicate non-empty BoundaryCodes"""
        result_df = data.copy()

        # Preserve existing validation state
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        # Track occurrences of each non-empty BoundaryCode
        code_occurrences: Dict[str, List[int]] = {}

        for idx, row in result_df.iterrows():
            code = str(row['BoundaryCode'])
            if code == "":  # Skip empty codes
                continue

            if code not in code_occurrences:
                code_occurrences[code] = []
            code_occurrences[code].append(idx)

        # Mark all duplicates as failed
        for code, indices in code_occurrences.items():
            if len(indices) > 1:  # Found duplicates
                for idx in indices:
                    result_df.at[idx, 'error'] = (
                        f"{result_df.at[idx, 'error']}"
                        f"Duplicate BoundaryCode '{code}'. "
                    )
                    result_df.at[idx, 'status'] = 'fail'

        return result_df