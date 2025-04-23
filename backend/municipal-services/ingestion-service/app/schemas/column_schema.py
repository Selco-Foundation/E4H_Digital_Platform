from dataclasses import dataclass
from typing import Optional
from app.schemas.mdms_source import MDMSSource


@dataclass
class ColumnSchema:
    name: str
    required: bool
    pattern: Optional[str] = None
    mdmsSource: Optional[MDMSSource] = None