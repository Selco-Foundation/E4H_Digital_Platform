from typing import List

import pandas as pd

from app.core.logging import AppLogger
from app.ingest.service.validator import Validator
from app.schemas.vendor_ingestion_shema_response import MDMSColumn

logger = AppLogger().get_logger()


class RequiredFieldValidator(Validator):
    def __init__(self, columns: List[MDMSColumn]):
        self.required_columns = [col for col in columns if col.required]

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        logger.trace("Starting required field validation")
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        logger.debug(f"Validating {len(self.required_columns)} required columns against {len(result_df)} rows")
        for col_schema in self.required_columns:
            col_name = col_schema.name
            mandatory_col_name = col_name + " (Mandatory)"
            logger.trace(f"Validating required column: {col_name}")

            # Check if either column exists
            if col_name in result_df.columns:
                actual_col_name = col_name
            elif mandatory_col_name in result_df.columns:
                actual_col_name = mandatory_col_name
            else:
                logger.warning(f"Neither column '{col_name}' nor '{mandatory_col_name}' found in the data")
                continue

            # Check for null values in the column that was found
            is_required_mask = result_df[actual_col_name].isna()
            if is_required_mask.any():
                error_count = is_required_mask.sum()
                logger.debug(f"Found {error_count} missing values for required field '{col_name}'")
                result_df.loc[is_required_mask, "error"] = result_df.loc[is_required_mask, "error"].astype(
                    str) + f"'{col_name}' is a required field. "
                result_df.loc[is_required_mask, "status"] = "fail"

        failed_count = (result_df["status"] == "fail").sum()
        if failed_count > 0:
            logger.info(f"Required field validation completed: {failed_count} rows failed validation")
        else:
            logger.debug("Required field validation completed: all rows passed")

        return result_df
