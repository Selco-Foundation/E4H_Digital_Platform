from typing import List

import pandas as pd

from app.ingest.service.validator import Validator
from app.schemas.vendor_ingestion_shema_response import MDMSColumn


class RequiredFieldValidator(Validator):
    def __init__(self, columns: List[MDMSColumn]):
        self.required_columns = [col for col in columns if col.required]

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        for col_schema in self.required_columns:
            col_name = col_schema.name
            mandatory_col_name = col_name + " (Mandatory)"

            # Check if either column exists
            if col_name in result_df.columns:
                actual_col_name = col_name
            elif mandatory_col_name in result_df.columns:
                actual_col_name = mandatory_col_name
            else:
                print(f"Warning: Neither column '{col_name}' nor '{mandatory_col_name}' found in the data.")
                continue

            # Check for null values in the column that was found
            is_required_mask = result_df[actual_col_name].isna()
            if is_required_mask.any():
                result_df.loc[is_required_mask, "error"] = result_df.loc[is_required_mask, "error"].astype(
                    str) + f"'{col_name}' is a required field. "
                result_df.loc[is_required_mask, "status"] = "fail"
            print(result_df.head())

        return result_df


class RequiredColumnValidator(Validator):
    """Fails every row when the sheet lacks a column the schema marks required.
    RequiredFieldValidator only checks the cells of columns that are present, so a sheet from an
    older template - missing a column added to the schema since - would otherwise pass."""

    def __init__(self, columns: List[MDMSColumn]):
        self.required_columns = [col.name for col in columns if col.required and col.name]

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")
        missing = [name for name in self.required_columns
                   if name not in result_df.columns and name + " (Mandatory)" not in result_df.columns]
        if missing:
            result_df["error"] = result_df["error"].fillna("").astype(str) + (
                "Missing column(s) %s - download the latest template. "
                % ", ".join("'%s'" % name for name in missing))
            result_df["status"] = "fail"
        return result_df
