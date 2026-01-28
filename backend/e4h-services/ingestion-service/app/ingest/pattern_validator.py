from typing import List

import pandas as pd

from app.core.logging import AppLogger
from app.ingest.service.validator import Validator
from app.schemas.vendor_ingestion_shema_response import MDMSColumn

logger = AppLogger().get_logger()


class PatternValidator(Validator):
    def __init__(self, columns: List[MDMSColumn]):
        self.pattern_columns = [col for col in columns if col.pattern]

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()

        for col_schema in self.pattern_columns:
            col_name = col_schema.name
            mandatory_col_name = col_name + " (Mandatory)"
            pattern = col_schema.pattern

            # Check if either column exists
            if col_name in result_df.columns:
                actual_col_name = col_name
            elif mandatory_col_name in result_df.columns:
                actual_col_name = mandatory_col_name
            else:
                logger.warning(f"Neither column '{col_name}' nor '{mandatory_col_name}' found in the data")
                continue

            # Apply pattern validation on the column that was found
            logger.trace(f"Validating pattern for column: {col_name}, pattern: {pattern}")
            pattern_mask = ~result_df[actual_col_name].astype(str).str.fullmatch(rf"{pattern}", na=False)
            if pattern_mask.any():
                error_count = pattern_mask.sum()
                logger.debug(f"Found {error_count} rows with pattern mismatch for column '{col_name}'")
                result_df.loc[pattern_mask, "error"] = result_df.loc[pattern_mask, "error"].astype(
                    str) + f"'{col_name}' doesn't match the pattern: {pattern}. "
                result_df.loc[pattern_mask, "status"] = "fail"
        
        failed_count = (result_df["status"] == "fail").sum()
        if failed_count > 0:
            logger.info(f"Pattern validation completed: {failed_count} rows failed validation")
        else:
            logger.debug("Pattern validation completed: all rows passed")
        return result_df