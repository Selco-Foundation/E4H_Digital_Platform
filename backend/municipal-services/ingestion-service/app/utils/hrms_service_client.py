from typing import Dict, Any

import requests


class HRMSServiceClient:
    def __init__(self,hrms_service_url):
        self.hrms_service_url = hrms_service_url

    def create_user(self, user_payload: Dict[str, Any]):
        url = f"{self.hrms_service_url}/egov-hrms/employees/_create"
        headers = {
            "Content-Type": "application/json"
        }
        params = {
            "tenantId": "in"
        }
        try:
            response = requests.post(url, headers=headers, params=params, json=user_payload)
            # response.raise_for_status()
            print(f"User created successfully: {response}")
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