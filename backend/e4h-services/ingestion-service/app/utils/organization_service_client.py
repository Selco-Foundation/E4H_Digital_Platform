from typing import Dict, Any

import requests

from app.core.logging import AppLogger

logger = AppLogger().get_logger()


class OrganizationServiceClient:
    def __init__(self, org_service_url: str):
        self.org_service_url = org_service_url

    def create_vendor(self, vendor_payload:Dict[str,Any]):
        url = f"{self.org_service_url}/vendor/organisation/v1/_create"
        headers = {
            "Content-Type": "application/json"
        }
        payload = vendor_payload
        logger.trace(f"Creating vendor in organization service: {url}")
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            logger.info("Vendor saved successfully in organization service")
            logger.debug(f"Vendor creation response status: {response.status_code}")
            return response.json()

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating vendor: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating vendor: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating vendor: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating vendor: {req_err}", exc_info=True)
            raise req_err