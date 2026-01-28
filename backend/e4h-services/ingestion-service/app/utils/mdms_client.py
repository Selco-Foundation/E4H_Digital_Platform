from typing import List, Dict, Any

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import IngestionSchemaResponse

logger = AppLogger().get_logger()


class MDMSClient:
    def __init__(self, mdms_url: str):
        self.mdms_url = mdms_url

    def fetch_schema(self, request_info: RequestInfo, schema_code: str) -> 'IngestionSchemaResponse':
        logger.trace(f"Fetching schema from MDMS: {schema_code}")
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
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            logger.info(f"Successfully fetched schema: {schema_code}")
            logger.debug(f"Schema fetch response status: {response.status_code}")
            return IngestionSchemaResponse.model_validate(response.json())
        except requests.exceptions.RequestException as e:
            logger.error(f"Error fetching schema {schema_code} from MDMS: {e}", exc_info=True)
            raise

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
        logger.trace(f"Getting column definitions with metadata for schema: {schema_code}")
        response = self.fetch_schema_column_definitions(request_info, schema_code)

        if not response.mdms or not response.mdms[0].data:
            logger.warning(f"No data found for schema: {schema_code}")
            return []

        result = []
        columns = response.mdms[0].data.columns or []
        logger.debug(f"Processing {len(columns)} columns for schema: {schema_code}")

        for col in columns:
            column_info = {
                "name": col.name,
                "type": col.type,
                "required": col.required,
                "pattern": col.pattern,
                "mdms_values": [],
                "code": col.code
            }

            if col.mdmsSource:
                dependent_schema_code = f"{col.mdmsSource.module}.{col.mdmsSource.master}"
                logger.trace(f"Fetching dependent schema for column {col.name}: {dependent_schema_code}")
                mdms_response = self.fetch_schema_column_definitions(request_info, dependent_schema_code)
                if mdms_response.mdms:
                    column_info["mdms_values"] = [mdms.data.model_dump() for mdms in mdms_response.mdms if mdms.data]
                    logger.debug(f"Found {len(column_info['mdms_values'])} MDMS values for column {col.name}")

            result.append(column_info)

        logger.info(f"Retrieved {len(result)} column definitions for schema: {schema_code}")
        return result


    def get_column_definitions_and_row_constraints_with_metadata(self, request_info: RequestInfo, schema_code: str) -> Dict[str, Any]:
        response = self.fetch_schema_column_definitions(request_info, schema_code)

        if not response.mdms or not response.mdms[0].data:
            return {}

        result = {}
        columns = response.mdms[0].data.columns or []
        row_constraints = response.mdms[0].data.rowConstraints or []
        result["row_constraints"] = row_constraints
        column_list = []

        for col in columns:
            column_info = {
                "name": col.name,
                "type": col.type,
                "required": col.required,
                "pattern": col.pattern,
                "mdms_values": [],
                "code": col.code
            }

            if col.mdmsSource:
                dependent_schema_code = f"{col.mdmsSource.module}.{col.mdmsSource.master}"
                mdms_response = self.fetch_schema_column_definitions(request_info, dependent_schema_code)
                if mdms_response.mdms:
                    column_info["mdms_values"] = [mdms.data.model_dump() for mdms in mdms_response.mdms if mdms.data]

            column_list.append(column_info)
        result["column_list"] = column_list
        return result

    def get_tenant_mapping(self, request_info: RequestInfo, tenant_ids: List[str]) -> Dict:
        logger.trace(f"Fetching tenant mapping for {len(tenant_ids)} tenants")
        all_tenant_data = {}

        for tenant_id in tenant_ids:
            logger.debug(f"Fetching tenant data for: {tenant_id}")
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
            try:
                response = requests.post(search_url, json=search_payload)
                if response.status_code in [200, 201, 202]:
                    data = response.json()
                    tenants = data.get("MdmsRes", {}).get("tenant", {}).get("tenants", [])
                    all_tenant_data.update(
                        {t["code"]: t for t in tenants if t.get("code") and t["code"] not in all_tenant_data})
                    logger.debug(f"Retrieved {len(tenants)} tenants for tenant_id: {tenant_id}")
                else:
                    logger.warning(f"Failed to fetch tenant data for {tenant_id}: status {response.status_code}")
            except requests.exceptions.RequestException as e:
                logger.error(f"Error fetching tenant data for {tenant_id}: {e}", exc_info=True)

        logger.info(f"Retrieved tenant mapping for {len(all_tenant_data)} tenants")
        return all_tenant_data