from typing import List, Optional, Set

import pandas as pd

from app.core.logging import AppLogger
from app.utils.facility_validator import normalize_facility_category_value

logger = AppLogger().get_logger()

# project.projectType is stored as the MDMS ProjectType record's `name`, not its `code`
# (e.g. "DRE for Anganwadis - Anganwadis"), so the category is read directly off that string -
# no MDMS lookup needed. The suffix after the last " - " is title-cased and plural for
# Anganwadi, while facility_category values are upper-cased and singular ("HEALTH"/"ANGANWADI").
_CATEGORY_SUFFIX_MAP = {
    "HEALTH": "HEALTH",
    "ANGANWADI": "ANGANWADI",
    "ANGANWADIS": "ANGANWADI",
}


def category_from_project_type_name(project_type_name: Optional[str]) -> Optional[str]:
    """
    Derive the facility category ("HEALTH"/"ANGANWADI") from a project's projectType name.
    Returns None for a missing or unrecognized suffix.
    """
    if not project_type_name:
        return None
    suffix = project_type_name.rsplit(" - ", 1)[-1].strip().upper()
    category = _CATEGORY_SUFFIX_MAP.get(suffix)
    if not category:
        logger.warning(
            f"category_from_project_type_name: unrecognized ProjectType category suffix '{suffix}' "
            f"for projectType '{project_type_name}'"
        )
    return category


def resolve_project_category(project_client, request_info, project_id: str) -> Optional[str]:
    """
    Resolve a project's facility category ("HEALTH"/"ANGANWADI") from its projectType. Returns
    None when it can't be determined (project not found, projectType missing, or an unrecognized
    name suffix) - callers must treat None as "don't filter", same fail-open posture as every
    other external service lookup in this file.
    """
    if not project_id:
        return None
    try:
        projects = project_client.search_project(request_info, project_id)
        project_list = projects.get("Project", []) if projects else []
        if not project_list:
            logger.warning(f"resolve_project_category: no project found for id {project_id}")
            return None

        project_type_name = project_list[0].get("project", {}).get("projectType")
        if not project_type_name:
            logger.warning(f"resolve_project_category: project {project_id} has no projectType")
            return None

        return category_from_project_type_name(project_type_name)
    except Exception as e:
        logger.warning(f"resolve_project_category: failed for project {project_id}: {e}")
        return None


def validate_facility_category_matches_project(
        df: pd.DataFrame,
        project_category: Optional[str],
) -> List[List[str]]:
    """
    Per-row check: the uploaded 'Category of Facility' must match the project's own category
    (HEALTH/ANGANWADI) - enforces, at validation time, the same category restriction already
    applied when generating the facility template, in case a row was hand-edited afterward.
    Blank category cells are left to the mandatory-field validation elsewhere; this only flags
    a genuine mismatch when both values are present.
    """
    df = df.reset_index(drop=True)
    errors: List[List[str]] = [[] for _ in range(len(df))]
    if not project_category:
        return errors
    for i, row in df.iterrows():
        row_category = normalize_facility_category_value(row)
        if row_category and row_category != project_category:
            errors[i].append(
                f"Category of Facility '{row_category}' does not match the project type category '{project_category}'"
            )
    return errors


def filter_facilities_by_category(
        facilities: List[dict],
        category: Optional[str],
        already_linked_facility_ids: Optional[Set[str]] = None,
) -> List[dict]:
    """
    Restrict `facilities` to those matching `category`, except facilities already linked
    (to a project/installation plan) which stay visible regardless of category so existing
    associations never silently disappear from the template.
    """
    if not category:
        return facilities
    already_linked = already_linked_facility_ids or set()
    return [
        f for f in facilities
        if f.get("facility_id") in already_linked
        or str(f.get("facility_category") or "").strip().upper() == category
    ]
