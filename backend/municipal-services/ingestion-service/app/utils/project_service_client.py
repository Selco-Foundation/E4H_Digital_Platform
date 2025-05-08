from typing import Dict, Any

import requests


class ProjectServiceClient:
    def __init__(self, project_service_url: str):
        self.project_service_url = project_service_url

    def update_facility_with_supervisor(self, facility_payload:Dict[str,Any]):
        url = f"{self.project_service_url}/facility/supervisor/v1/_update"
        headers = {
            "Content-Type": "application/json"
        }
        payload = facility_payload
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            print(f"Facility with supervisor updated successfully: {response}")
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