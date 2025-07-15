import json
import requests

from app.schemas.request_info import RequestInfo
from app.utils.convertor import get_incident_request_info


class IMServiceClient:
    def __init__(self, base_url):
        self.base_url = base_url

    def search_incident(self, incident_id: str):
        url = f"{self.base_url}/im-services/v2/request/_search?tenantId=nl.aoyimkumhwc&incidentId={incident_id}"
        headers = {
            "Content-Type": "application/json;charset=UTF-8",
            "Accept": "application/json"
        }

        request_info = get_incident_request_info()

        payload = {"RequestInfo": request_info}
        try:
            response = requests.post(url, headers=headers, json=payload)
            print(f"Incident searched successfully: {json.loads(response.text)}")
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

    def update_incident(self, payload: dict):
        url = f"{self.base_url}/im-services/v2/request/_update"
        headers = {
            "Content-Type": "application/json"
        }
        try:
            response = requests.post(url, headers=headers, json=payload)
            print(f"Incident updated successfully: {json.loads(response.text)}")
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