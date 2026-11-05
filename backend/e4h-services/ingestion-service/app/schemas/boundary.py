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


def select_boundaries_by_codes(boundaries: List[Boundary], codes) -> List[Boundary]:
    """The blocks lying under any of `codes` - each a state, district or block code, as the
    geography step's `boundary_codes` sends them. `get_all_boundaries` rows hold codes at every
    level, so each level is compared directly."""
    wanted = {code for code in codes if code}
    return [b for b in boundaries
            if b.block in wanted or b.district in wanted or b.state in wanted]


def flatten_boundary_codes(boundary_json: Dict) -> List[Boundary]:
    """Like `flatten_boundaries`, but every level holds its boundary CODE, not its name.

    This is the shape `get_all_boundaries` returns and `_format_boundary_data` localizes
    (`Boundary_<code>`), so a geography-filtered template is built exactly like the
    unfiltered one - whatever the caller put in each node's `name`.
    """
    boundaries: List[Boundary] = []

    def traverse(node, country="", state="", district=""):
        node_type = node.get("type")
        code = node.get("boundaryCode") or ""

        if node_type == "country":
            country = code
        elif node_type == "state":
            state = code
        elif node_type == "district":
            district = code
        elif node_type == "block":
            boundaries.append(Boundary(country=country, state=state, district=district,
                                       block=code, code=code))

        for child in node.get("children", []) or []:
            traverse(child, country, state, district)

    traverse(boundary_json)
    return boundaries