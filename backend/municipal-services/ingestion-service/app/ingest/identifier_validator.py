import pandas as pd

from app.ingest.service.validator import Validator


# CodeReview: Add regex pattern validation for each identifier type
# CodeReview: Consider implementing a validation rule engine for extensibility
# CodeReview: Add validation for international identifiers
class IdentifierValidator(Validator):
    #def __init__(self):
        # CodeReview: Load these patterns from configuration
        # self.validation_patterns = {
        #     "GSTIN": r'^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$',
        #     "PAN": r'^[A-Z]{5}[0-9]{4}[A-Z]{1}$'
        # }

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()

        if "Identifier Type" in result_df.columns:
            #CodeReview: These identifiers should be configured in MDMS and fetched from there. Not defined here. They are part of the vendor registry
            valid_identifiers = ["GSTIN", "PAN"]
            invalid_identifier_mask = ~result_df["Identifier Type"].astype(str).isin(valid_identifiers)

            if invalid_identifier_mask.any():
                result_df.loc[invalid_identifier_mask, "error"] = result_df.loc[
                                                                      invalid_identifier_mask, "error"].astype(
                    str) + "'Identifier Type' must be either 'GSTIN' or 'PAN'. "
                result_df.loc[invalid_identifier_mask, "status"] = "fail"

        print(result_df.head(2))
        return result_df