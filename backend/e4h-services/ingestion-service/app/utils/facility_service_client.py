import logging
from typing import Any, Dict, Optional, List

import requests

logger = logging.getLogger(__name__)


class FacilityServiceClient:
    def __init__(self, facility_service_url: str):
        self.facility_service_url = facility_service_url

    def create_facility(self, facility_payload: Dict[str, Any]):
        url = f"{self.facility_service_url}/facility-service/v2/facility/create"
        headers = {"Content-Type": "application/json"}
        payload = facility_payload
        try:
            response = requests.post(url, headers=headers, json=payload, timeout=30)
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error occurred: {http_err}")
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error occurred: {conn_err}")
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error occurred: {timeout_err}")
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"An error occurred: {req_err}")
            raise req_err

    def search_facility(
        self,
        tenant_id: str,
        facility_id: Optional[str] = None,
        boundary_code: Optional[str] = None,
        hfr_id: Optional[str] = None,
        nin_id: Optional[str] = None,
    ) -> Dict[str, Any]:
        """
        Search facilities with two optimized paths:
        - ID-based (facility_id / hfr_id / nin_id): single, non-paginated call – fast for template generation.
        - Boundary-based: paginated with larger page size to reduce round-trips.
        """
        url = f"{self.facility_service_url}/facility-service/v2/facility/search"
        headers = {"Accept": "application/json"}

        try:
            # Fast path for identifier-based lookups used by ingestion templates
            if facility_id or hfr_id or nin_id:
                params: Dict[str, Any] = {
                    "tenantId": tenant_id,
                    "limit": 200,  # more than enough for single-id lookups
                    "offset": 0,
                }
                if facility_id:
                    params["facilityId"] = facility_id
                if hfr_id:
                    params["hfrId"] = hfr_id
                if nin_id:
                    params["ninId"] = nin_id

                response = requests.get(url, headers=headers, params=params, timeout=30)
                response.raise_for_status()
                data = response.json()
                facilities = data.get("facilities", []) or []
                return {"totalCount": len(facilities), "facilities": facilities}

            # Boundary-based search: keep pagination but with higher page size
            limit = 5000  # reduce number of pages vs. previous 1000
            offset = 0
            all_facilities: List[Dict[str, Any]] = []

            params: Dict[str, Any] = {"tenantId": tenant_id, "limit": limit, "offset": offset}
            if boundary_code:
                params["boundaryCode"] = boundary_code

            # First request to get total count
            response = requests.get(url, headers=headers, params=params, timeout=60)
            response.raise_for_status()

            data = response.json()
            total_count = data.get("totalCount", 0)
            all_facilities.extend(data.get("facilities", []) or [])

            # If more pages are present, fetch them
            while len(all_facilities) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.get(url, headers=headers, params=params, timeout=60)
                response.raise_for_status()
                data = response.json()
                all_facilities.extend(data.get("facilities", []) or [])

            return {"totalCount": total_count, "facilities": all_facilities}

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error occurred: {http_err}")
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error occurred: {conn_err}")
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error occurred: {timeout_err}")
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"An error occurred: {req_err}")
            raise req_err
