import json
from typing import Dict, Any, List, Optional

import requests
from sqlalchemy import null, true

from app.schemas.request_info import RequestInfo


class FacilityServiceClient:
    def __init__(self, facility_service_url: str):
        self.facility_service_url = facility_service_url

    def create_facility(self, facility_payload: Dict[str, Any]):
        url = f"{self.facility_service_url}/facility-service/v2/facility/create"
        headers = {
            "Content-Type": "application/json"
        }
        payload = facility_payload
        try:
            response = requests.post(url, headers=headers, json=payload)
            return response

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

    def search_facility(self, tenant_id: str, facility_id: Optional[str] = None, boundary_code: Optional[str] = None) -> Dict[str, Any]:
        limit = 1000
        offset = 0
        all_facilities = []

        url = f"{self.facility_service_url}/facility-service/v2/facility/search"

        headers = {
            "Accept": "application/json"
        }

        try:
            # First request to get total count
            params = {
                "tenantId": tenant_id,
                "limit": limit,
                "offset": offset
            }

            # Add optional facility_id parameter if provided
            if facility_id:
                params["facilityId"] = facility_id
            if boundary_code:
                params["boundaryCode"] = boundary_code

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

            return {
                "totalCount": total_count,
                "facilities": all_facilities
            }

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