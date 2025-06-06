from typing import Set

import pandas as pd

from app.ingest.service.validator import Validator


class BoundaryCodeValidator(Validator):
    def __init__(self, boundary_codes: Set[str]):
        self.valid_boundary_codes = boundary_codes

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        if "Country Boundary Code" in result_df.columns:
            boundary_mask = ~result_df["Country Boundary Code"].astype(str).isin(self.valid_boundary_codes)
            boundary_mask = boundary_mask & ~result_df["Country Boundary Code"].isna()

            if boundary_mask.any():
                result_df.loc[boundary_mask, "error"] = result_df.loc[boundary_mask, "error"].astype(
                    str) + "'Country Boundary Code' is not valid. "
                result_df.loc[boundary_mask, "status"] = "fail"

        return result_df