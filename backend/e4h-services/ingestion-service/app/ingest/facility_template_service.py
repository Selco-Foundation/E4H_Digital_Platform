# services/facility_service.py
import os
from typing import Dict, List, Any

import pandas as pd
import requests

from app.core.logging import AppLogger
from app.schemas.boundary import Boundary
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import IngestionSchemaResponse
from app.utils.convertor import convert_json_to_boundary, format_facility_data_for_template
from app.utils.excel_utils import add_dropdowns_to_excel, lock_excel_columns, add_validations_to_excel, \
    lock_prefilled_rows_in_excel, add_non_blank_validations_to_file, autofit_columns
from app.utils.file_utils import create_empty_excel_file, create_excel_data_writer, remove_default_empty_sheet

logger = AppLogger().get_logger()
from dotenv import load_dotenv
load_dotenv()
mdms_url = os.getenv("MDMS_URL")
boundary_service_url = os.getenv("BOUNDARY_SERVICE_URL")
vendor_service_url = os.getenv("VENDOR_SERVICE_URL")

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


    def generate_template_file_with_data(self, output_path: str,
                               facility_schema: List[Dict[str, Any]],
                               boundary_list: List[Boundary],
                               facility_data: List[Dict[str, Any]],
                               extra_append_rows: int,
                               project_id: str = None
                               ) -> None:
        """
            Generates FacilityIngestionTemplate.xlsx with:
            - Facility schema columns (with mandatory indicators)
            - Dropdowns (from MDMS + Yes/No types)
            - Regex validation comments (for pattern columns)
            - Boundary data sheet
            - Existing facility data sheet
            """
        try:
            create_empty_excel_file(output_path)

            # 1. Prepare headers + dropdowns + validation map
            output_list = []
            dropdowns_map = {}
            column_validations = {}
            editable_columns = []
            allow_blank_map = {}
            always_locked_columns=[]

            for col in facility_schema:
                mandatory_indicator = "(Mandatory)" if col.get("required") else ""
                header_name = f"{col.get('name')} {mandatory_indicator}".strip()
                output_list.append(header_name)

                allow_blank_map[header_name] = not col.get("required", False)

                # --- 1. MDMS Dropdowns ---
                mdms_values = col.get("mdms_values")
                if mdms_values:
                    dropdown_options = [item.get("name") for item in mdms_values if item.get("name")]
                    if dropdown_options:
                        dropdowns_map[header_name] = dropdown_options

                # --- 2. Yes/No Dropdowns ---
                if col.get("type", "") in ["enum-yes-no"]:
                    dropdowns_map[header_name] = ["Yes", "No"]
                    editable_columns.append(header_name)

                # --- 3. Pattern Validation ---
                if col.get("pattern"):
                    column_validations[header_name] = {
                        "type": "regex",
                        "pattern": col["pattern"],
                        "message": f"Must match pattern: {col['pattern']}"
                    }

                # --- 4. Unique Validation (cannot be enforced in Excel, add hint) ---
                if col.get("type") in ["Unique_Id"]:
                    column_validations[header_name] = {
                        "type": "unique",
                        "message": "Values must be unique across rows"
                    }

                # --- 5. Locking Auto Gen Id columns (cannot be enforced in Excel, add hint) ---
                if col.get('type') in ["system_generated_id"]:
                    always_locked_columns.append(header_name)

            # Debug: Log all columns before adding Include in Project
            logger.info(f"Columns from schema: {output_list}")

            # Check if "Include in Project" column already exists (with or without "(Mandatory)")
            existing_include_column = None
            for col in output_list:
                if "Include in Project" in col:
                    existing_include_column = col
                    break

            if existing_include_column:
                # Use the existing column
                include_column = existing_include_column
                dropdowns_map[include_column] = ["Yes", "No"]
                editable_columns.append(include_column)
                logger.info(f"Using existing column: {include_column}")
            # else:
                # Add new "Include in Project" column
                # include_column = "Include in Project"
                # output_list.append(include_column)
                # dropdowns_map[include_column] = ["Yes", "No"]
                # editable_columns.append(include_column)
                # logger.info(f"Added new column: {include_column}")

            logger.info(f"Final columns: {output_list}")

            # Add Existing Facilities Sheet (Optional)
            formatted_facilities = []
            if facility_data:
                formatted_facilities = format_facility_data_for_template(facility_data, facility_schema, output_list, project_id)

            df_facility = pd.DataFrame(formatted_facilities, columns=output_list)
            facility_writer = create_excel_data_writer(
                output_path,
                "FacilityMapping"
            )
            facility_writer.write_data(df_facility)

            # Add Dropdowns
            add_dropdowns_to_excel(
                file_path=output_path,
                sheet_name="FacilityMapping",
                dropdowns=dropdowns_map,
                allow_blank_map=allow_blank_map,
                max_extra_rows= extra_append_rows
            )

            # Add Validations (Regex + Unique) as comments/hints
            add_validations_to_excel(
                file_path=output_path,
                sheet_name="FacilityMapping",
                validations=column_validations,
                allow_blank_map=allow_blank_map,
                max_extra_rows=extra_append_rows
            )

            # Add Boundary Data Sheet
            boundary_records = self._format_boundary_data(boundary_list)
            df_boundary = pd.DataFrame(boundary_records)
            boundary_writer = create_excel_data_writer(
                output_path,
                "BoundaryCodes"
            )
            boundary_writer.write_data(df_boundary)

            lock_excel_columns(
                file_path=output_path,
                sheet_name="BoundaryCodes",
                column_headers_to_unlock=[]
            )


            # Lock prefilled rows except editable columns
            lock_prefilled_rows_in_excel(
                file_path=output_path,
                sheet_name="FacilityMapping",
                editable_columns=editable_columns,
                total_rows=len(formatted_facilities),
                total_columns=len(output_list),
                always_locked_columns=always_locked_columns,
                extra_append_rows=extra_append_rows
            )
            add_non_blank_validations_to_file(
                file_path=output_path,
                sheet_name="FacilityMapping",
                facility_schema=facility_schema,
                allow_blank_map=allow_blank_map
            )

            autofit_columns(
                file_path=output_path,
                sheet_name="FacilityMapping",
            )
            autofit_columns(
                file_path=output_path,
                sheet_name="BoundaryCodes",
            )
            remove_default_empty_sheet(output_path)
            logger.info(f"Successfully created template file at {output_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            raise


    def generate_template_file(self, output_path: str,
                               facility_schema: List[Dict[str, Any]],
                               boundary_data: List[Boundary],
                               vendor_data: List[Dict]
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
                dropdowns=dropdowns_map
            )

            boundary_records = self._format_boundary_data(boundary_data)
            df_boundary = pd.DataFrame(boundary_records)
            boundary_writer = create_excel_data_writer(
                output_path,
                "BlockBoundaryCodes"
            )
            boundary_writer.write_data(df_boundary)

            df_vendor = pd.DataFrame(vendor_data)
            vendor_writer = create_excel_data_writer(
                output_path,
                "VendorCodes"
            )
            vendor_writer.write_data(df_vendor)

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

    def add_supervisor_columns_to_dataframe(self, df:pd.DataFrame):
        columns_to_add = {
            "Role (Mandatory)": "",
            "Name (Mandatory)": None,
            "Phone Number (Mandatory)": None,
            "Email Address (Mandatory)": None
        }
        df_modified = df.copy()

        for col_name, default_value in columns_to_add.items():
            if col_name not in df_modified.columns:
                df_modified[col_name] = default_value
        return df_modified



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
                    "Boundary Code": facility.get("boundaryCode", ""),
                    "Health Centre Name": facility.get("facility_name", ""),
                    "HC ID": facility.get("facility_id", ""),
                    "Type of HC": facility.get("facility_type", ""),
                    "HFR ID": details.get("hfr_id", ""),
                    "NIN ID": details.get("nin_id", ""),
                    "Selection?": ""  # dropdown will be added
                }

                records.append(record)

            df_facility = pd.DataFrame(records, columns=column_names)

            df_facility.to_excel(output_path, sheet_name="Facility Selection Template", index=False)

            dropdowns_map = {'Selection?': ['Yes', 'No']}

            add_dropdowns_to_excel(
                file_path=output_path,
                sheet_name="Facility Selection Template",
                dropdowns=dropdowns_map
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

    def get_all_vendor_codes(self, request_info : RequestInfo) -> List[Dict]:
        """
        Fetch all vendor codes from vendor service using the specified API format
        Returns list of vendor records with code and name
        """
        url = f"{vendor_service_url}/vendor/organisation/v1/_search"

        request_info_dict = request_info.model_dump(by_alias=True, exclude_none=True)

        payload = {
            "RequestInfo": request_info_dict,
            "SearchCriteria": {
                "tenantId": "in",
                "createdFrom": 0
            },
            "Pagination": {
                "limit": 10000,
                "offset": 0
            }
        }

        headers = {
            "Content-Type": "application/json",
        }

        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            return self._parse_vendor_response(response.json())
        except requests.exceptions.RequestException as e:
            logger.error(f"Error fetching vendor codes: {str(e)}")
            if hasattr(e, 'response') and e.response:
                logger.error(f"Vendor service response: {e.response.text}")
            return []
        except Exception as e:
            logger.error(f"Unexpected error fetching vendor codes: {str(e)}")
            return []

    def _parse_vendor_response(self, response_json: Dict) -> List[Dict]:
        """Parse vendor API response into list of vendor records"""
        vendors = []
        if response_json and 'organisations' in response_json:
            for vendor in response_json['organisations']:
                vendors.append({
                    "Vendor Code": vendor.get('code', ''),
                    "Vendor Name": vendor.get('name', ''),
                    "Vendor Application Number": vendor.get('applicationNumber', ''),
                })
        return vendors