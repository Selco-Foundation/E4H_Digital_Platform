import json
from typing import Dict, Any, List

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo

logger = AppLogger().get_logger()


class AMCSchedulerServiceClient:
    def __init__(self, amc_scheduler_service_url: str):
        self.amc_scheduler_service_url = amc_scheduler_service_url

    def create_amc_configuration(self, request_info: RequestInfo, configuration_payload: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create AMC configuration via AMC Scheduler Service
        """
        facility_id = configuration_payload.get("facilityId", "unknown")
        project_id = configuration_payload.get("projectId", "unknown")
        logger.trace(f"Creating AMC configuration: facility_id={facility_id}, project_id={project_id}")
        
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
            logger.info(f"AMC configuration created successfully: facility_id={facility_id}, project_id={project_id}")
            logger.debug(f"Create response status: {response.status_code}")
            return response.json()
        except requests.exceptions.HTTPError as http_err:
            error_detail = ""
            if hasattr(http_err.response, 'text'):
                try:
                    error_json = http_err.response.json()
                    error_detail = error_json.get("Errors", [{}])[0].get("message", str(http_err))
                except:
                    error_detail = http_err.response.text
            logger.error(f"HTTP error creating AMC configuration: {http_err.response.status_code} - {error_detail}", exc_info=True)
            raise Exception(f"HTTP error {http_err.response.status_code}: {error_detail or str(http_err)}")
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating AMC configuration: {conn_err}", exc_info=True)
            raise Exception(f"Connection error: {str(conn_err)}")
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating AMC configuration: {timeout_err}", exc_info=True)
            raise Exception(f"Timeout error: {str(timeout_err)}")
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating AMC configuration: {req_err}", exc_info=True)
            raise Exception(f"Request error: {str(req_err)}")

    def search_amc_configurations(self, request_info: RequestInfo, facility_id: str = None, project_id: str = None, vendor: str = None) -> Dict[str, Any]:
        """
        Search for existing AMC configurations
        """
        logger.trace(f"Searching AMC configurations: facility_id={facility_id}, project_id={project_id}, vendor={vendor}")
        
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
            result = response.json()
            config_count = len(result.get("AmcConfigurations", []))
            logger.info(f"AMC configuration search completed: {config_count} configurations found")
            logger.debug(f"Search response status: {response.status_code}")
            return result
        except requests.exceptions.HTTPError as http_err:
            error_detail = ""
            if hasattr(http_err.response, 'text'):
                try:
                    error_json = http_err.response.json()
                    error_detail = error_json.get("Errors", [{}])[0].get("message", str(http_err))
                except:
                    error_detail = http_err.response.text
            logger.error(f"HTTP error searching AMC configurations: {http_err.response.status_code} - {error_detail}", exc_info=True)
            raise Exception(f"HTTP error {http_err.response.status_code}: {error_detail or str(http_err)}")
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching AMC configurations: {conn_err}", exc_info=True)
            raise Exception(f"Connection error: {str(conn_err)}")
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching AMC configurations: {timeout_err}", exc_info=True)
            raise Exception(f"Timeout error: {str(timeout_err)}")
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching AMC configurations: {req_err}", exc_info=True)
            raise Exception(f"Request error: {str(req_err)}")