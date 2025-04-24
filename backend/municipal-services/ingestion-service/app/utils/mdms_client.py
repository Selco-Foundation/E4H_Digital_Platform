import requests

from app.schemas.mdms_data_response import MDMSDataResponse
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import VendorIngestionSchemaResponse
from app.utils.convertor import convert_json_to_object
from app.utils.http_client import HttpClientInterface


class MDMSClient:
    def __init__(self, mdms_url: str):
        self.mdms_url = mdms_url

    def fetch_vendor_schema(self, request_info: RequestInfo) -> 'VendorIngestionSchemaResponse':
        url = f"{self.mdms_url}/egov-mdms-service/v2/_search"
        payload = {
            "RequestInfo": {"authToken": request_info.auth_token},
            "MdmsCriteria": {
                "tenantId": "in",
                "schemaCode": "data-ingestion.VendorIngestionSchema"
            }
        }
        headers = {
            "Accept": "application/json, text/plain, */*",
        }
        response = requests.post(url, headers=headers, json=payload)
        return convert_json_to_object(response.text)

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
