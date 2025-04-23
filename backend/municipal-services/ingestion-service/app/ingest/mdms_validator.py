from typing import List

import pandas as pd

from app.ingest.service.validator import Validator
from app.schemas.column_schema import ColumnSchema
from app.schemas.filter_type import FilterType
from app.utils.mdms_client import MDMSClient
from app.utils.mdms_data_transformation import transform_mdms_data


class MDMSValidator(Validator):
    def __init__(self, columns: List[ColumnSchema], mdms_client: MDMSClient):
        self.mdms_columns = [col for col in columns if col.mdmsSource]
        self.mdms_client = mdms_client
        self.column_source_map = {}
        self._fetch_validation_data()

    def _fetch_validation_data(self):
        for column in self.mdms_columns:
            if column.mdmsSource:
                mdms_data = self.mdms_client.fetch_mdms_data(
                    column.mdmsSource.module,
                    column.mdmsSource.master
                )
                self.column_source_map[column.name] = transform_mdms_data(mdms_data, column)

    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        result_df = data.copy()
        result_df["status"] = result_df.get("status", None)
        result_df["error"] = result_df.get("error", "")

        for col_schema in self.mdms_columns:
            col_name = col_schema.name

            if col_name not in result_df.columns or col_name not in self.column_source_map:
                continue

            source_data = self.column_source_map[col_name]["values"]

            if col_schema.mdmsSource.filterType == FilterType.ONE_OF:
                value_mask = ~result_df[col_name].astype(str).isin(source_data)
                if value_mask.any():
                    result_df.loc[value_mask, "error"] = result_df.loc[value_mask, "error"].astype(
                        str) + f"'{col_name}' doesn't match the allowed values: {source_data}. "
                    result_df.loc[value_mask, "status"] = "fail"

        return result_df