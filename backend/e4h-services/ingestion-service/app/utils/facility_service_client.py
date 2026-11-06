import logging
from typing import Any, Dict, Optional, List, Sequence, Union

import requests

logger = logging.getLogger(__name__)


SYSTEM_USER_ROLE = "SYSTEM_USER"


def _as_system_request_info(request_info: Dict[str, Any]) -> Dict[str, Any]:
    """Present the caller as this service rather than as the person who uploaded the sheet.

    Facility search filters by the caller's HRMS jurisdiction. Ingestion looks facilities up by id
    to build and validate templates, and reads a missing row as "not found" - so a scoped lookup
    would report real facilities as missing. Identifying as a service keeps those lookups unscoped
    and preserves today's behaviour exactly.
    """
    enriched = dict(request_info or {})
    user_info = dict(enriched.get("userInfo") or {})
    roles = list(user_info.get("roles") or [])
    if not any((role or {}).get("code") == SYSTEM_USER_ROLE for role in roles):
        roles.append({"code": SYSTEM_USER_ROLE, "name": "System User"})
    user_info["roles"] = roles
    user_info["type"] = "SYSTEM"
    enriched["userInfo"] = user_info
    return enriched


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

    def validate_identifiers(self, request_info_payload: Dict[str, Any], tenant_id: str,
                             facilities: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Check HFR ID, NIN ID and PoC Username of facilities before creating them.

        facilities: [{"facilityName", "hfrId", "ninId", "facilityPocUsername"}, ...]
        Returns facility-service's conflicts: [{"index": <position in facilities>, "errors": [...]}].
        """
        url = f"{self.facility_service_url}/facility-service/v2/facility/_validate-identifiers"
        payload = {"RequestInfo": request_info_payload, "tenantId": tenant_id, "facilities": facilities}
        response = requests.post(url, headers={"Content-Type": "application/json"}, json=payload, timeout=120)
        response.raise_for_status()
        return (response.json() or {}).get("conflicts", []) or []

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
        block_codes: Optional[Sequence[str]] = None,
        hfr_ids: Optional[Sequence[str]] = None,
        nin_ids: Optional[Sequence[str]] = None,
        limit: int = 10000,
        offset: int = 0,
        send_non_paginated_response: bool = True,
        sort_by: Optional[str] = None,
        sort_order: Optional[str] = None,
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
            "block": [...],
            ...
          }
        }
        """
        return self._bulk_search_facility_by_path(
            endpoint_path="/facility-service/v2/facility/_bulk-search",
            request_info=request_info,
            tenant_ids=tenant_ids,
            facility_ids=facility_ids,
            block_codes=block_codes,
            hfr_ids=hfr_ids,
            nin_ids=nin_ids,
            limit=limit,
            offset=offset,
            send_non_paginated_response=send_non_paginated_response,
            sort_by=sort_by,
            sort_order=sort_order,
        )

    def bulk_search_facility_with_boundary(
        self,
        request_info: Union[Dict[str, Any], Any],
        tenant_ids: Sequence[str],
        facility_ids: Optional[Sequence[str]] = None,
        block_codes: Optional[Sequence[str]] = None,
        hfr_ids: Optional[Sequence[str]] = None,
        nin_ids: Optional[Sequence[str]] = None,
        limit: int = 10000,
        offset: int = 0,
        send_non_paginated_response: bool = True,
        sort_by: Optional[str] = None,
        sort_order: Optional[str] = None,
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
            block_codes=block_codes,
            hfr_ids=hfr_ids,
            nin_ids=nin_ids,
            limit=limit,
            offset=offset,
            send_non_paginated_response=send_non_paginated_response,
            sort_by=sort_by,
            sort_order=sort_order,
        )

    def _bulk_search_facility_by_path(
        self,
        endpoint_path: str,
        request_info: Union[Dict[str, Any], Any],
        tenant_ids: Sequence[str],
        facility_ids: Optional[Sequence[str]] = None,
        block_codes: Optional[Sequence[str]] = None,
        hfr_ids: Optional[Sequence[str]] = None,
        nin_ids: Optional[Sequence[str]] = None,
        limit: int = 10000,
        offset: int = 0,
        send_non_paginated_response: bool = True,
        sort_by: Optional[str] = None,
        sort_order: Optional[str] = None,
    ) -> Dict[str, Any]:
        url = f"{self.facility_service_url}{endpoint_path}"
        headers = {"Content-Type": "application/json", "Accept": "application/json"}

        # Support both Pydantic RequestInfo and plain dict
        if hasattr(request_info, "model_dump"):
            request_info_payload = request_info.model_dump(by_alias=True, exclude_none=True)
        else:
            request_info_payload = request_info or {}

        payload: Dict[str, Any] = {
            # Known gap, recorded in docs/jurisdiction/NEXT_STEPS.md: because this is a service
            # call, an uploader can still build a template covering facilities outside their own
            # area. Closing that needs ingestion to decide which role the uploader is acting as,
            # which is a product question rather than a technical one.
            "RequestInfo": _as_system_request_info(request_info_payload),
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
                "boundaryCodes": [],
                "state": [],
                "district": [],
                # These are block codes - flatten_boundaries only ever emits block nodes - so they
                # go in `block`, which facility-service resolves through the hierarchy into the
                # facilities beneath them. `boundaryCodes` is matched against facility.boundary_code
                # directly, so a block code sent there would match nothing.
                "block": list(block_codes) if block_codes else [],
                "sendNonPaginatedResponse": send_non_paginated_response,
                "limit": limit,
                "offset": offset,
                "isOnmReady": None,
            },
        }

        # Only sent when asked for: facility-service defaults to `updated_at DESC`, and every
        # other caller here depends on that. `sortBy` is matched against the column name, so it
        # is "created_at".
        if sort_by:
            payload["Facility"]["sortBy"] = sort_by
        if sort_order:
            payload["Facility"]["sortOrder"] = sort_order

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
