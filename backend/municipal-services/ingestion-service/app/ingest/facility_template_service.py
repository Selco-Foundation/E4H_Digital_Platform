# services/facility_service.py
import os
from typing import Dict, List

import pandas as pd
import requests

from app.core.logging import AppLogger
from app.schemas.boundary import Boundary
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import IngestionSchemaResponse
from app.utils.convertor import convert_json_to_boundary
from app.utils.file_utils import create_empty_excel_file, create_excel_data_writer

logger = AppLogger().get_logger()
from dotenv import load_dotenv
load_dotenv()
mdms_url = os.getenv("MDMS_URL")
boundary_service_url = os.getenv("BOUNDARY_SERVICE_URL")

class FacilityTemplateService:

    def get_all_boundaries(self, request_info: RequestInfo) -> List[Boundary]:
        url = f"{boundary_service_url}/boundary-service/boundary/getAllBoundaries"
        params = {
            "page": 0,
            "size": 20000,
            "tenantId": "in",
            "hierarchyType": "SELCO"
        }
        payload = {
            "apiId": "org.egov.boundary",
            "ver": "1.0",
            "ts": "",
            "action": "search",
            "did": "",
            "key": "",
            "msgId": "",
            "authToken": request_info.auth_token
        }

        headers = {
            "Content-Type": "application/json",
            "Accept": "application/json, text/plain, */*"
        }
        response = requests.get(url, params=params, headers=headers, json=payload)
        return convert_json_to_boundary(response.text)

    def generate_template_file(self, output_path: str,
                               facility_schema: IngestionSchemaResponse,
                               boundary_data: List[Boundary]
                               ) -> None:
        try:
            create_empty_excel_file(output_path)
            schema_columns = facility_schema.mdms[0].data.columns

            output_list = []
            for col in schema_columns:
                mandatory_indicator = "(Mandatory)" if col.required else ""
                output_list.append(f"{col.name} {mandatory_indicator}".strip())
            df_facility = pd.DataFrame(columns=output_list)

            facility_writer = create_excel_data_writer(
                output_path,
                "FacilityIngestionTemplate"
            )
            facility_writer.write_data(df_facility)
            boundary_records = self._format_boundary_data(boundary_data)
            df_boundary = pd.DataFrame(boundary_records)
            boundary_writer = create_excel_data_writer(
                output_path,
                "BlockBoundaryCodes"
            )
            boundary_writer.write_data(df_boundary)

            logger.info(f"Successfully created template file at {output_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            raise

    def _format_boundary_data(self, boundary_data: List[Boundary]) -> List[Dict[str, str]]:
        """Format boundary data into required structure"""
        boundary_records = []
        for boundary in boundary_data:
            boundary_records.append({
                "Country": boundary.get("country", ""),
                "State": boundary.get("state", ""),
                "District": boundary.get("district", ""),
                "Block": boundary.get("block", ""),
                "BoundaryCode": boundary.get("code", "")
            })
        if not boundary_records:
            boundary_records.append({
                "Country": "",
                "State": "",
                "District": "",
                "Block": "",
                "BoundaryCode": ""
            })

        return boundary_records