import requests

from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import VendorIngestionSchemaResponse
from app.utils.convertor import convert_json_to_object


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
