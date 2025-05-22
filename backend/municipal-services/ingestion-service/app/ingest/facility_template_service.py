# services/facility_service.py
import os
from typing import Dict, List, Any

import pandas as pd
import requests
from fastapi import HTTPException
from openpyxl import load_workbook
from openpyxl.worksheet.datavalidation import DataValidation
from sqlalchemy import false

from app.core.logging import AppLogger
from app.schemas.boundary import Boundary
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import IngestionSchemaResponse
from app.utils.convertor import convert_json_to_boundary
from app.utils.excel_utils import add_dropdowns_to_excel, lock_excel_columns
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
            "hierarchyType": "SELCO",
            "boundaryType": "Block"
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
                               facility_schema: List[Dict[str, Any]],
                               boundary_data: List[Boundary]
                               ) -> None:
        try:
            create_empty_excel_file(output_path)

            output_list = []
            dropdowns_map = {}
            for col in facility_schema:
                mandatory_indicator = "(Mandatory)" if col.get("required") else ""
                header_name = f"{col.get('name')} {mandatory_indicator}".strip()
                output_list.append(header_name)

                mdms_values = col.get("mdms_values")
                if mdms_values:
                    dropdown_options = [item.get("name") for item in mdms_values if item.get("name")]
                    if dropdown_options:
                        dropdowns_map[header_name] = dropdown_options

            df_facility = pd.DataFrame(columns=output_list)
            facility_writer = create_excel_data_writer(
                output_path,
                "FacilityIngestionTemplate"
            )
            facility_writer.write_data(df_facility)

            add_dropdowns_to_excel(
                file_path=output_path,
                sheet_name="FacilityIngestionTemplate",
                dropdowns=dropdowns_map,
                allow_blank=True
            )

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

    def add_supervisor_columns_to_facility_template(self,input_path, output_path, sheet_name):
        """
        Add supervisor columns to the specified Excel sheet if they don't already exist.

        Args:
            input_path: Path to the input Excel file
            output_path: Path where the modified Excel file will be saved
            sheet_name: Name of the sheet to modify

        Raises:
            HTTPException: If the specified sheet is not found
        """
        try:
            df = pd.read_excel(input_path, sheet_name=sheet_name)
            columns_to_add = {
                "Role (Mandatory)": "Supervisor",
                "Name (Mandatory)": None,
                "Phone Number (Mandatory)": None,
                "Email Address (Mandatory)": None
            }

            for col_name, default_value in columns_to_add.items():
                if col_name not in df.columns:
                    df[col_name] = default_value

            with pd.ExcelWriter(output_path, engine='openpyxl') as writer:
                all_sheets = pd.read_excel(input_path, sheet_name=None)
                for sheet, sheet_df in all_sheets.items():
                    if sheet == sheet_name:
                        df.to_excel(writer, sheet_name=sheet, index=False)
                    else:
                        sheet_df.to_excel(writer, sheet_name=sheet, index=False)

        except ValueError as e:
            if "No sheet named" in str(e):
                raise HTTPException(status_code=400, detail=f"Sheet '{sheet_name}' not found in the uploaded file")
            raise e

    def generate_selection_template_file(self, output_path: str,
                                         facility_selection_schema: IngestionSchemaResponse,
                                         facility_data: List[Dict[str, Any]]) -> None:
        try:
            create_empty_excel_file(output_path)

            schema_columns = facility_selection_schema.mdms[0].data.columns
            column_names = [col.name.strip() for col in schema_columns]

            records = []
            for facility in facility_data:
                address = facility.get("address", {})
                details = facility.get("facility_details", {})

                record = {
                    "Country": "India",
                    "State": address.get("state", ""),
                    "District": address.get("district", ""),
                    "Block": address.get("block", ""),
                    "Boundary Code": details.get("boundaryCode", ""),
                    "Health Centre Name": facility.get("facility_name", ""),
                    "HC ID": facility.get("facility_id", ""),
                    "Type of HC": facility.get("facility_type", ""),
                    "HFR ID": details.get("hfrId", ""),
                    "NIN ID": details.get("ninId", ""),
                    "Selection?": ""  # dropdown will be added
                }

                records.append(record)

            df_facility = pd.DataFrame(records, columns=column_names)

            df_facility.to_excel(output_path, sheet_name="Facility Selection Template", index=False)

            dropdowns_map = {'Selection?': ['Yes', 'No']}

            add_dropdowns_to_excel(
                file_path=output_path,
                sheet_name="Facility Selection Template",
                dropdowns=dropdowns_map,
                allow_blank=False
            )

            lock_excel_columns(
                file_path=output_path,
                sheet_name="Facility Selection Template",
                column_headers_to_unlock=[ "Selection?"]
            )

            logger.info(f"Successfully created template file at {output_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            raise