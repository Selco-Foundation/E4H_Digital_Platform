import logging
from typing import Any, Dict, List, Optional, Union

import requests

from app.schemas.request_info import RequestInfo

logger = logging.getLogger(__name__)

DEFAULT_TENANT_ID = "in"


class HRMSServiceClient:
    def __init__(self, hrms_service_url: str):
        self.hrms_service_url = hrms_service_url.rstrip("/")

    @staticmethod
    def _request_info_payload(request_info: Union[RequestInfo, Dict[str, Any]]) -> Dict[str, Any]:
        if hasattr(request_info, "model_dump"):
            return request_info.model_dump(by_alias=True, exclude_none=True)
        return request_info or {}

    def create_user(self, user_payload: Dict[str, Any]):
        url = f"{self.hrms_service_url}/egov-hrms/employees/_create"
        headers = {"Content-Type": "application/json"}
        params = {"tenantId": DEFAULT_TENANT_ID}
        try:
            requests.post(url, headers=headers, params=params, json=user_payload, timeout=60)
            response = self.search_user(user_payload=user_payload)
            return response
        except requests.exceptions.RequestException as req_err:
            logger.error("HRMS create user failed: %s", req_err)
            raise req_err

    def search_user(self, user_payload: Dict[str, Any]):
        url = f"{self.hrms_service_url}/egov-hrms/employees/_search"
        headers = {"Content-Type": "application/json"}
        params = {
            "tenantId": DEFAULT_TENANT_ID,
            "phone": user_payload["Employees"][0]["user"]["mobileNumber"],
        }
        logger.trace(f"Searching user in HRMS: {url}")
        try:
            response = requests.post(url, headers=headers, params=params, json=user_payload, timeout=60)
            return response
        except requests.exceptions.RequestException as req_err:
            logger.error("HRMS search user failed: %s", req_err)
            raise req_err

    def get_employee_by_uuid(
            self,
            hrms_uuid: str,
            request_info: Union[RequestInfo, Dict[str, Any]],
            tenant_id: str = DEFAULT_TENANT_ID,
    ) -> Optional[Dict[str, Any]]:
        url = f"{self.hrms_service_url}/egov-hrms/employees/_search"
        headers = {"Content-Type": "application/json"}
        params = {"tenantId": tenant_id, "uuids": hrms_uuid}
        payload = {"RequestInfo": self._request_info_payload(request_info)}
        try:
            response = requests.post(url, headers=headers, params=params, json=payload, timeout=120)
            response.raise_for_status()
            employees = response.json().get("Employees") or []
            return employees[0] if employees else None
        except requests.exceptions.RequestException as req_err:
            logger.error("HRMS get employee by uuid failed for %s: %s", hrms_uuid, req_err)
            raise req_err

    def update_employee(
            self,
            employee: Dict[str, Any],
            request_info: Union[RequestInfo, Dict[str, Any]],
            tenant_id: str = DEFAULT_TENANT_ID,
    ) -> Optional[List[Dict[str, Any]]]:
        url = f"{self.hrms_service_url}/egov-hrms/employees/_update"
        headers = {"Content-Type": "application/json"}
        params = {"tenantId": tenant_id}
        payload = {
            "RequestInfo": self._request_info_payload(request_info),
            "Employees": [employee],
        }
        try:
            response = requests.post(url, headers=headers, params=params, json=payload, timeout=120)
            response.raise_for_status()
            employees = response.json().get("Employees") or []
            return employees if employees else None
        except requests.exceptions.RequestException as req_err:
            logger.error("HRMS update employee failed for %s: %s", employee.get("uuid"), req_err)
            raise req_err
