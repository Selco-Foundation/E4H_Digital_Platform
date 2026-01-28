import pandas as pd

from app.core.logging import AppLogger
from app.ingest.service.validator import Validator

logger = AppLogger().get_logger()


class IdentifierValidator(Validator):

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        logger.trace("Starting identifier validation")
        result_df = data.copy()

        if "Identifier Type" in result_df.columns:
            valid_identifiers = ["GSTIN", "PAN"]
            invalid_identifier_mask = ~result_df["Identifier Type"].astype(str).isin(valid_identifiers)

            if invalid_identifier_mask.any():
                error_count = invalid_identifier_mask.sum()
                logger.debug(f"Found {error_count} rows with invalid identifier type")
                result_df.loc[invalid_identifier_mask, "error"] = result_df.loc[
                                                                      invalid_identifier_mask, "error"].astype(
                    str) + "'Identifier Type' must be either 'GSTIN' or 'PAN'. "
                result_df.loc[invalid_identifier_mask, "status"] = "fail"
        else:
            logger.debug("Identifier Type column not found, skipping validation")

        failed_count = (result_df["status"] == "fail").sum()
        if failed_count > 0:
            logger.info(f"Identifier validation completed: {failed_count} rows failed validation")
        else:
            logger.debug("Identifier validation completed: all rows passed")
        return result_df