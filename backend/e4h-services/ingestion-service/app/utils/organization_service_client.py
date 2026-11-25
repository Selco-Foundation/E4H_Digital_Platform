from typing import Dict, Any, List, Optional

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo

logger = AppLogger().get_logger()

DEFAULT_TENANT_ID = "in"

class OrganizationServiceClient:
    def __init__(self, org_service_url: str):
        self.org_service_url = org_service_url

    def create_vendor(self, vendor_payload:Dict[str,Any]):
        url = f"{self.org_service_url}/vendor/organisation/v1/_create"
        headers = {
            "Content-Type": "application/json"
        }
        payload = vendor_payload
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            print(f"Vendor save successfully: {response}")
            return response.json()

        except requests.exceptions.HTTPError as http_err:
            print(f"HTTP error occurred: {http_err}")
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            print(f"Connection error occurred: {conn_err}")
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            print(f"Timeout error occurred: {timeout_err}")
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            print(f"An error occurred: {req_err}")
            raise req_err

    def fetch_vendor_organisations(self, request_info: RequestInfo,
                                   tenant_id: str = DEFAULT_TENANT_ID) -> List[Dict[str, Any]]:
        """Every active vendor organisation (orgType VENDOR, orgStatus ACTIVE). Raises when the
        vendor service can't be reached: an empty list would silently mean "no vendors"."""
        if not self.org_service_url:
            raise RuntimeError("VENDOR_SERVICE_URL is not configured")
        url = f"{self.org_service_url}/vendor/organisation/v1/_search"
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "SearchCriteria": {"tenantId": tenant_id, "orgType": "VENDOR", "orgStatus": "ACTIVE",
                               "createdFrom": 0},
            "Pagination": {"limit": 10000, "offset": 0},
        }
        response = requests.post(url, headers={"Content-Type": "application/json"}, json=payload, timeout=60)
        response.raise_for_status()
        return response.json().get("organisations") or []

    def fetch_org_user_ids(self, organisation_id: str, request_info: RequestInfo,
                           tenant_id: str = DEFAULT_TENANT_ID) -> List[str]:
        """The user ids (HRMS uuids) of an organisation's users, deleted ones excluded. Raises
        when the vendor service can't be reached."""
        url = (f"{self.org_service_url}/vendor/organisation/v1/user/_search"
               f"?limit=100&offset=0&tenantId={tenant_id}")
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "OrgUser": {"tenantId": tenant_id, "organizationIds": [organisation_id]},
        }
        response = requests.post(url, headers={"Content-Type": "application/json"}, json=payload, timeout=60)
        response.raise_for_status()
        user_ids = []
        for org_user in response.json().get("OrgUsers") or []:
            user_id = self._string_value(org_user.get("userId"))
            if user_id and not org_user.get("isDeleted") and user_id not in user_ids:
                user_ids.append(user_id)
        return user_ids

    def _find_organisation_by_code(
            self,
            vendor_code: str,
            request_info: RequestInfo,
            tenant_id: str,
    ) -> Optional[Dict[str, Any]]:
        url = f"{self.org_service_url}/vendor/organisation/v1/_search"
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "SearchCriteria": {"tenantId": tenant_id, "code": vendor_code},
            "Pagination": {"limit": 1, "offset": 0},
        }
        try:
            response = requests.post(
                url,
                headers={"Content-Type": "application/json"},
                json=payload,
                timeout=60,
            )
            response.raise_for_status()
            organisations = response.json().get("organisations") or []
            return organisations[0] if organisations else None
        except Exception as e:
            logger.warning("Organisation search failed for vendor code %s: %s", vendor_code, e)
            return None

    def find_first_org_user_hrms_uuid(
            self,
            vendor_code: str,
            request_info: RequestInfo,
            tenant_id: str = DEFAULT_TENANT_ID,
    ) -> Optional[str]:
        """Resolve the HRMS uuid of the first active user for a vendor organisation code."""
        normalized = self.normalize_facility_vendor_code(vendor_code)
        if not normalized or not self.org_service_url:
            return None

        organisation = self._find_organisation_by_code(normalized, request_info, tenant_id)
        if not organisation:
            return None

        organisation_id = self._string_value(organisation.get("id"))
        if not organisation_id:
            return None
        return self._find_first_org_user_hrms_uuid(organisation_id, request_info, tenant_id)

    def _find_first_org_user_hrms_uuid(
            self,
            organisation_id: str,
            request_info: RequestInfo,
            tenant_id: str,
    ) -> Optional[str]:
        url = (
            f"{self.org_service_url}/vendor/organisation/v1/user/_search"
            f"?limit=10&offset=0&tenantId={tenant_id}"
        )
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "OrgUser": {
                "tenantId": tenant_id,
                "organizationIds": [organisation_id],
            },
        }
        try:
            response = requests.post(
                url,
                headers={"Content-Type": "application/json"},
                json=payload,
                timeout=60,
            )
            response.raise_for_status()
            org_users = response.json().get("OrgUsers") or []
            for org_user in org_users:
                if org_user.get("isDeleted"):
                    continue
                user_id = self._string_value(org_user.get("userId"))
                if user_id:
                    return user_id
            return None
        except Exception as e:
            logger.warning(
                "Organisation user HRMS lookup failed for organisation %s: %s",
                organisation_id,
                e,
            )
            return None

    @staticmethod
    def _string_value(value: Any) -> Optional[str]:
        if value is None:
            return None
        s = str(value).strip()
        return s if s else None
