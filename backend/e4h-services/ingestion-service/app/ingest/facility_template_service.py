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
    lock_prefilled_rows_in_excel, add_non_blank_validations_to_file, autofit_columns, \
    add_facility_category_conditional_validations
from app.utils.file_utils import create_empty_excel_file, create_excel_data_writer, remove_default_empty_sheet
from app.utils.localization_service_client import LocalizationServiceClient

logger = AppLogger().get_logger()
from dotenv import load_dotenv
load_dotenv()
mdms_url = os.getenv("MDMS_URL")
boundary_service_url = os.getenv("BOUNDARY_SERVICE_URL")
vendor_service_url = os.getenv("VENDOR_SERVICE_URL")
localization_service_url = os.getenv("LOCALIZATION_SERVICE_URL")

EDITABLE_SOLAR_COLUMN_CODES = {
    "facility_type",
    "system_type",
    "total_system_capacity",
    "solution_design_type",
    "facility_details.solar_solution_design_type",
    "custom_solar_solution_design",
    "custom_solar_system_capacity",
}

ASSESSMENT_INCLUDE_EXCLUDED_COLUMN_CODES = {
    *EDITABLE_SOLAR_COLUMN_CODES,
    "include_in_fieldplan",
    "included_in_field_plan",
}

ASSESSMENT_INCLUDE_EXCLUDED_NAME_FRAGMENTS = (
    "include in field plan",
    "included in field plan",
    "include in project",
    "system type",
    "total system capacity",
    "solution design type",
    "custom solution design",
    "custom total system capacity",
)

FACILITY_TYPE_DROPDOWN_FALLBACK = ["Sub Center", "Primary Health Centre", "Anganwadi"]
SYSTEM_TYPE_DROPDOWN_FALLBACK = ["DC Off-grid", "AC Off-grid", "AC Hybrid"]
SOLAR_CAPACITY_DROPDOWN_FALLBACK = [
    "0.25 kWp", "0.5 kWp", "1 kWp", "2 kWp", "3 kWp", "5 kWp",
    "6 kWp", "7 kWp", "8 kWp", "10 kWp", "Custom",
]
SOLUTION_DESIGN_DROPDOWN_FALLBACK = ["Custom Solution Design"]


def _skip_column_for_assessment_include(col: Dict[str, Any]) -> bool:
    """Assessment include template: project facility snapshot + Include in Assessment Plan only."""
    column_code = (col.get("code") or "").strip().lower()
    col_name = (col.get("name") or "").strip().lower()
    if column_code in ASSESSMENT_INCLUDE_EXCLUDED_COLUMN_CODES:
        return True
    if any(fragment in col_name for fragment in ASSESSMENT_INCLUDE_EXCLUDED_NAME_FRAGMENTS):
        return True
    return False


