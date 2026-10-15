"""The geography a plan covers, and whether a facility falls inside it.

Pure: nothing here calls a service. Both functions exist so the template download and anything
that later needs the same rule share one definition instead of each writing its own prefix test.
"""
from typing import Any, Dict, List, Optional, Sequence

from app.core.logging import AppLogger

logger = AppLogger().get_logger()

# Most specific first: a plan's blocks describe its geography exactly, and the coarser levels are
# only ever a fallback for a record that never captured them. `state` is the scalar an assessment
# plan stores alongside geographyDetails.
_GEOGRAPHY_LEVELS = ("blocks", "districts", "states", "state")


def _boundary_code(entry: Any) -> str:
    """One boundary code, from either shape a geographyDetails list can hold.

    The platform emits these as flat code strings in some places and as {"code": ..., "name": ...}
    objects in others, and both reach here. Normalising costs a line; guessing wrong yields an
    empty code list, which would read as "this plan covers nowhere".
    """
    if isinstance(entry, str):
        return entry.strip()
    if isinstance(entry, dict):
        return str(entry.get("code") or "").strip()
    return ""


def geography_scope_codes(geography_details: Optional[Dict[str, Any]]) -> List[str]:
    """The boundary codes a geographyDetails object covers, at the most specific level it recorded.

    Returns [] -- and logs -- when the record carries no usable geography at all, which callers
    must treat as "cannot check" rather than "covers nothing": a download that silently returns
    no rows reads as a broken button, while one with too many rows is recoverable.
    """
    geography = geography_details or {}
    if not isinstance(geography, dict):
        logger.warning(f"geographyDetails is {type(geography).__name__}, not an object")
        return []

    for level in _GEOGRAPHY_LEVELS:
        value = geography.get(level)
        if value is None:
            continue
        # `state` is a scalar on an assessment plan; every other level is a list.
        entries = value if isinstance(value, (list, tuple, set)) else [value]
        codes = [code for code in (_boundary_code(e) for e in entries) if code]
        if codes:
            return codes

    logger.warning(
        f"geographyDetails has no boundary codes at any of {_GEOGRAPHY_LEVELS}; "
        f"keys present: {sorted(geography)}"
    )
    return []


def is_within_scope(boundary_code: str, scope_codes: Sequence[str]) -> bool:
    """Whether a boundary code sits at or below one of these boundaries.

    Boundary codes are hierarchical ("India_Karnataka_Mysuru_HD_Kote"), and a facility's own code
    is its block's, sometimes with a site-specific segment appended, so a district is matched by
    prefix rather than by equality. The trailing underscore is what stops HD_Kote matching
    HD_Kote2.
    """
    code = (boundary_code or "").strip()
    if not code:
        return False
    # Callers pass whatever they were given, and a None in the list must not take the request down.
    ancestors = [str(a).strip() for a in scope_codes if a]
    return any(
        code == ancestor or code.startswith(ancestor + "_")
        for ancestor in ancestors if ancestor
    )
