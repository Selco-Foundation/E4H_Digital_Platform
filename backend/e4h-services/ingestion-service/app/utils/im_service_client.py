import requests
import logging

logger = logging.getLogger(__name__)


class IMServiceClient:
    def __init__(self, base_url):
        self.base_url = base_url

    def search_incident(self, incident_id: str, tenant_id: str, request_info: dict):
        url = f"{self.base_url}/im-services/v2/request/_search?tenantId={tenant_id}&incidentId={incident_id}"
        headers = {
            "Content-Type": "application/json;charset=UTF-8",
            "Accept": "application/json"
        }
        payload = {"RequestInfo": request_info}
        try:
            logger.info(
                "Searching incident",
                extra={"incident_id": incident_id, "tenant_id": tenant_id, "url": url},
            )
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            result = response.json()
            logger.info(
                "Incident search successful",
                extra={"incident_id": incident_id, "tenant_id": tenant_id, "status_code": response.status_code},
            )
            return result
        except requests.exceptions.RequestException as err:
            logger.error(
                "Incident search failed",
                extra={"incident_id": incident_id, "tenant_id": tenant_id, "error": str(err)},
            )
            raise

    def update_incident(self, payload: dict):
        url = f"{self.base_url}/im-services/v2/request/_update"
        headers = {
            "Content-Type": "application/json"
        }
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            result = response.json()
            logger.info(
                "Incident update successful",
                extra={"status_code": response.status_code},
            )
            return result
        except requests.exceptions.RequestException as err:
            logger.error(
                "Incident update failed",
                extra={"error": str(err)},
            )
            raise

    def update_incident_data(self, payload: dict):
        url = f"{self.base_url}/im-services/v2/request/migration/_update"
        headers = {
            "Content-Type": "application/json"
        }
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            result = response.json()
            logger.info(
                "Incident data update successful",
                extra={"status_code": response.status_code},
            )
            return result
        except requests.exceptions.RequestException as err:
            logger.error(
                "Incident data update failed",
                extra={"error": str(err)},
            )
            raise