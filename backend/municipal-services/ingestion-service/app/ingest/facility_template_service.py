# services/facility_service.py
import os
from typing import Dict, List, Any
import pandas as pd
import logging
from fastapi import Depends

from app.core.logging import AppLogger
from app.schemas.boundary_code import BoundaryResponse
from app.schemas.facility_ingestion_schema_response import FacilityIngestionSchemaResponse
from app.utils.file_utils import FileUtils
from app.utils.http_client import HttpClientInterface, AsyncHttpClient

logger = AppLogger().get_logger()
from dotenv import load_dotenv
load_dotenv()
mdms_url = os.getenv("MDMS_URL")
boundary_service_url = os.getenv("BOUNDARY_URL")

class FacilityTemplateService:
    def __init__(
            self,
            http_client: HttpClientInterface = Depends(AsyncHttpClient),
            file_utils: FileUtils = Depends(),
    ):
        self.http_client = http_client
        self.file_utils = file_utils

    async def get_facility_schema(self) -> FacilityIngestionSchemaResponse:
        try:
            json_data = {
                "RequestInfo": {},
                "moduleName": "data-ingestion",
                "masterName": "FacilityIngestionSchema"
            }
            response_data = self.http_client.post_sync(
                f"{mdms_url}/_search",
                json=json_data
            )
            return FacilityIngestionSchemaResponse(mdms_response=response_data)
        except Exception as e:
            logger.error(f"Failed to fetch facility schema: {e}")
            raise Exception(f"Failed to fetch facility schema: {str(e)}")

    async def get_boundary_data(self) -> BoundaryResponse:
        """Fetch boundary data from boundary service"""
        try:
            json_data = {
                "RequestInfo": {},
                "hierarchyTypeCode": "ADMIN",
                "boundaryType": "Block"
            }

            response_data = self.http_client.post_sync(
                f"{boundary_service_url}/boundary/_search",
                json=json_data
            )
            return BoundaryResponse(**response_data)
        except Exception as e:
            logger.error(f"Failed to fetch boundary data: {e}")
            raise Exception(f"Failed to fetch boundary data: {str(e)}")

    def generate_template_file(self, output_path: str,
                               facility_schema: FacilityIngestionSchemaResponse,
                               boundary_data: BoundaryResponse) -> None:
        try:
            self.file_utils.create_empty_excel_file(output_path)
            schema_columns = facility_schema.columns
            df_facility = pd.DataFrame(columns=schema_columns)
            facility_writer = self.file_utils.create_excel_data_writer(
                output_path,
                "FacilityIngestionTemplate"
            )
            facility_writer.write_data(df_facility)

            # Write boundary data to the second sheet
            boundary_records = self._format_boundary_data(boundary_data)
            df_boundary = pd.DataFrame(boundary_records)
            boundary_writer = self.file_utils.create_excel_data_writer(
                output_path,
                "BlockBoundaryCodes"
            )
            boundary_writer.write_data(df_boundary)

            logger.info(f"Successfully created template file at {output_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            raise

    def _format_boundary_data(self, boundary_data: BoundaryResponse) -> List[Dict[str, str]]:
        """Format boundary data into required structure"""
        boundary_records = []

        boundaries = boundary_data.boundaries
        for boundary in boundaries:
            boundary_records.append({
                "Country": boundary.get("country", ""),
                "State": boundary.get("state", ""),
                "District": boundary.get("district", ""),
                "Block": boundary.get("block", ""),
                "BoundaryCode": boundary.get("code", "")
            })
        if not boundary_records:
            boundary_records.append({
                "Country": "India",
                "State": "Telangana",
                "District": "Hyderabad",
                "Block": "Block 1",
                "BoundaryCode": "<boundary-code>"
            })

        return boundary_records