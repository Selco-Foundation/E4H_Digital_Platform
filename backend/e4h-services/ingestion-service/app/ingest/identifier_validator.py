import pandas as pd

from app.ingest.service.validator import Validator


class IdentifierValidator(Validator):

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()

        if "Identifier Type" in result_df.columns:
            valid_identifiers = ["GSTIN", "PAN"]
            invalid_identifier_mask = ~result_df["Identifier Type"].astype(str).isin(valid_identifiers)

            if invalid_identifier_mask.any():
                result_df.loc[invalid_identifier_mask, "error"] = result_df.loc[
                                                                      invalid_identifier_mask, "error"].astype(
                    str) + "'Identifier Type' must be either 'GSTIN' or 'PAN'. "
                result_df.loc[invalid_identifier_mask, "status"] = "fail"

        print(result_df.head(2))
        return result_df