from typing import Set

import pandas as pd

from app.core.logging import AppLogger
from app.ingest.service.validator import Validator

logger = AppLogger().get_logger()


class BoundaryCodeValidator(Validator):
    def __init__(self, boundary_codes: Set[str]):
        self.valid_boundary_codes = boundary_codes
        logger.debug(f"Initialized BoundaryCodeValidator with {len(boundary_codes)} valid boundary codes")

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        logger.trace("Starting boundary code validation")
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        if "Country Boundary Code" in result_df.columns:
            boundary_mask = ~result_df["Country Boundary Code"].astype(str).isin(self.valid_boundary_codes)
            boundary_mask = boundary_mask & ~result_df["Country Boundary Code"].isna()

            if boundary_mask.any():
                error_count = boundary_mask.sum()
                logger.debug(f"Found {error_count} rows with invalid boundary codes")
                result_df.loc[boundary_mask, "error"] = result_df.loc[boundary_mask, "error"].astype(
                    str) + "'Country Boundary Code' is not valid. "
                result_df.loc[boundary_mask, "status"] = "fail"
        else:
            logger.debug("Country Boundary Code column not found, skipping validation")

        failed_count = (result_df["status"] == "fail").sum()
        if failed_count > 0:
            logger.info(f"Boundary code validation completed: {failed_count} rows failed validation")
        else:
            logger.debug("Boundary code validation completed: all rows passed")

        return result_df