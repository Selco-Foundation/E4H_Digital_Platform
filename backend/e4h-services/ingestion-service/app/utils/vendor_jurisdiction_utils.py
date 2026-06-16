import logging
from typing import Any, Dict, List, Optional, Union

from app.schemas.request_info import RequestInfo
from app.utils.hrms_service_client import HRMSServiceClient
from app.utils.organization_service_client import OrganizationServiceClient

logger = logging.getLogger(__name__)

DEFAULT_TENANT_ID = "in"


def _request_info_payload(request_info: Union[RequestInfo, Dict[str, Any]]) -> Dict[str, Any]:
    if hasattr(request_info, "model_dump"):
        return request_info.model_dump(by_alias=True, exclude_none=True)
    return request_info or {}


def merge_facility_jurisdiction(
        existing: Optional[List[Dict[str, Any]]],
        boundary_code: str,
        tenant_id: str = DEFAULT_TENANT_ID,
) -> List[Dict[str, Any]]:
    merged: List[Dict[str, Any]] = list(existing or [])
    if not boundary_code:
        return merged

    normalized = boundary_code.strip()
    for jurisdiction in merged:
        if jurisdiction and str(jurisdiction.get("boundary", "")).strip().lower() == normalized.lower():
            jurisdiction["hierarchy"] = "ADMIN"
            jurisdiction["boundaryType"] = "Facility"
            jurisdiction["tenantId"] = tenant_id
            jurisdiction["isActive"] = True
            return merged

    merged.append({
        "hierarchy": "ADMIN",
        "boundary": normalized,
        "boundaryType": "Facility",
        "tenantId": tenant_id,
        "isActive": True,
    })
    return merged


def bulk_assign_vendor_jurisdictions(
        org_client: OrganizationServiceClient,
        hrms_client: HRMSServiceClient,
        request_info: Union[RequestInfo, Dict[str, Any]],
        vendor_boundary_map: Dict[str, List[str]],
        tenant_id: str = DEFAULT_TENANT_ID,
) -> Dict[str, str]:
    """
    For each vendor code, merge all facility boundary codes into the first org user's HRMS jurisdictions
    with a single update per vendor.
    Returns vendor_code -> status message.
    """
    results: Dict[str, str] = {}
    if not vendor_boundary_map:
        return results

    for vendor_code, boundary_codes in vendor_boundary_map.items():
        normalized_vendor = org_client.normalize_facility_vendor_code(vendor_code)
        unique_boundaries = []
        seen = set()
        for boundary in boundary_codes or []:
            if not boundary:
                continue
            key = str(boundary).strip()
            if not key or key.lower() in seen:
                continue
            seen.add(key.lower())
            unique_boundaries.append(key)

        if not normalized_vendor:
            results[vendor_code] = "Skipped: empty vendor code"
            continue
        if not unique_boundaries:
            results[normalized_vendor] = "Skipped: no boundary codes"
            continue

        try:
            hrms_uuid = org_client.find_first_org_user_hrms_uuid(
                normalized_vendor, request_info, tenant_id
            )
            if not hrms_uuid:
                results[normalized_vendor] = "Skipped: no org user found"
                continue

            employee = hrms_client.get_employee_by_uuid(hrms_uuid, request_info, tenant_id)
            if not employee:
                results[normalized_vendor] = f"Skipped: HRMS employee not found for {hrms_uuid}"
                continue

            jurisdictions = employee.get("jurisdictions") or []
            for boundary in unique_boundaries:
                jurisdictions = merge_facility_jurisdiction(jurisdictions, boundary, tenant_id)
            employee["jurisdictions"] = jurisdictions

            updated = hrms_client.update_employee(employee, request_info, tenant_id)
            if updated:
                results[normalized_vendor] = f"Updated with {len(unique_boundaries)} boundary(ies)"
                logger.info(
                    "Bulk-assigned %s facility boundaries to vendor %s (HRMS user %s)",
                    len(unique_boundaries),
                    normalized_vendor,
                    hrms_uuid,
                )
            else:
                results[normalized_vendor] = "Failed: HRMS update returned no employees"
        except Exception as exc:
            logger.error(
                "Failed bulk jurisdiction assignment for vendor %s: %s",
                normalized_vendor,
                exc,
                exc_info=True,
            )
            results[normalized_vendor] = f"Failed: {exc}"

    return results
