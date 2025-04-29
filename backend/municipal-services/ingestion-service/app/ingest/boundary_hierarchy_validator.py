from typing import List, Dict
import pandas as pd
from app.ingest.service.validator import Validator


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