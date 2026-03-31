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


def flatten_boundaries(boundary_json: Dict) -> List[Boundary]:
    """Recursively flatten nested boundary JSON into Boundary dataclasses."""
    boundaries: List[Boundary] = []

    def traverse(node, country="", state="", district="", block=""):
        node_type = node.get("type")
        name = node.get("name")
        code = node.get("boundaryCode")

        if node_type == "country":
            country = name
        elif node_type == "state":
            state = name
        elif node_type == "district":
            district = name
        elif node_type == "block":
            block = name

        if node_type == "block":  # leaf node → create Boundary row
            boundaries.append(
                Boundary(
                    country=country,
                    state=state,
                    district=district,
                    block=block,
                    code=code,
                )
            )

        for child in node.get("children", []):
            traverse(child, country, state, district, block)

    traverse(boundary_json)
    return boundaries