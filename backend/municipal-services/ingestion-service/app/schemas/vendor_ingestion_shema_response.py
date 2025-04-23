from dataclasses import dataclass
from typing import List

from app.schemas.column_schema import ColumnSchema


@dataclass
class VendorIngestionSchemaResponse:
    columns: List[ColumnSchema]