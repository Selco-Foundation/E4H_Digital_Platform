import json
import os
from typing import Dict, Any, List, Optional, Tuple

import requests
from requests.exceptions import HTTPError, ConnectionError, Timeout, RequestException

from app.schemas.request_info import RequestInfo
from app.core.logging import AppLogger

from dotenv import load_dotenv
load_dotenv()
time_out = int(os.getenv("TIME_OUT", "60"))

logger = AppLogger().get_logger()
class BoundaryServiceClient:
    def __init__(self, boundary_service_url: str):
        self.boundary_service_url = boundary_service_url

    @staticmethod
    def _extract_error_message(response_body: Optional[Dict[str, Any]], fallback: str) -> str:
        if not response_body:
            return fallback
        errors = response_body.get("Errors") or []
        if errors:
            messages = [
                err.get("message") or err.get("code") or str(err)
                for err in errors
                if isinstance(err, dict)
            ]
            if messages:
                return "; ".join(messages)
        return fallback

    def create_boundaries(
        self, request_info: RequestInfo, boundary_data: List[Dict[str, Any]]
    ) -> Tuple[Optional[Dict[str, Any]], Optional[str]]:
        """
        Create boundaries. Returns (response_body, error_message).
        error_message is set when the API reports failure (4xx or Errors in body).
        """
        url = f"{self.boundary_service_url}/boundary-service/boundary/_create"
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "Boundary": boundary_data
        }
        try:
            response = requests.post(url, json=payload, timeout=time_out)
            try:
                response_body = response.json()
            except ValueError:
                response_body = {}

            if response.status_code >= 400:
                error_message = self._extract_error_message(
                    response_body,
                    f"HTTP {response.status_code}: {response.text}",
                )
                logger.error(f"Boundary creation failed: {error_message}")
                return response_body, error_message

            if response_body.get("Errors"):
                error_message = self._extract_error_message(response_body, "Boundary creation failed")
                logger.error(f"Boundary creation returned errors: {error_message}")
                return response_body, error_message

            return response_body, None

        except HTTPError as e:
            response_body = {}
            if e.response is not None:
                try:
                    response_body = e.response.json()
                except ValueError:
                    response_body = {}
            error_message = self._extract_error_message(response_body, str(e))
            logger.error(f"HTTP error during boundary creation: {error_message}")
            return response_body, error_message
        except ConnectionError as e:
            logger.error(f"Connection error during boundary creation: {e}")
            raise
        except Timeout as e:
            logger.error(f"Timeout error during boundary creation: {e}")
            raise
        except RequestException as e:
            logger.error(f"Unexpected request error during boundary creation: {e}")
            raise

    def search_boundaries(self, request_info: RequestInfo, tenant_id: str, codes: List[str],
                          page_size: int = 300) -> Dict[str, Any]:
        """
        Search boundaries by codes (case-insensitive), paging through all results.
        The boundary service applies a default LIMIT (50) and, with ignoreCase, one code can
        match several case-variant rows, so a single unpaged call can silently drop codes.
        page_size must not exceed the service's boundary.max.default.limit (300).
        """
        codes_param = "%2C".join(codes)
        base_url = (
            f"{self.boundary_service_url}/boundary-service/boundary/_search"
            f"?tenantId={tenant_id}&codes={codes_param}&ignoreCase=true&limit={page_size}"
        )
        headers = {'Content-Type': 'application/json'}
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True)
        }

        try:
            result: Dict[str, Any] = {}
            boundaries: List[Dict[str, Any]] = []
            offset = 0
            while True:
                response = requests.post(f"{base_url}&offset={offset}", headers=headers,
                                         json=payload, timeout=time_out)
                response.raise_for_status()
                result = response.json() or {}
                page = result.get("Boundary") or []
                boundaries.extend(page)
                if len(page) < page_size:
                    break
                offset += page_size
            result["Boundary"] = boundaries
            return result

        except HTTPError as e:
            logger.error(f"HTTP error during boundary search: {e}")
            raise
        except ConnectionError as e:
            logger.error(f"Connection error during boundary search: {e}")
            raise
        except Timeout as e:
            logger.error(f"Timeout error during boundary search: {e}")
            raise
        except RequestException as e:
            logger.error(f"Unexpected request error during boundary search: {e}")
            raise

    def create_boundary_relationship(self, request_info: RequestInfo, tenant_id: str,
                                     code: str, hierarchy_type: str, boundary_type: str,
                                     parent: Optional[str] = None) -> Dict[str, Any]:
        url = f"{self.boundary_service_url}/boundary-service/boundary-relationships/_create"
        headers = {'Content-Type': 'application/json'}

        relationship = {
            "tenantId": tenant_id,
            "code": code,
            "hierarchyType": hierarchy_type,
            "boundaryType": boundary_type,
            "parent": parent
        }

        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "BoundaryRelationship": relationship
        }

        try:
            response = requests.post(url, headers=headers, json=payload, timeout=time_out)
            try:
                response_body = response.json()
            except ValueError:
                response_body = {}

            if response.status_code >= 400:
                error_message = self._extract_error_message(
                    response_body,
                    f"HTTP {response.status_code}: {response.text}",
                )
                logger.error(f"HTTP error during relationship creation for {code}: {error_message}")
                return {"Errors": response_body.get("Errors") or [{"message": error_message}]}

            return response_body

        except HTTPError as e:
            response_body = {}
            if e.response is not None:
                try:
                    response_body = e.response.json()
                except ValueError:
                    response_body = {}
            error_message = self._extract_error_message(response_body, str(e))
            logger.error(f"HTTP error during relationship creation for {code}: {error_message}")
            return {"Errors": response_body.get("Errors") or [{"message": error_message}]}
        except ConnectionError as e:
            logger.error(f"Connection error during relationship creation for {code}: {e}")
            raise
        except Timeout as e:
            logger.error(f"Timeout error during relationship creation for {code}: {e}")
            raise
        except RequestException as e:
            logger.error(f"Unexpected error during relationship creation for {code}: {e}")
            raise
