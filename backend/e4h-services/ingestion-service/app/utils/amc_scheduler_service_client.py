import json
from typing import Dict, Any, List

import requests

from app.schemas.request_info import RequestInfo


class AMCSchedulerServiceClient:
    def __init__(self, amc_scheduler_service_url: str):
        self.amc_scheduler_service_url = amc_scheduler_service_url

    def create_amc_configuration(self, request_info: RequestInfo, configuration_payload: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create AMC configuration via AMC Scheduler Service
        """
        url = f"{self.amc_scheduler_service_url}/asset-amc/v1/configuration/_create"
        headers = {
            "Content-Type": "application/json"
        }
        
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "AmcConfigurations": [configuration_payload],
            "apiOperation": "CREATE"
        }
        
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.HTTPError as http_err:
            error_detail = ""
            if hasattr(http_err.response, 'text'):
                try:
                    error_json = http_err.response.json()
                    error_detail = error_json.get("Errors", [{}])[0].get("message", str(http_err))
                except:
                    error_detail = http_err.response.text
            raise Exception(f"HTTP error {http_err.response.status_code}: {error_detail or str(http_err)}")
        except requests.exceptions.ConnectionError as conn_err:
            raise Exception(f"Connection error: {str(conn_err)}")
        except requests.exceptions.Timeout as timeout_err:
            raise Exception(f"Timeout error: {str(timeout_err)}")
        except requests.exceptions.RequestException as req_err:
            raise Exception(f"Request error: {str(req_err)}")

    def search_amc_configurations(self, request_info: RequestInfo, facility_id: str = None, project_id: str = None, vendor: str = None) -> Dict[str, Any]:
        """
        Search for existing AMC configurations
        """
        url = f"{self.amc_scheduler_service_url}/asset-amc/v1/configuration/_search"
        headers = {
            "Content-Type": "application/json"
        }
        
        search_criteria = {}
        if facility_id:
            search_criteria["facilityId"] = facility_id
        if project_id:
            search_criteria["projectId"] = project_id
        if vendor:
            search_criteria["vendor"] = vendor
        
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "AmcConfiguration": search_criteria
        }
        
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.HTTPError as http_err:
            error_detail = ""
            if hasattr(http_err.response, 'text'):
                try:
                    error_json = http_err.response.json()
                    error_detail = error_json.get("Errors", [{}])[0].get("message", str(http_err))
                except:
                    error_detail = http_err.response.text
            raise Exception(f"HTTP error {http_err.response.status_code}: {error_detail or str(http_err)}")
        except requests.exceptions.ConnectionError as conn_err:
            raise Exception(f"Connection error: {str(conn_err)}")
        except requests.exceptions.Timeout as timeout_err:
            raise Exception(f"Timeout error: {str(timeout_err)}")
        except requests.exceptions.RequestException as req_err:
            raise Exception(f"Request error: {str(req_err)}")