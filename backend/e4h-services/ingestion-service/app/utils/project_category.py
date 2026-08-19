from typing import List, Optional, Set

from app.core.logging import AppLogger

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

        suffix = project_type_name.rsplit(" - ", 1)[-1].strip().upper()
        category = _CATEGORY_SUFFIX_MAP.get(suffix)
        if not category:
            logger.warning(
                f"resolve_project_category: unrecognized ProjectType category suffix '{suffix}' "
                f"for projectType '{project_type_name}'"
            )
        return category
    except Exception as e:
        logger.warning(f"resolve_project_category: failed for project {project_id}: {e}")
        return None


def filter_facilities_by_category(
        facilities: List[dict],
        category: Optional[str],
        already_linked_facility_ids: Optional[Set[str]] = None,
) -> List[dict]:
    """
    Restrict `facilities` to those matching `category`, except facilities already linked
    (to a project/field-plan) which stay visible regardless of category so existing
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
