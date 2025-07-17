from typing import List, Dict, Any

import requests

from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import IngestionSchemaResponse
from app.utils.convertor import convert_json_to_object

class MDMSClient:
    def __init__(self, mdms_url: str):
        self.mdms_url = mdms_url

    def fetch_schema(self, request_info: RequestInfo, schema_code: str) -> 'IngestionSchemaResponse':
        url = f"{self.mdms_url}/egov-mdms-service/v2/_search"
        payload = {
            "RequestInfo": {"authToken": request_info.auth_token},
            "MdmsCriteria": {
                "tenantId": "in",
                "schemaCode": schema_code
            }
        }
        headers = {
            "Accept": "application/json, text/plain, */*",
        }
        response = requests.post(url, headers=headers, json=payload)
        # return convert_json_to_object(response.text)
        return IngestionSchemaResponse.model_validate(response.json())

    # Optionally, keep convenience methods for clarity
    def fetch_vendor_schema(self, request_info: RequestInfo) -> 'IngestionSchemaResponse':
        return self.fetch_schema(request_info, "data-ingestion.VendorIngestionSchema")

    def fetch_facility_schema(self, request_info: RequestInfo) -> 'IngestionSchemaResponse':
        return self.fetch_schema(request_info, "data-ingestion.FacilityIngestionSchema")

    def fetch_boundary_schema(self, request_info: RequestInfo) -> 'IngestionSchemaResponse':
        return self.fetch_schema(request_info, "data-ingestion.BoundaryIngestionSchema")

    def fetch_facility_selection_schema(self, request_info: RequestInfo) -> 'IngestionSchemaResponse':
        return self.fetch_schema(request_info, "data-ingestion.FacilitySelectionSchema")

    def fetch_schema_column_definitions(self, request_info: RequestInfo, schema_code: str) -> IngestionSchemaResponse:
        url = f"{self.mdms_url}/egov-mdms-service/v2/_search"
        payload = {
            "RequestInfo": {"authToken": request_info.auth_token},
            "MdmsCriteria": {
                "tenantId": "in",
                "schemaCode": schema_code
            }
        }
        headers = {"Accept": "application/json, text/plain, */*"}
        response = requests.post(url, headers=headers, json=payload)
        return IngestionSchemaResponse.model_validate(response.json())

    def get_column_definitions_with_metadata(self, request_info: RequestInfo, schema_code: str) -> List[Dict[str, Any]]:
        response = self.fetch_schema_column_definitions(request_info, schema_code)

        if not response.mdms or not response.mdms[0].data:
            return []

        result = []
        columns = response.mdms[0].data.columns or []

        for col in columns:
            column_info = {
                "name": col.name,
                "type": col.type,
                "required": col.required,
                "pattern": col.pattern,
                "mdms_values": []
            }

            if col.mdmsSource:
                dependent_schema_code = f"{col.mdmsSource.module}.{col.mdmsSource.master}"
                mdms_response = self.fetch_schema_column_definitions(request_info, dependent_schema_code)
                if mdms_response.mdms:
                    column_info["mdms_values"] = [mdms.data.model_dump() for mdms in mdms_response.mdms if mdms.data]

            result.append(column_info)

        return result

    def get_tenant_mapping(self, request_info: RequestInfo, tenant_ids: List[str]) -> Dict:
        all_tenant_data = {}

        for tenant_id in tenant_ids:
            search_url = f"{self.mdms_url}/egov-mdms-service/v1/_search"
            search_payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "MdmsCriteria": {
                    "tenantId": tenant_id,
                    "moduleDetails": [
                        {
                            "moduleName": "tenant",
                            "masterDetails": [
                                {
                                    "name": "tenants"
                                }
                            ]
                        }
                    ]
                }
            }
            response = requests.post(search_url, json=search_payload)
            if response.status_code == 200:
                data = response.json()
                tenants = data.get("MdmsRes", {}).get("tenant", {}).get("tenants", [])
                all_tenant_data.update(
                    {t["code"]: t for t in tenants if t.get("code") and t["code"] not in all_tenant_data})

        return all_tenant_data