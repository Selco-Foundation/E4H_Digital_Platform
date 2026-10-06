import logging
from typing import Any, Dict, Optional, List, Sequence, Union

import requests

logger = logging.getLogger(__name__)


class FacilityServiceClient:
    def __init__(self, facility_service_url: str):
        self.facility_service_url = facility_service_url

    def create_facility(self, facility_payload: Dict[str, Any]):
        url = f"{self.facility_service_url}/facility-service/v2/facility/create"
        headers = {"Content-Type": "application/json"}
        payload = facility_payload
        facility_count = len(payload.get("facilities") or [])
        timeout = 120 if facility_count > 1 else 30
        try:
            response = requests.post(url, headers=headers, json=payload, timeout=timeout)
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

    def bulk_search_facility(
        self,
        request_info: Union[Dict[str, Any], Any],
        tenant_ids: Sequence[str],
        facility_ids: Optional[Sequence[str]] = None,
        boundary_codes: Optional[Sequence[str]] = None,
        hfr_ids: Optional[Sequence[str]] = None,
        nin_ids: Optional[Sequence[str]] = None,
        limit: int = 10000,
        offset: int = 0,
        send_non_paginated_response: bool = True,
    ) -> Dict[str, Any]:
        """
        Bulk search facilities using facility-service bulk search endpoint.

        Mirrors the curl contract:
        POST /facility-service/v2/facility/_bulk-search
        {
          "RequestInfo": {...},
          "Facility": {
            "tenantIds": [...],
            "facilityIds": [...],
            "boundaryCodes": [...],
            ...
          }
        }
        """
        return self._bulk_search_facility_by_path(
            endpoint_path="/facility-service/v2/facility/_bulk-search",
            request_info=request_info,
            tenant_ids=tenant_ids,
            facility_ids=facility_ids,
            boundary_codes=boundary_codes,
            hfr_ids=hfr_ids,
            nin_ids=nin_ids,
            limit=limit,
            offset=offset,
            send_non_paginated_response=send_non_paginated_response,
        )

    def bulk_search_facility_with_boundary(
        self,
        request_info: Union[Dict[str, Any], Any],
        tenant_ids: Sequence[str],
        facility_ids: Optional[Sequence[str]] = None,
        boundary_codes: Optional[Sequence[str]] = None,
        hfr_ids: Optional[Sequence[str]] = None,
        nin_ids: Optional[Sequence[str]] = None,
        limit: int = 10000,
        offset: int = 0,
        send_non_paginated_response: bool = True,
    ) -> Dict[str, Any]:
        """
        Bulk search using facility-service endpoint that returns facility + address
        and resolves boundary details using boundary codes present on result rows.
        """
        return self._bulk_search_facility_by_path(
            endpoint_path="/facility-service/v2/facility/_bulk-search-with-boundary",
            request_info=request_info,
            tenant_ids=tenant_ids,
            facility_ids=facility_ids,
            boundary_codes=boundary_codes,
            hfr_ids=hfr_ids,
            nin_ids=nin_ids,
            limit=limit,
            offset=offset,
            send_non_paginated_response=send_non_paginated_response,
        )

    def _bulk_search_facility_by_path(
        self,
        endpoint_path: str,
        request_info: Union[Dict[str, Any], Any],
        tenant_ids: Sequence[str],
        facility_ids: Optional[Sequence[str]] = None,
        boundary_codes: Optional[Sequence[str]] = None,
        hfr_ids: Optional[Sequence[str]] = None,
        nin_ids: Optional[Sequence[str]] = None,
        limit: int = 10000,
        offset: int = 0,
        send_non_paginated_response: bool = True,
    ) -> Dict[str, Any]:
        url = f"{self.facility_service_url}{endpoint_path}"
        headers = {"Content-Type": "application/json", "Accept": "application/json"}

        # Support both Pydantic RequestInfo and plain dict
        if hasattr(request_info, "model_dump"):
            request_info_payload = request_info.model_dump(by_alias=True, exclude_none=True)
        else:
            request_info_payload = request_info or {}

        payload: Dict[str, Any] = {
            "RequestInfo": request_info_payload,
            "Facility": {
                "tenantIds": list(tenant_ids),
                "facilityIds": list(facility_ids) if facility_ids else [],
                "facilityNames": [],
                "hfrIds": list(hfr_ids) if hfr_ids else [],
                "ninIds": list(nin_ids) if nin_ids else [],
                "facilityPocNames": [],
                "facilityPocPhones": [],
                "facilityPocEmails": [],
                "facilityStatus": [],
                "userIds": [],
                "boundaryCodes": list(boundary_codes) if boundary_codes else [],
                "state": [],
                "district": [],
                "block": [],
                "sendNonPaginatedResponse": send_non_paginated_response,
                "limit": limit,
                "offset": offset,
                "isOnmReady": None,
            },
        }

        try:
            response = requests.post(url, headers=headers, json=payload, timeout=60)
            response.raise_for_status()
            data = response.json() or {}
            facilities = data.get("facilities", []) or data.get("Facilities", []) or []
            total_count = data.get("totalCount", len(facilities))
            return {"totalCount": total_count, "facilities": facilities}
        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error occurred during bulk facility search: {http_err}")
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error occurred during bulk facility search: {conn_err}")
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error occurred during bulk facility search: {timeout_err}")
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"An error occurred during bulk facility search: {req_err}")
            raise req_err
