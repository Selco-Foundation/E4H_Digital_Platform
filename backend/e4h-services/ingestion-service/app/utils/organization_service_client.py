from typing import Dict, Any, Optional

import pandas as pd
import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
logger = AppLogger().get_logger()

class OrganizationServiceClient:
    def __init__(self, org_service_url: str):
        self.org_service_url = org_service_url

    def create_vendor(self, vendor_payload:Dict[str,Any]):
        url = f"{self.org_service_url}/vendor/organisation/v1/_create"
        headers = {
            "Content-Type": "application/json"
        }
        payload = vendor_payload
        logger.trace(f"Creating vendor in organization service: {url}")
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            logger.info("Vendor saved successfully in organization service")
            logger.debug(f"Vendor creation response status: {response.status_code}")
            return response.json()

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating vendor: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating vendor: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating vendor: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating vendor: {req_err}", exc_info=True)
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
