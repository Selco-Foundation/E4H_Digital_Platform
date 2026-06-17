import os
from typing import Dict, Any, Optional

import pandas as pd
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

    def normalize_facility_vendor_code(self, val) -> str:
        if pd.isna(val):
            return ""
        s = str(val).strip()
        if len(s) > 2 and s.endswith(".0") and s[:-2].isdigit():
            s = s[:-2]
        return s

    def fetch_registered_vendor_codes(self, request_info: RequestInfo) -> Optional[set]:
        """
        Vendor codes registered in the organisation (vendor) service.
        None means the registry could not be loaded (degraded: only non-empty checks apply).
        """
        if not self.org_service_url:
            return None
        url = f"{self.org_service_url}/vendor/organisation/v1/_search"
        try:
            payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "SearchCriteria": {"tenantId": "in", "createdFrom": 0},
                "Pagination": {"limit": 10000, "offset": 0},
            }
            response = requests.post(
                url,
                headers={"Content-Type": "application/json"},
                json=payload,
                timeout=60,
            )
            response.raise_for_status()
            data = response.json()
            codes = set()
            for org in (data.get("organisations") or []):
                c = org.get("code")
                if c is not None and str(c).strip():
                    codes.add(str(c).strip())
            return codes
        except Exception as e:
            logger.warning(f"Could not load vendor codes for facility ingestion validation: {e}")
            return None

    def resolve_mapped_vendor_fields(
            self,
            vendor_code: str,
            request_info: RequestInfo,
            tenant_id: str = DEFAULT_TENANT_ID,
    ) -> Dict[str, Optional[str]]:
        """
        Resolve mappedVendorName (organisation name) and mappedVendorUserName (first org user's userName)
        for a registered vendor code.
        """
        normalized = self.normalize_facility_vendor_code(vendor_code)
        if not normalized:
            return {"mappedVendorName": None, "mappedVendorUserName": None}
        if not self.org_service_url:
            logger.warning("VENDOR_SERVICE_URL not configured; cannot resolve vendor mapping")
            return {"mappedVendorName": None, "mappedVendorUserName": None}

        organisation = self._find_organisation_by_code(normalized, request_info, tenant_id)
        if not organisation:
            logger.warning("No organisation found for vendor code %s", normalized)
            return {"mappedVendorName": None, "mappedVendorUserName": None}

        vendor_name = self._string_value(organisation.get("name"))
        organisation_id = self._string_value(organisation.get("id"))
        vendor_user_name = None
        if organisation_id:
            vendor_user_name = self._find_first_org_user_name(
                organisation_id, request_info, tenant_id
            )

        return {
            "mappedVendorName": vendor_name,
            "mappedVendorUserName": vendor_user_name,
        }

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

    def _find_first_org_user_name(
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
                user = org_user.get("user") or {}
                user_name = self._string_value(user.get("userName"))
                if user_name:
                    return user_name
                user_id = self._string_value(org_user.get("userId"))
                if user_id:
                    return self._resolve_hrms_user_name(user_id, request_info, tenant_id)
            return None
        except Exception as e:
            logger.warning(
                "Organisation user search failed for organisation %s: %s",
                organisation_id,
                e,
            )
            return None

    def _resolve_hrms_user_name(
            self,
            user_uuid: str,
            request_info: RequestInfo,
            tenant_id: str,
    ) -> Optional[str]:
        hrms_host = os.getenv("HRMS_SERVICE_URL") or os.getenv("EGOV_HRMS_HOST")
        if not hrms_host:
            return None
        url = f"{hrms_host.rstrip('/')}/egov-hrms/employees/_search?tenantId={tenant_id}&uuids={user_uuid}"
        try:
            response = requests.post(
                url,
                headers={"Content-Type": "application/json"},
                json={"RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True)},
                timeout=60,
            )
            response.raise_for_status()
            employees = response.json().get("Employees") or []
            if not employees:
                return None
            user = employees[0].get("user") or {}
            return self._string_value(user.get("userName"))
        except Exception as e:
            logger.warning("HRMS lookup failed for user %s: %s", user_uuid, e)
            return None

    @staticmethod
    def _string_value(value: Any) -> Optional[str]:
        if value is None:
            return None
        s = str(value).strip()
        return s if s else None
