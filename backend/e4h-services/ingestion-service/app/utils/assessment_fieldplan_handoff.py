"""
Helpers for assessment eligible → installation field plan handoff (LLD §2.2.7, §2.2.9, API §8).
"""
from __future__ import annotations

import json
from typing import Any, Dict, List, Optional, Tuple

import pandas as pd

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
from app.utils.assessment_service_client import AssessmentServiceClient

logger = AppLogger().get_logger()

PLAN_FACILITY_ID_COLUMN = "Plan Facility Id"
ASSESSMENT_PLAN_ID_COLUMN = "Assessment Plan Id"
ASSESSMENT_PLAN_NAME_COLUMN = "Assessment Plan Name"


def parse_assessment_plan_ids(raw: Optional[Any]) -> List[str]:
    if raw is None:
        return []
    if isinstance(raw, list):
        return [str(x).strip() for x in raw if str(x).strip()]
    if not str(raw).strip():
        return []
    value = str(raw).strip()
    if value.startswith("["):
        parsed = json.loads(value)
        return [str(x).strip() for x in parsed if str(x).strip()]
    return [part.strip() for part in value.split(",") if part.strip()]


def load_eligible_facility_map(
    assessment_client: AssessmentServiceClient,
    request_info: RequestInfo,
    project_id: str,
    tenant_id: str,
    assessment_plan_ids: List[str],
) -> Dict[str, Dict[str, Any]]:
    """Map planFacilityId → eligible facility record."""
    response = assessment_client.search_eligible_facilities(
        request_info=request_info,
        project_id=project_id,
        tenant_id=tenant_id,
        assessment_plan_ids=assessment_plan_ids,
    )
    facilities = response.get("facilities") or []
    return {
        str(f.get("planFacilityId")): f
        for f in facilities
        if f.get("planFacilityId")
    }


def find_column(df: pd.DataFrame, partial: str) -> Optional[str]:
    partial_lower = partial.lower()
    for col in df.columns:
        if partial_lower in str(col).lower():
            return col
    return None


def validate_assessment_handoff_rows(
    df: pd.DataFrame,
    eligible_by_plan_facility_id: Dict[str, Dict[str, Any]],
) -> List[List[str]]:
    """Return per-row validation error messages (empty list = passed)."""
    df = df.reset_index(drop=True)
    errors: List[List[str]] = [[] for _ in range(len(df))]

    plan_facility_col = find_column(df, "plan facility id")
    include_col = find_column(df, "included in field plan")

    if not plan_facility_col:
        for i in range(len(df)):
            errors[i].append("Missing mandatory column: Plan Facility Id")
        return errors

    for i, row in df.iterrows():
        include_val = ""
        if include_col:
            include_val = str(row.get(include_col, "")).strip().lower()
        elif "Included in Field Plan (Mandatory)" in df.columns:
            include_val = str(row.get("Included in Field Plan (Mandatory)", "")).strip().lower()
        if include_val != "yes":
            continue

        plan_facility_id = str(row.get(plan_facility_col, "")).strip()
        if not plan_facility_id or plan_facility_id.lower() in ("nan", "none"):
            errors[i].append("Plan Facility Id is required for rows marked Included in Field Plan = Yes")
            continue

        if plan_facility_id not in eligible_by_plan_facility_id:
            errors[i].append(
                f"ASSESSMENT_FACILITY_NOT_ELIGIBLE: planFacilityId '{plan_facility_id}' "
                "is not ELIGIBLE or already handed off"
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


def build_assessment_fieldplan_template_rows(
    eligible_facilities: List[Dict[str, Any]],
    facilities_by_id: Dict[str, Dict[str, Any]],
) -> List[Dict[str, Any]]:
    rows: List[Dict[str, Any]] = []
    for eligible in eligible_facilities:
        facility_id = eligible.get("facilityId")
        facility = facilities_by_id.get(facility_id, {})
        rows.append({
            PLAN_FACILITY_ID_COLUMN: eligible.get("planFacilityId", ""),
            ASSESSMENT_PLAN_ID_COLUMN: eligible.get("assessmentPlanId", ""),
            ASSESSMENT_PLAN_NAME_COLUMN: eligible.get("assessmentPlanName", ""),
            "Facility Id": facility_id or "",
            "Health Centre Name (Mandatory)": (
                eligible.get("facilityName")
                or facility.get("facility_name")
                or facility.get("facilityName")
                or ""
            ),
            "Category of Facility (Mandatory)": facility.get("facility_category") or facility.get("facilityCategory") or "",
            "Type of HC (Mandatory)": facility.get("facility_type") or facility.get("facilityType") or "",
            "Boundary Code (Mandatory)": facility.get("boundary_code") or facility.get("boundaryCode") or "",
            "Included in Field Plan (Mandatory)": "",
        })
    return rows


def extract_assessment_link_meta(row: pd.Series, df: pd.DataFrame) -> Tuple[Optional[str], Optional[str]]:
    plan_facility_col = find_column(df, "plan facility id")
    assessment_plan_col = find_column(df, "assessment plan id")
    plan_facility_id = None
    assessment_plan_id = None
    if plan_facility_col:
        val = row.get(plan_facility_col)
        if pd.notna(val) and str(val).strip():
            plan_facility_id = str(val).strip()
    if assessment_plan_col:
        val = row.get(assessment_plan_col)
        if pd.notna(val) and str(val).strip():
            assessment_plan_id = str(val).strip()
    return plan_facility_id, assessment_plan_id
