import json
from typing import Dict, Any

import requests

from app.core.logging import AppLogger

logger = AppLogger().get_logger()


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
        logger.trace(f"Creating user in HRMS: {url}")
        try:
            requests.post(url, headers=headers, params=params, json=user_payload)
            response = self.search_user(user_payload = user_payload)
            logger.info("User created successfully in HRMS")
            logger.debug(f"User creation response: {json.loads(response.text)}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating user in HRMS: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating user in HRMS: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating user in HRMS: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating user in HRMS: {req_err}", exc_info=True)
            raise req_err

    def search_user(self, user_payload: Dict[str, Any]):
        url = f"{self.hrms_service_url}/egov-hrms/employees/_search"
        headers = {
            "Content-Type": "application/json"
        }
        params = {
            "tenantId":"in",
            "phone": user_payload["Employees"][0]["user"]["mobileNumber"]
        }
        logger.trace(f"Searching user in HRMS: {url}")
        try:
            response = requests.post(url, headers=headers, params=params, json=user_payload)
            # response.raise_for_status()
            logger.info("User fetched successfully from HRMS")
            logger.debug(f"User search response: {json.loads(response.text)}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error searching user in HRMS: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching user in HRMS: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching user in HRMS: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching user in HRMS: {req_err}", exc_info=True)
            raise req_err