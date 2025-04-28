from typing import Dict, Any

from celery.worker.state import requests

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