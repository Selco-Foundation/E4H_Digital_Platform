from typing import Any, Dict, List, Optional

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo

logger = AppLogger().get_logger()

_AMC_HTTP_TIMEOUT = (30, 180)


class AMCSchedulerServiceClient:
    def __init__(self, amc_scheduler_service_url: str):
        self.amc_scheduler_service_url = amc_scheduler_service_url

    @staticmethod
    def _post(
        session: Optional[requests.Session],
        url: str,
        *,
        headers: Dict[str, str],
        json: Any,
        timeout: tuple,
        params: Optional[Dict[str, Any]] = None,
    ):
        """Use caller-provided Session for connection reuse, or one-off requests.post."""
        if session is not None:
            return session.post(url, headers=headers, json=json, params=params, timeout=timeout)
        return requests.post(url, headers=headers, json=json, params=params, timeout=timeout)

    def create_amc_configuration(
        self,
        request_info: RequestInfo,
        configuration_payload: Dict[str, Any],
        session: Optional[requests.Session] = None,
    ) -> Dict[str, Any]:
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
        }

        try:
            response = self._post(
                session, url, headers=headers, json=payload, timeout=_AMC_HTTP_TIMEOUT
            )
            response.raise_for_status()
            logger.info(f"AMC configuration created successfully: facility_id={facility_id}, project_id={project_id}")
            logger.debug(f"Create response status: {response.status_code}")
            return response.json()
        except requests.exceptions.HTTPError as http_err:
            error_detail = ""
            if http_err.response is not None and hasattr(http_err.response, "text"):
                try:
                    error_json = http_err.response.json()
                    error_detail = error_json.get("Errors", [{}])[0].get("message", str(http_err))
                except Exception:
                    error_detail = http_err.response.text
            logger.error(
                f"HTTP error creating AMC configuration: {http_err.response.status_code} - {error_detail}",
                exc_info=True,
            )
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

    def create_amc_configurations_bulk(
        self,
        request_info: RequestInfo,
        configuration_payloads: List[Dict[str, Any]],
        session: Optional[requests.Session] = None,
    ) -> Dict[str, Any]:
        """
        Create multiple AMC configurations in one request.
        """
        logger.trace(f"Creating AMC configurations in bulk: count={len(configuration_payloads)}")
        url = f"{self.amc_scheduler_service_url}/asset-amc/v1/configuration/_create"
        headers = {
            "Content-Type": "application/json"
        }
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "AmcConfigurations": configuration_payloads,
        }
        try:
            response = self._post(
                session, url, headers=headers, json=payload, timeout=_AMC_HTTP_TIMEOUT
            )
            response.raise_for_status()
            logger.info(f"AMC bulk configuration create succeeded: count={len(configuration_payloads)}")
            logger.debug(f"Bulk create response status: {response.status_code}")
            return response.json()
        except requests.exceptions.HTTPError as http_err:
            error_detail = ""
            if http_err.response is not None and hasattr(http_err.response, "text"):
                try:
                    error_json = http_err.response.json()
                    error_detail = error_json.get("Errors", [{}])[0].get("message", str(http_err))
                except Exception:
                    error_detail = http_err.response.text
            logger.error(
                f"HTTP error bulk creating AMC configurations: {http_err.response.status_code} - {error_detail}",
                exc_info=True,
            )
            raise Exception(f"HTTP error {http_err.response.status_code}: {error_detail or str(http_err)}")
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error bulk creating AMC configurations: {conn_err}", exc_info=True)
            raise Exception(f"Connection error: {str(conn_err)}")
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error bulk creating AMC configurations: {timeout_err}", exc_info=True)
            raise Exception(f"Timeout error: {str(timeout_err)}")
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error bulk creating AMC configurations: {req_err}", exc_info=True)
            raise Exception(f"Request error: {str(req_err)}")

    def search_amc_configurations(
        self,
        request_info: RequestInfo,
        facility_id: str = None,
        facility_ids: Optional[List[str]] = None,
        project_id: str = None,
        vendor: str = None,
        tenant_id: str = "in",
        limit: int = 1000,
        offset: int = 0,
        session: Optional[requests.Session] = None,
    ) -> Dict[str, Any]:
        """
        Search for existing AMC configurations. tenantId is mandatory on the AmcConfigurationSearchCriteria
        contract, and facilityId/projectId/vendor filters are list-typed (facilityIds/projectIds/vendorIds),
        not singular strings.
        """
        resolved_facility_ids = facility_ids if facility_ids else ([facility_id] if facility_id else None)
        logger.trace(
            f"Searching AMC configurations: facility_ids={resolved_facility_ids}, project_id={project_id}, vendor={vendor}"
        )

        url = f"{self.amc_scheduler_service_url}/asset-amc/v1/configuration/_search"
        headers = {
            "Content-Type": "application/json"
        }

        search_criteria = {"tenantId": tenant_id}
        if resolved_facility_ids:
            search_criteria["facilityIds"] = resolved_facility_ids
        if project_id:
            search_criteria["projectIds"] = [project_id]
        if vendor:
            search_criteria["vendorIds"] = [vendor]

        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "searchCriteria": search_criteria,
        }
        params = {"tenantId": tenant_id, "limit": limit, "offset": offset}

        try:
            response = self._post(
                session, url, headers=headers, json=payload, params=params, timeout=_AMC_HTTP_TIMEOUT
            )
            response.raise_for_status()
            result = response.json()
            config_count = len(result.get("AmcConfigurations", []))
            logger.info(f"AMC configuration search completed: {config_count} configurations found")
            logger.debug(f"Search response status: {response.status_code}")
            return result
        except requests.exceptions.HTTPError as http_err:
            error_detail = ""
            if http_err.response is not None and hasattr(http_err.response, "text"):
                try:
                    error_json = http_err.response.json()
                    error_detail = error_json.get("Errors", [{}])[0].get("message", str(http_err))
                except Exception:
                    error_detail = http_err.response.text
            logger.error(
                f"HTTP error searching AMC configurations: {http_err.response.status_code} - {error_detail}",
                exc_info=True,
            )
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