def filter_assessment_include_schema(facility_schema: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    return [col for col in facility_schema if not _skip_column_for_assessment_include(col)]


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
                               type: str = None,
                               optimize_for_performance: bool = False
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

            schema_for_template = facility_schema
            if type == "assessment_include":
                schema_for_template = filter_assessment_include_schema(facility_schema)

            for col in schema_for_template:
                column_code = col.get("code")

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

                if column_code in EDITABLE_SOLAR_COLUMN_CODES:
                    editable_columns.append(header_name)
                    if column_code == "facility_type" and header_name not in dropdowns_map:
                        dropdowns_map[header_name] = FACILITY_TYPE_DROPDOWN_FALLBACK
                    elif column_code == "system_type" and header_name not in dropdowns_map:
                        dropdowns_map[header_name] = SYSTEM_TYPE_DROPDOWN_FALLBACK
                    elif column_code == "total_system_capacity" and header_name not in dropdowns_map:
                        dropdowns_map[header_name] = SOLAR_CAPACITY_DROPDOWN_FALLBACK
                    elif column_code in {
                        "solution_design_type",
                        "facility_details.solar_solution_design_type",
                    } and header_name not in dropdowns_map:
                        dropdowns_map[header_name] = SOLUTION_DESIGN_DROPDOWN_FALLBACK

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

            if type == "assessment_include":
                include_column = "Include in Assessment Plan (Mandatory)"
                if not any("Include in Assessment Plan" in col for col in output_list):
                    output_list.append(include_column)
                    dropdowns_map[include_column] = ["Yes", "No"]
                    editable_columns.append(include_column)
                    allow_blank_map[include_column] = False
                    logger.info(f"Added assessment include column: {include_column}")

            if type != "assessment_include":
                existing_include_column = None
                for col in output_list:
                    if "Include in Project" in col:
                        existing_include_column = col
                        break

                if existing_include_column:
                    include_column = existing_include_column
                    dropdowns_map[include_column] = ["Yes", "No"]
                    editable_columns.append(include_column)
                    logger.info(f"Using existing column: {include_column}")

            logger.info(f"Final columns: {output_list}")

            # Add Existing Facilities Sheet (Optional)
            formatted_facilities = []
            if facility_data:
                formatted_facilities = format_facility_data_for_template(
                    facility_data, schema_for_template, output_list, type
                )

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

            # Add Validations (Regex + Unique) as comments/hints.
            # These are helpful but expensive on large sheets, so allow skipping
            # them when optimize_for_performance is enabled.
            if not optimize_for_performance:
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

            # Non-blank validations are helpful but expensive; keep them only
            # in fully featured mode. Autofit is needed for usability, so it is
            # always applied using a lightweight implementation.
            if not optimize_for_performance:
                add_non_blank_validations_to_file(
                    file_path=output_path,
                    sheet_name="FacilityMapping",
                    facility_schema=facility_schema,
                    allow_blank_map=allow_blank_map
                )
#                 add_facility_category_conditional_validations(
#                     file_path=output_path,
#                     sheet_name="FacilityMapping",
#                 )

            autofit_columns(
                file_path=output_path,
                sheet_name="FacilityMapping",
                auto_fit=True,
                max_rows_to_scan=10,
                enable_wrap_text=False,
            )
            autofit_columns(
                file_path=output_path,
                sheet_name="BoundaryCodes",
                auto_fit=True,
                max_rows_to_scan=10,
                enable_wrap_text=False,
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
            allow_blank_map = {}
            for col in facility_schema:
                col_name = col.get("name")
                if col_name and str(col_name).strip().lower() == "include in project":
                    #remove "Include in Project" from facility ingestion template
                    continue
                mandatory_indicator = "(Mandatory)" if col.get("required") else ""
                header_name = f"{col.get('name')} {mandatory_indicator}".strip()
                output_list.append(header_name)

                allow_blank_map[header_name] = not col.get("required", False)

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
                allow_blank_map=allow_blank_map
            )

            # add_health_category_hfr_nin_validations(
            #     file_path=output_path,
            #     sheet_name="FacilityIngestionTemplate",
            # )

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

            remove_default_empty_sheet(output_path)
            logger.info(f"Successfully created template file at {output_path}")
        except Exception as e:
            logger.error(f"Error generating template file: {e}")
            raise

    def _format_boundary_data(self, boundary_data: List[Boundary]) -> List[Dict[str, str]]:
        """Format boundary data into required structure, with localized display names."""
        boundary_records = []

        all_raw_codes = set()
        for boundary in boundary_data:
            for field in ("country", "state", "district", "block"):
                val = boundary.get(field, "")
                if val:
                    all_raw_codes.add(val)

        loc_codes = [f"Boundary_{code}" for code in all_raw_codes]

        localization_map: Dict[str, str] = {}
        if localization_service_url and loc_codes:
            try:
                loc_client = LocalizationServiceClient(localization_service_url)
                loc_response = loc_client.search_messages(
                    tenant_id="in",
                    locale="en_IN",
                    module="rainmaker-in",
                    codes=loc_codes,
                )
                for m in loc_response.get("messages", []):
                    code = (m.get("code") or "").strip()
                    message = m.get("message", "")
                    if code and message:
                        localization_map[code] = message
            except Exception as e:
                logger.error(f"Error fetching boundary localizations: {e}", exc_info=True)

        def localized(raw_code: str) -> str:
            if not raw_code:
                return ""
            loc_key = f"Boundary_{raw_code}"
            return localization_map.get(loc_key, loc_key)

        for boundary in boundary_data:
            boundary_records.append({
                "Country": localized(boundary.get("country", "")),
                "State": localized(boundary.get("state", "")),
                "District": localized(boundary.get("district", "")),
                "Block": localized(boundary.get("block", "")),
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
                    "Vendor Id": vendor.get('id', ''),
                    "Vendor Code": vendor.get('code', ''),
                    "Vendor Name": vendor.get('name', ''),
                    "Vendor Application Number": vendor.get('applicationNumber', ''),
                })
        return vendors