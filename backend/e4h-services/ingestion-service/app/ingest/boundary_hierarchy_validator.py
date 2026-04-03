from typing import List, Dict
import pandas as pd

from app.core.logging import AppLogger
from app.ingest.service.validator import Validator

logger = AppLogger().get_logger()


class BoundaryHierarchyValidator(Validator):
    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        """Fails all occurrences of duplicate non-empty BoundaryCodes"""
        logger.trace("Starting boundary hierarchy validation")
        result_df = data.copy()

        # Preserve existing validation state
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        # Track occurrences of each non-empty BoundaryCode
        code_occurrences: Dict[str, List[int]] = {}

        logger.debug(f"Checking {len(result_df)} rows for duplicate boundary codes")
        for idx, row in result_df.iterrows():
            code = str(row['BoundaryCode'])
            if code == "":  # Skip empty codes
                continue

            if code not in code_occurrences:
                code_occurrences[code] = []
            code_occurrences[code].append(idx)

        # Mark all duplicates as failed
        duplicate_count = 0
        for code, indices in code_occurrences.items():
            if len(indices) > 1:  # Found duplicates
                duplicate_count += len(indices)
                logger.debug(f"Found duplicate BoundaryCode '{code}' in {len(indices)} rows")
                for idx in indices:
                    result_df.at[idx, 'error'] = (
                        f"{result_df.at[idx, 'error']}"
                        f"Duplicate BoundaryCode '{code}'. "
                    )
                    result_df.at[idx, 'status'] = 'fail'

        if duplicate_count > 0:
            logger.info(f"Boundary hierarchy validation completed: {duplicate_count} rows with duplicate codes")
        else:
            logger.debug("Boundary hierarchy validation completed: no duplicates found")

        return result_df