from celery.worker.state import requests

from app.schemas.mdms_data_response import MDMSDataResponse
from app.schemas.vendor_ingestion_shema_response import VendorIngestionSchemaResponse


class MDMSClient:
    def __init__(self, mdms_url: str):
        self.mdms_url = mdms_url

    def fetch_vendor_schema(self) -> VendorIngestionSchemaResponse:
        schema_response = requests.post(
            f"{self.mdms_url}/_search",
            json={"RequestInfo": {}, "moduleName": "data-ingestion", "masterName": "VendorIngestionSchema"},
        )
        schema_response.raise_for_status()
        return VendorIngestionSchemaResponse(**schema_response.json())

    def fetch_mdms_data(self, module: str, master: str, tenant_id: str = "in") -> MDMSDataResponse:
        mdms_data_response = requests.post(
            f"{self.mdms_url}/_search",
            json={
                "RequestInfo": {},
                "moduleName": module,
                "masterName": master,
                "tenantId": tenant_id
            },
        )
        mdms_data_response.raise_for_status()
        return MDMSDataResponse(**mdms_data_response.json())
