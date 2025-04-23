from dataclasses import dataclass
from typing import Optional, List, Dict, Any

from pydantic import BaseModel


@dataclass
class MDMSDataResponse:
    MdmsRes: Dict[str, Any]
