from dataclasses import dataclass
from typing import Optional
from app.schemas.filter_type import FilterType


@dataclass
class MDMSSource:
    module: str
    master: str
    filterType: FilterType
    valueAttribute: Optional[str] = None