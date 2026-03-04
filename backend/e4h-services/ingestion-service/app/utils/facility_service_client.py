from typing import Any, Dict, Optional

import requests

from app.core.logging import AppLogger

logger = AppLogger().get_logger()


class FacilityServiceClient:
    def __init__(self, facility_service_url: str):
        self.facility_service_url = facility_service_url

    def create_facility(self, facility_payload: Dict[str, Any]):
        logger.trace("Creating facility in facility service")
        url = f"{self.facility_service_url}/facility-service/v2/facility/create"
        headers = {"Content-Type": "application/json"}
        payload = facility_payload
        facility_id = facility_payload.get("facility", {}).get("facility_id") or "unknown"
        try:
            response = requests.post(url, headers=headers, json=payload)
            logger.info(f"Facility created successfully: facility_id={facility_id}")
            logger.debug(f"Create response status: {response.status_code}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating facility {facility_id}: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating facility {facility_id}: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating facility {facility_id}: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating facility {facility_id}: {req_err}", exc_info=True)
            raise req_err

    def search_facility(
        self,
        tenant_id: str,
        facility_id: Optional[str] = None,
        boundary_code: Optional[str] = None,
        hfr_id: Optional[str] = None,
        nin_id: Optional[str] = None,
    ) -> Dict[str, Any]:
        limit = 1000
        offset = 0
        all_facilities = []

        url = f"{self.facility_service_url}/facility-service/v2/facility/search"

        headers = {"Accept": "application/json"}

        logger.trace(f"Searching facilities: tenant_id={tenant_id}, facility_id={facility_id}, boundary_code={boundary_code}")
        try:
            # First request to get total count
            params = {"tenantId": tenant_id, "limit": limit, "offset": offset}

            # Add optional facility_id parameter if provided
            if facility_id:
                params["facilityId"] = facility_id
            if boundary_code:
                params["boundaryCode"] = boundary_code
            if hfr_id:
                params["hfrId"] = hfr_id
            if nin_id:
                params["ninId"] = nin_id

            response = requests.get(url, headers=headers, params=params)
            response.raise_for_status()

            data = response.json()
            total_count = data.get("totalCount", 0)
            all_facilities.extend(data.get("facilities", []))

            # If more pages are present, fetch them
            while len(all_facilities) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.get(url, headers=headers, params=params)
                response.raise_for_status()
                data = response.json()
                all_facilities.extend(data.get("facilities", []))

            logger.info(f"Facility search completed: {total_count} facilities found")
            logger.debug(f"Search parameters: tenant_id={tenant_id}, facility_id={facility_id}, boundary_code={boundary_code}")
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
