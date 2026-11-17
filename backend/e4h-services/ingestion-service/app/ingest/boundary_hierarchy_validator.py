from typing import List, Dict
import pandas as pd
from app.ingest.service.validator import Validator


class BoundaryHierarchyValidator(Validator):
    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        """Keeps the first row per non-empty BoundaryCode; later duplicates are skipped (not failed)."""
        result_df = data.copy()

        # Preserve existing validation state
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        # Track row order for each non-empty BoundaryCode (first row in file wins)
        code_occurrences: Dict[str, List[int]] = {}

        for idx, row in result_df.iterrows():
            raw = row["BoundaryCode"]
            if pd.isna(raw) or str(raw).strip() == "":
                continue
            code = str(raw).strip()

            if code not in code_occurrences:
                code_occurrences[code] = []
            code_occurrences[code].append(idx)

        # Mark rows after the first occurrence as skipped (excluded from creation path)
        for code, indices in code_occurrences.items():
            if len(indices) <= 1:
                continue
            for idx in indices[1:]:
                prev_err = str(result_df.at[idx, "error"] or "").strip()
                dup_msg = f"Duplicate BoundaryCode '{code}'; ignored (earlier identical row)."
                result_df.at[idx, "error"] = f"{prev_err + ' ' if prev_err else ''}{dup_msg}"
                result_df.at[idx, "status"] = "skipped"

        return result_df