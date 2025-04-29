import json
from typing import Dict, Any, List, Optional

import requests

from app.schemas.request_info import RequestInfo


class BoundaryServiceClient:
    def __init__(self, boundary_service_url: str):
        self.boundary_service_url = boundary_service_url

    def create_boundaries(self, request_info: RequestInfo, boundary_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Create new boundaries.

        Args:
            request_info: The request info object containing auth details.
            boundary_data: A list of dictionaries, where each dictionary represents
                           the data for a boundary to be created.

        Returns:
            The API response after attempting to create the boundaries.
        """
        url = f"{self.boundary_service_url}/boundary-service/boundary/_create"

        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "Boundary": boundary_data
        }

        boundary_response = requests.post(url, json=payload)
        boundary_response.raise_for_status()
        return boundary_response.json()

    def search_boundaries(self, request_info: RequestInfo, tenant_id: str, codes: List[str]) -> Dict[str, Any]:
        """
        Search for boundaries by their codes.

        Args:
            request_info: The request info object containing auth details
            tenant_id: The tenant ID to search within
            codes: List of boundary codes to search for

        Returns:
            The API response with matching boundaries
        """
        # Convert list of codes to comma-separated string and URL encode
        codes_param = "%2C".join(codes)

        url = f"{self.boundary_service_url}/boundary-service/boundary/_search?tenantId={tenant_id}&codes={codes_param}"
        headers = {
            'Content-Type': 'application/json'
        }

        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True)
        }

        response = requests.post(url, headers=headers, json=payload)
        response.raise_for_status()
        return response.json()

    def create_boundary_relationship(self, request_info: RequestInfo, tenant_id: str,
                                     code: str, hierarchy_type: str, boundary_type: str,
                                     parent: Optional[str] = None) -> Dict[str, Any]:
        """
        Create a boundary relationship.

        Args:
            request_info: The request info object containing auth details
            tenant_id: The tenant ID
            code: The boundary code
            hierarchy_type: Type of hierarchy (e.g., "SELCO")
            boundary_type: Type of boundary (e.g., "Country", "State", etc.)
            parent: Parent boundary code, None for top-level boundaries

        Returns:
            The API response
        """
        url = f"{self.boundary_service_url}/boundary-service/boundary-relationships/_create"
        headers = {
            'Content-Type': 'application/json'
        }

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

        response = requests.post(url, headers=headers, json=payload)
        return response.json()