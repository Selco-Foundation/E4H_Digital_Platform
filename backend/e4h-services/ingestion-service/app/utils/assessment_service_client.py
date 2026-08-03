import json
from typing import Any, Dict, List, Optional

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo

logger = AppLogger().get_logger()


class AssessmentServiceClient:
    def __init__(self, fieldplan_service_url: str):
        self.fieldplan_service_url = fieldplan_service_url.rstrip("/")

    def bulk_create_plan_facilities(
        self,
        request_info: RequestInfo,
        plan_id: str,
        tenant_id: str,
        facilities: List[Dict[str, Any]],
    ) -> Dict[str, Any]:
        url = f"{self.fieldplan_service_url}/field-planner/assessment/v1/internal/plan/facility/_bulk-create"
        headers = {"Content-Type": "application/json"}
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "planId": plan_id,
            "tenantId": tenant_id,
            "facilities": facilities,
        }
        logger.info(
            f"Assessment bulk-create: plan_id={plan_id}, count={len(facilities)}"
        )
        logger.debug(f"Assessment bulk-create payload facilities: {facilities}")
        response = requests.post(url, headers=headers, json=payload)
        if not response.ok:
            logger.error(
                "Assessment bulk-create failed: status=%s body=%s",
                response.status_code,
                response.text,
            )
            response.raise_for_status()
        return response.json()

    def search_assessment_plan(
        self, request_info: RequestInfo, plan_id: str, tenant_id: str = "in"
    ) -> Optional[Dict[str, Any]]:
        url = f"{self.fieldplan_service_url}/field-planner/assessment/v1/plan/_detail"
        headers = {"Content-Type": "application/json"}
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "planId": plan_id,
        }
        response = requests.post(url, headers=headers, json=payload)
        response.raise_for_status()
        data = response.json()
        return data.get("plan")

    def bulk_include_plan_facilities(
        self,
        request_info: RequestInfo,
        plan_id: str,
        tenant_id: str,
        facility_ids: List[str],
    ) -> Dict[str, Any]:
        url = f"{self.fieldplan_service_url}/field-planner/assessment/v1/plan/facility/_bulk-include"
        headers = {"Content-Type": "application/json"}
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "planId": plan_id,
            "tenantId": tenant_id,
            "facilityIds": facility_ids,
        }
        response = requests.post(url, headers=headers, json=payload)
        response.raise_for_status()
        return response.json()

    def search_plan_facilities(
        self,
        request_info: RequestInfo,
        plan_id: str,
        filters: Optional[Dict[str, Any]] = None,
        export_all: bool = False,
        include_response_summary: bool = False,
        offset: int = 0,
        limit: int = 10000,
    ) -> Dict[str, Any]:
        url = (
            f"{self.fieldplan_service_url}/field-planner/assessment/v1/plan/facility/_search"
            f"?offset={offset}&limit={limit}"
        )
        headers = {"Content-Type": "application/json"}
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "planId": plan_id,
            "filters": filters or {},
            "exportAll": export_all,
            "includeResponseSummary": include_response_summary,
        }
        response = requests.post(url, headers=headers, json=payload)
        response.raise_for_status()
        return response.json()

    def search_eligible_facilities(
        self,
        request_info: RequestInfo,
        project_id: str,
        tenant_id: str,
        assessment_plan_ids: Optional[List[str]] = None,
    ) -> Dict[str, Any]:
        url = (
            f"{self.fieldplan_service_url}/field-planner/assessment/v1/internal/project/"
            "eligible-facilities/_search"
        )
        headers = {"Content-Type": "application/json"}
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "projectId": project_id,
            "tenantId": tenant_id,
            "assessmentPlanIds": assessment_plan_ids or [],
        }
        response = requests.post(url, headers=headers, json=payload)
        response.raise_for_status()
        return response.json()

    def apply_facility_handoff(
        self,
        request_info: RequestInfo,
        plan_facility_id: str,
        installation_field_plan_id: str,
        field_plan_facility_id: Optional[str] = None,
    ) -> Dict[str, Any]:
        url = (
            f"{self.fieldplan_service_url}/field-planner/assessment/v1/internal/plan/facility/_handoff"
        )
        headers = {"Content-Type": "application/json"}
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "planFacilityId": plan_facility_id,
            "installationFieldPlanId": installation_field_plan_id,
        }
        if field_plan_facility_id:
            payload["fieldPlanFacilityId"] = field_plan_facility_id
        response = requests.post(url, headers=headers, json=payload)
        response.raise_for_status()
        return response.json()
