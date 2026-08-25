"""
Helpers for assessment eligible → installation plan handoff (LLD §2.2.7, §2.2.9, API §8).

Excel does not carry planFacilityId / assessmentPlanId columns — those are resolved
server-side from Facility Id against the eligible-facilities API.
"""
from __future__ import annotations

from typing import Any, Dict, List, Optional

import pandas as pd

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
from app.utils.assessment_service_client import AssessmentServiceClient

logger = AppLogger().get_logger()


def should_use_assessment_handoff_validation(
    assessment_client: Optional[AssessmentServiceClient],
    request_info: RequestInfo,
    project_id: Optional[str],
    tenant_id: str,
) -> bool:
    """True when there are eligible, unassigned facilities ready for installation plan handoff."""
    if not project_id or assessment_client is None:
        return False
    try:
        facilities = fetch_eligible_assessment_facilities(
            assessment_client=assessment_client,
            request_info=request_info,
            project_id=project_id,
            tenant_id=tenant_id,
        )
        if facilities:
            logger.info(
                "Project %s has %s eligible assessment facility(ies) for handoff validation",
                project_id,
                len(facilities),
            )
            return True
        return False
    except Exception as exc:
        logger.warning(
            "Could not load eligible assessment facilities for project %s: %s",
            project_id,
            exc,
        )
        return False


def fetch_eligible_assessment_facilities(
    assessment_client: AssessmentServiceClient,
    request_info: RequestInfo,
    project_id: str,
    tenant_id: str,
) -> List[Dict[str, Any]]:
    response = assessment_client.search_eligible_facilities(
        request_info=request_info,
        project_id=project_id,
        tenant_id=tenant_id,
    )
    return response.get("facilities") or []


def merge_eligible_facilities_into_list(
    all_facilities: List[Dict[str, Any]],
    eligible_facilities: List[Dict[str, Any]],
    facilities_by_id: Dict[str, Dict[str, Any]],
) -> List[Dict[str, Any]]:
    """Ensure eligible assessment facilities appear even if geography filtering skipped them."""
    existing_ids = {
        str(f.get("facility_id") or f.get("facilityId"))
        for f in all_facilities
        if f.get("facility_id") or f.get("facilityId")
    }
    merged = list(all_facilities)
    for eligible in eligible_facilities:
        facility_id = eligible.get("facilityId")
        if not facility_id or str(facility_id) in existing_ids:
            continue
        facility = dict(facilities_by_id.get(facility_id) or {})
        if not facility.get("facility_id"):
            facility["facility_id"] = facility_id
        facility["include_in_fieldplan"] = "No"
        merged.append(facility)
        existing_ids.add(str(facility_id))
    return merged


def restrict_to_eligible_and_linked_facilities(
    all_facilities: List[Dict[str, Any]],
    eligible_facilities: List[Dict[str, Any]],
    fieldplan_linked_facility_ids: Optional[set] = None,
) -> List[Dict[str, Any]]:
    """
    After a closed assessment, the installation plan sheet is the eligible pool only
    (plus rows already on this installation plan). Not Eligible / unassessed HFs are excluded.
    """
    keep_ids = {
        str(entry.get("facilityId"))
        for entry in eligible_facilities
        if entry.get("facilityId")
    }
    if fieldplan_linked_facility_ids:
        keep_ids.update(str(fid) for fid in fieldplan_linked_facility_ids if fid)
    if not keep_ids:
        return all_facilities
    return [
        facility
        for facility in all_facilities
        if str(facility.get("facility_id") or facility.get("facilityId") or "") in keep_ids
    ]


def load_eligible_facility_map(
    assessment_client: AssessmentServiceClient,
    request_info: RequestInfo,
    project_id: str,
    tenant_id: str,
) -> Dict[str, Dict[str, Any]]:
    """Map facilityId → eligible facility record (all closed plans in project)."""
    facilities = fetch_eligible_assessment_facilities(
        assessment_client=assessment_client,
        request_info=request_info,
        project_id=project_id,
        tenant_id=tenant_id,
    )
    return {
        str(f.get("facilityId")): f
        for f in facilities
        if f.get("facilityId") and f.get("planFacilityId")
    }


def find_column(df: pd.DataFrame, partial: str) -> Optional[str]:
    partial_lower = partial.lower()
    for col in df.columns:
        if partial_lower in str(col).lower():
            return col
    return None


def validate_assessment_handoff_rows(
    df: pd.DataFrame,
    eligible_by_facility_id: Dict[str, Dict[str, Any]],
) -> List[List[str]]:
    """
    Validate Include=Yes rows that resolve to an assessment handoff.

    Handoff is detected by Facility Id matching the eligible pool (server-side).
    Non-eligible Include=Yes rows stay on the legacy installation plan path (no error).
    """
    df = df.reset_index(drop=True)
    errors: List[List[str]] = [[] for _ in range(len(df))]
    if not eligible_by_facility_id:
        return errors

    facility_id_col = find_column(df, "facility id")
    include_col = find_column(df, "included in field plan")

    for i, row in df.iterrows():
        include_val = ""
        if include_col:
            include_val = str(row.get(include_col, "")).strip().lower()
        elif "Included in Installation Plan (Mandatory)" in df.columns:
            include_val = str(row.get("Included in Installation Plan (Mandatory)", "")).strip().lower()
        if include_val != "yes":
            continue

        facility_id = ""
        if facility_id_col:
            raw = row.get(facility_id_col)
            if pd.notna(raw) and str(raw).strip():
                facility_id = str(raw).strip()
        if not facility_id:
            continue

        # Only rows in the eligible pool are assessment handoffs; others are legacy includes.
        if facility_id not in eligible_by_facility_id:
            continue

        meta = eligible_by_facility_id[facility_id]
        if not meta.get("planFacilityId"):
            errors[i].append(
                f"ASSESSMENT_FACILITY_NOT_ELIGIBLE: facilityId '{facility_id}' "
                "is missing planFacilityId for handoff"
            )

    return errors


def merge_assessment_validation_errors(
    base_errors: List[List[str]],
    assessment_errors: List[List[str]],
) -> List[List[str]]:
    merged: List[List[str]] = []
    for i in range(max(len(base_errors), len(assessment_errors))):
        row_errors = []
        if i < len(base_errors):
            row_errors.extend(base_errors[i])
        if i < len(assessment_errors):
            row_errors.extend(assessment_errors[i])
        merged.append(row_errors)
    return merged


def resolve_plan_facility_id_for_handoff(
    facility_id: Optional[str],
    eligible_by_facility_id: Dict[str, Dict[str, Any]],
) -> Optional[str]:
    """Resolve planFacilityId from Facility Id against the eligible pool (no Excel column)."""
    if not facility_id or not eligible_by_facility_id:
        return None
    meta = eligible_by_facility_id.get(str(facility_id).strip())
    if not meta:
        return None
    plan_facility_id = meta.get("planFacilityId")
    return str(plan_facility_id).strip() if plan_facility_id else None
