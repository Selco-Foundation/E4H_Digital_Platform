from dataclasses import dataclass
from typing import List, Dict, Any


@dataclass
class Boundary:
    country: str
    state: str
    district: str
    block: str
    code: str

    def get(self, key: str, default: Any = None) -> Any:
        attributes = {
            "country": self.country,
            "state": self.state,
            "district": self.district,
            "block": self.block,
            "code": self.code
        }
        return attributes.get(key, default)