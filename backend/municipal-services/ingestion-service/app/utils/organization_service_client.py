from typing import Dict, Any

from celery.worker.state import requests

from app.schemas.request_info import RequestInfo


class OrganizationServiceClient:
    def __init__(self, org_service_url: str):
        self.org_service_url = org_service_url

    def create_vendor(self, request_info:RequestInfo,vendor_data: Dict[str, Any]) -> Dict[str, Any]:
        org_response = requests.post(
            f"{self.org_service_url}/org-services/organisation/v1/_create",
            json={"RequestInfo": request_info, "Organisations": [vendor_data]},
        )
        org_response.raise_for_status()
        return org_response.json()