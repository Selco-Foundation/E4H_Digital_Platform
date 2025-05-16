from typing import Dict, Any, List

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

    def search_facility(self, tenant_id: str, facility_id: str):

        url = f"{self.facility_service_url}/facility-service/v2/facility/search"
        params = {"tenant_id": tenant_id}

        # Add optional facility_id parameter if provided
        if facility_id:
            params["facility_id"] = "FAC/2025/000039"

        headers = {
            "Accept": "application/json"
        }

        try:
            response = requests.get(url, headers=headers, params=params)
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


    def search_facility_by_id(self, facility_id: str) -> List[Dict[str, Any]]:
        url = f"{self.facility_service_url}/facility-service/v2/facility/search"
        params = {
            "tenant_id": 'in',
            "facility_id": facility_id,
            "limit": 1,
            "offset": 0
        }
        headers = {
            "Accept": "application/json"
        }

        try:
            response = requests.get(url, headers=headers, params=params)
            response.raise_for_status()
            facilities = response.json()
            print(f"Facility search result for ID {facility_id}: {facilities}")
            return facilities  # This is a list of facility dicts

        except requests.exceptions.HTTPError as http_err:
            print(f"HTTP error occurred while searching for facility {facility_id}: {http_err}")
            raise http_err
        except requests.exceptions.RequestException as req_err:
            print(f"Request error occurred while searching for facility {facility_id}: {req_err}")
            raise req_err

    def search_facility_by_boundary_codes(self, boundary_codes: List[str], request_info: RequestInfo):
        url = f"{self.facility_service_url}/facility-service/v2/facility/_search"

        headers = {
            "Content-Type": "application/json"
        }

        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "boundaryCodes": boundary_codes
        }

        try:
            response = requests.post(url, headers=headers, json=payload)
            return response.json()
        except requests.exceptions.RequestException as err:
            print(f"Request error: {err}")
            raise err
