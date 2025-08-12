from dataclasses import dataclass
from typing import Optional, List, Dict, Any


@dataclass
class MDMSDataResponse:
    MdmsRes: Dict[str, Any]
