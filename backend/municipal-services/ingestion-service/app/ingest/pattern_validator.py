from typing import List

import pandas as pd

from app.ingest.service.validator import Validator
from app.schemas.column_schema import ColumnSchema


class PatternValidator(Validator):
    def __init__(self, columns: List[ColumnSchema]):
        self.pattern_columns = [col for col in columns if col.pattern]

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        for col_schema in self.pattern_columns:
            col_name = col_schema.name
            pattern = col_schema.pattern

            if col_name not in result_df.columns:
                print(f"Warning: Column '{col_name}' not found in the data.")
                continue

            pattern_mask = ~result_df[col_name].astype(str).str.fullmatch(rf"{pattern}", na=False)
            if pattern_mask.any():
                result_df.loc[pattern_mask, "error"] = result_df.loc[pattern_mask, "error"].astype(
                    str) + f"'{col_name}' doesn't match the pattern: {pattern}. "
                result_df.loc[pattern_mask, "status"] = "fail"

        return result_df