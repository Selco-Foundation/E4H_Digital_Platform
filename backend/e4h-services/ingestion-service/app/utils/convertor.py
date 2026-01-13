import datetime
import json
import time
from typing import Dict, Any, Optional, List, Union
from datetime import datetime

import pandas as pd
from pandas import Series
from psycopg.types import none
from pydantic import ValidationError
from sqlalchemy import false, true

from app.core.logging import AppLogger
from app.schemas.boundary import Boundary
from app.schemas.request_info import RequestInfo
from app.schemas.vendor import Vendor
from app.schemas.vendor_ingestion_shema_response import (
    MDMS, IngestionSchemaResponse, MDMSAuditDetails, MDMSColumn, MDMSData,
    MDMSDataSource, ResponseInfo)

logger = AppLogger().get_logger()


def format_facility_data_for_template(
    facility_data: List[Dict[str, Any]],
    facility_schema: List[Dict[str, Any]],
    headers: List[str],
    type: str = None,
) -> List[Dict[str, Any]]:
    """
    Converts raw facility data into rows, aligned with `headers`
    (already computed from facility_schema in generate_template_file).
    """

    def get_nested_value(data: Dict[str, Any], path: str):
        cur = data
        for part in path.split("."):
            if isinstance(cur, dict) and part in cur:
                cur = cur[part]
            else:
                return ""
        return "" if cur is None else cur

    compiled_cols = []
    for col, header in zip(facility_schema, headers):
        mdms_values = col.get("mdms_values") or []
        code_to_name = {mv.get("code"): mv.get("name") for mv in mdms_values if mv.get("code")}
        compiled_cols.append({
            "header": header,
            "path": col.get("code", ""),
            "type": (col.get("type") or "").strip().lower(),
            "code_to_name": code_to_name,
        })

    formatted_rows: List[Dict[str, Any]] = []
    if type and type == "project":
        for facility in facility_data:
            row = {}
            for c in compiled_cols:
                val = get_nested_value(facility, c["path"])
                if c["code_to_name"] and isinstance(val, str):
                    val = c["code_to_name"].get(val, val)

                if c["type"] in ("enum-yes-no", "boolean"):
                    if isinstance(val, bool):
                        val = "Yes" if val else "No"
                    elif isinstance(val, str):
                        val = "Yes" if val.strip().lower() in ("true", "yes", "1") else "No"
                    else:
                        val = ""
                row[c["header"]] = val

            # Add "Include in Project" column value (find the actual column name)
            include_column_name = None
            for header in headers:
                if "Include in Project" in header:
                    include_column_name = header
                    break

            if include_column_name:
                include_value = facility.get("include_in_project", "No")
                row[include_column_name] = include_value
                # Debug logging
                facility_id = facility.get("facility_id", "unknown")
                logger.debug(
                    f"Facility {facility_id} - include_in_project field: {facility.get('include_in_project', 'NOT_SET')} -> setting to: {include_value} in column: {include_column_name}")

            formatted_rows.append(row)

    elif type == "fieldplan":
        for facility in facility_data:
            row = {}
            for c in compiled_cols:
                val = get_nested_value(facility, c["path"])
                if c["code_to_name"] and isinstance(val, str):
                    val = c["code_to_name"].get(val, val)

                if c["type"] in ("enum-yes-no", "boolean"):
                    if isinstance(val, bool):
                        val = "Yes" if val else "No"
                    elif isinstance(val, str):
                        val = "Yes" if val.strip().lower() in ("true", "yes", "1") else "No"
                    else:
                        val = ""
                row[c["header"]] = val

            # Add "Include in Project" column value (find the actual column name)
            include_column_name = None
            for header in headers:
                if "Included in Field Plan" in header:
                    include_column_name = header
                    break

            if include_column_name:
                include_value = facility.get("include_in_fieldplan", "No")
                row[include_column_name] = include_value
                # Debug logging
                facility_id = facility.get("facility_id", "unknown")
                logger.debug(
                    f"Facility {facility_id} - include_in_fieldplan field: {facility.get('include_in_fieldplan', 'NOT_SET')} -> setting to: {include_value} in column: {include_column_name}")

            formatted_rows.append(row)


    return formatted_rows


def request_info_from_json(request_info_input: Union[str, Dict[str, Any]]) -> RequestInfo:
    """
    Accepts either a JSON string or a dictionary and constructs a RequestInfo object using pydantic.
    """
    try:
        if isinstance(request_info_input, str):
            data: Dict[str, Any] = json.loads(request_info_input)
        elif isinstance(request_info_input, dict):
            data = request_info_input
        else:
            raise TypeError(f"Invalid type for request_info: {type(request_info_input)}")

        return RequestInfo(**data)

    except json.JSONDecodeError as e:
        logger.error(f"Invalid JSON string in request_info_from_json: {e}", exc_info=True)
        raise
    except Exception as e:
        logger.error(f"Pydantic model creation failed in request_info_from_json: {e}", exc_info=True)
        raise


def convert_json_to_object(json_str: str) -> Optional[IngestionSchemaResponse]:
    """
    Converts a JSON string to a IngestionSchemaResponse object,
    handling nested objects.

    Args:
        json_str: The JSON string to convert.

    Returns:
        A IngestionSchemaResponse object if the conversion is successful,
        None otherwise.
    """
    try:
        data: Dict[str, Any] = json.loads(json_str)

        # Extract ResponseInfo
        response_info_data = None
        if 'ResponseInfo' in data and isinstance(data['ResponseInfo'], dict):
            try:
                response_info_data = ResponseInfo(**data['ResponseInfo'])
            except ValidationError as e:
                logger.warning(f"Validation error for ResponseInfo: {e}")
                # Provide default or None

        # Process mdms list
        mdms_objects = []
        if 'mdms' in data and isinstance(data['mdms'], list):
            for item in data['mdms']:
                if not isinstance(item, dict):
                    continue

                try:
                    # Process data field if it exists
                    if 'data' in item and isinstance(item['data'], dict):
                        data_dict = item['data']

                        # Process columns if they exist
                        columns_list = []
                        if 'columns' in data_dict and isinstance(data_dict['columns'], list):
                            for col in data_dict['columns']:
                                if not isinstance(col, dict):
                                    continue

                                # Process mdmsSource if it exists
                                if 'mdmsSource' in col and isinstance(col['mdmsSource'], dict):
                                    try:
                                        col['mdmsSource'] = MDMSDataSource(**col['mdmsSource'])
                                    except ValidationError:
                                        col['mdmsSource'] = None

                                try:
                                    columns_list.append(MDMSColumn(**col))
                                except ValidationError:
                                    # Skip invalid column
                                    pass

                            data_dict['columns'] = columns_list if columns_list else None
                        else:
                            data_dict['columns'] = None

                        try:
                            item['data'] = MDMSData(**data_dict)
                        except ValidationError:
                            item['data'] = None

                    # Process auditDetails if it exists
                    if 'auditDetails' in item and isinstance(item['auditDetails'], dict):
                        try:
                            item['auditDetails'] = MDMSAuditDetails(**item['auditDetails'])
                        except ValidationError:
                            item['auditDetails'] = None

                    # Create MDMS object
                    mdms_obj = MDMS(**item)
                    mdms_objects.append(mdms_obj)
                except ValidationError as e:
                    logger.warning(f"Validation error for MDMS item: {e}")
                    # Skip invalid item

        # Create response object with proper field names
        try:
            return IngestionSchemaResponse(
                response_info=response_info_data,
                mdms=mdms_objects if mdms_objects else None
            )
        except ValidationError as e:
            logger.warning(f"Validation error when creating response object: {e}")
            # Try with explicit field names matching the class definition
            return IngestionSchemaResponse(**{
                "ResponseInfo": response_info_data,
                "mdms": mdms_objects if mdms_objects else None
            })

    except json.JSONDecodeError as e:
        logger.error(f"Invalid JSON string in convert_json_to_object: {e}", exc_info=True)
        return None
    except ValidationError as e:
        logger.error(f"Data validation failed in convert_json_to_object: {e}", exc_info=True)
        return None
    except Exception as e:
        logger.error(f"Unexpected error in convert_json_to_object: {e}", exc_info=True)
        return None


def convert_json_to_boundary(json_str: str) -> List[Boundary]:
    data = json.loads(json_str)
    locations = [Boundary(**item) for item in data]
    return locations


def create_vendor_request(request_info: RequestInfo, vendor: Vendor):
    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "organisations": [{
            "tenantId": "in",
            "name": vendor.vendor_name,
            "code": None,
            "orgAddress": [
                {
                    "tenantId": "in",
                    "boundaryType": "country",
                    "boundaryCode": vendor.country_boundary_code,
                    "hqAddress": vendor.hq_address
                }
            ],
            "contactDetails": [
                {
                    "contactName": vendor.vendor_name,
                    "contactMobileNumber": vendor.poc_phone
                }
            ],
            "identifiers": [
                {
                    "type": vendor.identifier_type,
                    "value": vendor.identifier_value
                }
            ],
            "functions": [
                {
                    "type": "",
                    "subType": ""
                }
            ],
            "isActive": True

        }]
    }


def get_project_creation_payload(request_info: RequestInfo, project_name: str, project_type: str,
                                 parent_id:str, start_date:str, end_date:str, subType:str):
    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "Projects": [{
            "tenantId": "in",
            "name": project_name,
            "projectType": project_type,
            "parent": parent_id,
            "startDate": start_date,
            "endDate": end_date,
            "projectSubType": subType,
            "department": "",
            "description" :"",
            "referenceID" : "1"
        }],
        "isCascadingProjectDateUpdate": False,
        "apiOperation": "CREATE"
    }

def get_installation_spoc_creation_payload(request_info: RequestInfo, name:str, mobile_number:str, email:str):
    current_date = datetime.datetime.now()
    current_timestamp = int(time.mktime(current_date.timetuple()) * 1000)
    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "Employees": [
            {
                "tenantId": "in",
                "employeeStatus": "EMPLOYED",
                "dateOfAppointment": current_timestamp,
                "employeeType": "PERMANENT",
                "user": {
                    "name": name,
                    "mobileNumber": mobile_number,
                    "emailId": email,
                    "roles": [
                        {"code": "INSTALLATION_REPORT_VIEWER", "name": "Installation report viewer"},
                        {"code": "HRMS_ADMIN", "name": "Hrms admin"}
                    ],
                    "tenantId": "in",
                },
                "code": name,
                "jurisdictions": [
                    {
                        "hierarchy": "ADMIN",
                        "roles": [
                            {"code": "INSTALLATION_REPORT_VIEWER", "name": "Installation report viewer"},
                            {"value": "HRMS_ADMIN", "label": "Hrms admin"}
                        ],
                        "boundaryType": "City",
                        "boundary": "in",
                        "furnishedRolesList": "INSTALLATION_REPORT_VIEWER, HRMS_ADMIN",
                        "tenantId": "in",
                    }
                ],
                "assignments": [
                    {
                        "fromDate": current_timestamp,
                        "toDate": "",
                        "isCurrentAssignment": True,
                        "department": "DEPT_1",
                        "designation": "DESIG_01"
                    }
                ],
                "serviceHistory": [],
                "education": [],
                "tests": [],
            }
        ],
    }

def get_user_creation_payload_staff(request_info: RequestInfo, row: Series):
    current_date = datetime.datetime.now()
    current_timestamp = int(time.mktime(current_date.timetuple()) * 1000)

    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "Employees": [
            {
                "tenantId": "in",
                "employeeStatus": "EMPLOYED",
                "dateOfAppointment": current_timestamp,
                "employeeType": "PERMANENT",
                "user": {
                    "name": row.get("Name", ""),
                    "mobileNumber": row.get("Phone Number", ""),
                    "emailId": row.get("Email Address", ""),
                    "roles": [
                        {"code": "INSTALLATION_REPORT_PART_A_EDITOR", "name": "Installation Report Part A Editor"},
                        {"code": "EMPLOYEE", "name": "employee"}
                    ],
                    "tenantId": "in",
                },
                "code": row.get("Name", ""),
                "jurisdictions": [
                    {
                        "hierarchy": "ADMIN",
                        "roles": [
                            {"code": "INSTALLATION_REPORT_PART_A_EDITOR", "name": "Installation Report Part A Editor"},
                            {"code": "EMPLOYEE", "name": "employee"}
                        ],
                        "boundaryType": "City",
                        "boundary": "in",
                        "furnishedRolesList": "INSTALLATION_REPORT_PART_A_EDITOR, EMPLOYEE",
                        "tenantId": "in",
                    }
                ],
                "assignments": [
                    {
                        "fromDate": current_timestamp,
                        "toDate": "",
                        "isCurrentAssignment": True,
                        "department": "DEPT_1",
                        "designation": "DESIG_01"
                    }
                ],
                "serviceHistory": [],
                "education": [],
                "tests": [],
            }
        ],
    }

def get_user_creation_payload_supervisors(request_info: RequestInfo, row: Series):
    current_date = datetime.datetime.now()
    current_timestamp = int(time.mktime(current_date.timetuple()) * 1000)

    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "Employees": [
            {
                "tenantId": "in",
                "employeeStatus": "EMPLOYED",
                "dateOfAppointment": current_timestamp,
                "employeeType": "PERMANENT",
                "user": {
                    "name": row.get("Name", ""),
                    "mobileNumber": row.get("Phone Number", ""),
                    "emailId": row.get("Email Address", ""),
                    "roles": [
                        {"code": "INSTALLATION_REPORT_PART_B_EDITOR", "name": "Installation Report Part B Editor"},
                        {"code": "INSTALLATION_REPORT_PART_A_REVIEWER", "name": "Installation Report Part A Reviewer"},
                        {"code": "EMPLOYEE", "name": "employee"}
                    ],
                    "tenantId": "in",
                },
                "code": row.get("Name", ""),
                "jurisdictions": [
                    {
                        "hierarchy": "ADMIN",
                        "roles": [
                            {"code": "INSTALLATION_REPORT_PART_B_EDITOR", "name": "Installation Report Part B Editor"},
                            {"code": "INSTALLATION_REPORT_PART_A_REVIEWER", "name": "Installation Report Part A Reviewer"},
                            {"code": "EMPLOYEE", "name": "employee"}
                        ],
                        "boundaryType": "City",
                        "boundary": "in",
                        "furnishedRolesList": "INSTALLATION_REPORT_PART_B_EDITOR, INSTALLATION_REPORT_PART_A_REVIEWER, EMPLOYEE",
                        "tenantId": "in",
                    }
                ],
                "assignments": [
                    {
                        "fromDate": current_timestamp,
                        "toDate": "",
                        "isCurrentAssignment": True,
                        "department": "DEPT_1",
                        "designation": "DESIG_01"
                    }
                ],
                "serviceHistory": [],
                "education": [],
                "tests": [],
            }
        ],
    }

def get_staff_creation_payload(request_info:RequestInfo, user_uuid:str, parent_id:str):
    current_date = datetime.datetime.now()
    one_year_later = current_date.replace(year=current_date.year + 1)
    current_timestamp = int(time.mktime(current_date.timetuple()) * 1000)
    one_year_later_timestamp = int(time.mktime(one_year_later.timetuple()) * 1000)

    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "ProjectStaff":{
            "userId":user_uuid,
            "projectId":parent_id,
            "startDate": current_timestamp,
            "endDate": one_year_later_timestamp,
            "channel": "MOBILE",
            "isDeleted": False,
            "tenantId": "in"
        }
    }

def get_staff_search_payload(request_info:RequestInfo, user_uuid:str):
    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "ProjectStaff":{
            "staffId": [user_uuid]
        }
    }

def safe_get(row, key, default=None):
    val = row.get(key, default)
    return default if pd.isna(val) else val


def create_facility_payload(request_info: RequestInfo, row: Series, facility_schema: List[Dict[str, Any]]):
    facility_type_name = safe_get(row, 'Type of HC (Mandatory)')
    facility_type_code = get_mdms_code_by_name(facility_schema, 'Type of HC', facility_type_name)

    solar_solution_design_type_name = safe_get(row, 'Solution Design Type (Mandatory)')
    solar_solution_design_type_code = get_mdms_code_by_name(facility_schema, 'Solution Design Type', solar_solution_design_type_name)

    return {
        'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
        'facilities': [
            {
                'tenant_id': 'in',
                'facility_name': safe_get(row, 'Health Centre Name (Mandatory)'),
                'facility_type': facility_type_code,
                'facility_category': safe_get(row, 'Category', 'HEALTH'),
                'facility_ownership': safe_get(row, 'Ownership', 'GOVERNMENT'),
                'facility_region': safe_get(row, 'Region', 'RURAL'),
                'isActive': True,
                'boundaryCode': safe_get(row, 'Boundary Code (Mandatory)'),
                'address': {
                    'tenantId': 'in',
                    'latitude': safe_get(row, 'Latitude'),
                    'longitude': safe_get(row, 'Longitude'),
                    'addressLine1': safe_get(row, 'Address'),
                    'state': safe_get(row, 'State'),
                    'district': safe_get(row, 'District'),
                    'block': safe_get(row, 'Block')
                },
                'facility_details': {
                    'vendor_code': safe_get(row, 'Vendor Code (Mandatory)'),
                    'solar_solution_design_type': solar_solution_design_type_code,
                    'pocName': safe_get(row, 'HC PoC Name (Mandatory)'),
                    'pocDesignation': safe_get(row, 'HC PoC Designation'),
                    'pocContact': safe_get(row, 'HC PoC Contact number (Mandatory)'),
                    'hfr_id': safe_get(row, 'HFR ID'),
                    'nin_id': safe_get(row, 'NIN ID')
                }
            }
        ]
    }

def convert_response_to_facility(response: Dict[str, Any], role_type: str):
    return {
        "Country": "India",
        "State": response["address"]["state"],
        "District": response["address"]["district"],
        "Block": response["address"]["block"],
        "Boundary Code (Mandatory)": response["boundaryCode"],
        "Health Centre Name (Mandatory)": response["facility_name"],
        "Type of HC (Mandatory)": response["facility_type"],
        "HFR ID": response["facility_details"]["hfr_id"],
        "NIN ID": "",
        "Facility ID": response["facility_id"],
        "HC PoC Name (Mandatory)": response["facility_details"]["pocName"],
        "HC PoC Designation": "",
        "HC PoC Contact Number (Mandatory)": response["facility_details"]["pocContact"],
        "Latitude": response["address"]["latitude"],
        "Longitude": response["address"]["longitude"],
        "Address": (response["address"]["addressNumber"] or "") + " " + (response["address"]["addressLine1"] or "") + " " \
           + (response["address"]["addressLine2"] or "") + " " + (response["address"]["landmark"] or "") + " " \
           + (response["address"]["city"] or "") + " " + (response["address"]["pincode"] or ""),
        "Role": role_type,
        "Name": "",
        "Gender": "",
        "Phone Number": "",
        "Email Address": ""
    }

def create_project_payload(request_info: RequestInfo, row: Series):
    def to_epoch(date_str: str) -> int:
        try:
            dt = datetime.datetime.strptime(date_str.strip(), "%d/%m/%Y")
            return int(dt.timestamp() * 1000)
        except ValueError:
            raise ValueError(f"Date '{date_str}' is not in the format DD/MM/YYYY")
    return {
        'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
        'Projects': [
            {
                'tenantId': 'in',
                'name': safe_get(row, 'Project Name'),
                'projectType': safe_get(row, 'Project Type'),
                'projectSubType': safe_get(row, 'Project Sub Type'),
                'department': safe_get(row, 'Project Department'),
                'description': safe_get(row, 'Project Description'),
                'referenceID': safe_get(row, 'Project Reference ID'),
                'parent': safe_get(row, 'Parent Project ID'),
                'startDate': to_epoch(safe_get(row, 'Project Start Date (DD/MM/YYYY)')),
                'endDate': to_epoch(safe_get(row, 'Project End Date (DD/MM/YYYY)')),
                'address': {
                    'boundary': safe_get(row, 'Boundary Code'),
                    'boundaryType': safe_get(row, 'Boundary Type'),
                }
            }
        ],
        'isCascadingProjectDateUpdate': False,
        'apiOperation': 'CREATE'
    }


def get_mdms_code_by_name(schema_list: List[Dict[str, Any]], field_name: str, value: str) -> str:
    """
    From schema_list, finds the entry where `name` matches `field_name` and then returns the `code`
    of the mdms_value where `name` == value.

    Raises:
        ValueError: If the field_name or value is not found in the schema.
    """
    for schema in schema_list:
        if schema.get('name') == field_name:
            for item in schema.get('mdms_values', []):
                if item.get('name') == value:
                    return item.get('code')
            raise ValueError(f"Invalid value '{value}' for field '{field_name}' in MDMS schema.")

    raise ValueError(f"Field name '{field_name}' not found in MDMS schema.")

def get_expected_roles_for_staff() -> List[str]:
    return ["INSTALLATION_REPORT_PART_A_EDITOR", "EMPLOYEE"]

def get_expected_roles_for_supervisor() -> List[str]:
    return ["INSTALLATION_REPORT_PART_B_EDITOR", "INSTALLATION_REPORT_PART_A_REVIEWER", "EMPLOYEE"]

def check_role_mismatch_for_user_type(existing_user: Dict[str, Any], user_type: str) -> Dict[str, Any]:
    if user_type.lower() == "staff" or user_type.lower() == "field_staff":
        expected_roles = get_expected_roles_for_staff()
    elif user_type.lower() == "supervisor" or user_type.lower() == "field_supervisor":
        expected_roles = get_expected_roles_for_supervisor()
    else:
        return {
            "has_mismatch": False,
            "current_roles": [],
            "expected_roles": [],
            "mismatch_details": f"Unknown user type: {user_type}"
        }

    # Extract current roles from user data
    current_roles = []
    user_data = existing_user.get("user", {})
    roles = user_data.get("roles", [])

    for role in roles:
        role_code = role.get("code", "")
        if role_code:
            current_roles.append(role_code)

    # Check for mismatches
    missing_roles = []
    unexpected_roles = []

    for expected_role in expected_roles:
        if expected_role not in current_roles:
            missing_roles.append(expected_role)

    for current_role in current_roles:
        if current_role not in expected_roles:
            unexpected_roles.append(current_role)

    has_mismatch = bool(missing_roles or unexpected_roles)

    mismatch_details = ""
    if has_mismatch:
        if missing_roles:
            mismatch_details += f"Missing roles: {', '.join(missing_roles)}. "
        if unexpected_roles:
            mismatch_details += f"Unexpected roles: {', '.join(unexpected_roles)}."

    return {
        "has_mismatch": has_mismatch,
        "current_roles": current_roles,
        "expected_roles": expected_roles,
        "mismatch_details": mismatch_details.strip()
    }

def get_incident_data_update_request_info():
    return {
        "apiId": "Rainmaker",
        "authToken": "222d0cf6-07c2-4d90-8a71-0292c200ae74",
        "userInfo": {
             "id": 4294,
            "userName": "7204449839",
            "salutation": None,
            "name": "Revathi J",
            "gender": "MALE",
            "mobileNumber": "7204449839",
            "emailId": "",
            "altContactNumber": None,
            "pan": None,
            "aadhaarNumber": None,
            "permanentAddress": None,
            "permanentCity": None,
            "permanentPinCode": None,
            "correspondenceAddress": None,
            "correspondenceCity": None,
            "correspondencePinCode": None,
            "alternatemobilenumber": None,
            "active": True,
            "locale": "en_IN",
            "type": "EMPLOYEE",
            "accountLocked": False,
            "accountLockedDate": 0,
            "fatherOrHusbandName": None,
            "relationship": None,
            "signature": None,
            "bloodGroup": None,
            "photo": None,
            "identificationMark": None,
            "createdBy": 0,
            "lastModifiedBy": 4294,
            "tenantId": "pg",
            "roles": [
                {
                    "code": "COMPLAINANT",
                    "name": "Complainant",
                    "tenantId": "pg"
                },
                {
                    "code": "EMPLOYEE",
                    "name": "Employee",
                    "tenantId": "pg"
                },
                {
                    "code": "COMPLAINT_ASSESSOR",
                    "name": "Complaint Assessor",
                    "tenantId": "pg"
                },
                {
                    "code": "COMPLAINT_FACILITATOR_2",
                    "name": "Complaint facilitator 2",
                    "tenantId": "pg"
                },
                {
                    "code": "SUPERUSER",
                    "name": "Super User",
                    "tenantId": "pg"
                }
            ],
            "uuid": "1e18f9bc-9702-4326-b66f-3732092e25d9",
            "createdDate": "07-07-2025 12:57:24",
            "lastModifiedDate": "07-07-2025 16:44:01",
            "dob": "1994-02-08",
            "pwdExpiryDate": "05-10-2025 12:57:24"
        },
        "msgId": "1751897062350|en_IN",
        "plainAccessRequest": {}
    }


def create_incident_data_update_payload(search_response: dict, update_data: dict) -> dict:
    wrappers = search_response.get("IncidentWrappers") or []
    if not wrappers:
        raise ValueError("Incident not found in search response (empty IncidentWrappers).")
    incident_wrapper = wrappers[0]
    incident = incident_wrapper.get("incident", {})
    workflow = incident_wrapper.get("workflow", {})
    filed_date = incident.get("filedDate")

    if pd.isna(filed_date) or int(filed_date) == 0:
        formatted_date = ""
    else :
        dt = datetime.fromtimestamp(int(filed_date) / 1000)
        formatted_date = dt.strftime("%d/%m/%Y")



    request_info = get_incident_data_update_request_info()

    original_type = incident.get('incidentType', '')
    original_subtype = incident.get('incidentSubType', '')

    details = {
        "CS_COMPLAINT_DETAILS_TICKET_NO": incident.get("incidentId"),
        "CS_COMPLAINT_DETAILS_APPLICATION_STATUS": f"CS_COMMON_{incident.get('applicationStatus')}",
        "CS_ADDCOMPLAINT_TICKET_TYPE": f"SERVICEDEFS.{original_type.upper()}",
        "CS_ADDCOMPLAINT_TICKET_SUB_TYPE": f"SERVICEDEFS.{original_subtype.upper()}",
        "CS_ADDCOMPLAINT_SYSTEM_FUNCTIONAL": update_data.get("systemFunctional"),
        "CS_ADDCOMPLAINT_DISTRICT": incident.get("district", ""),
        "CS_ADDCOMPLAINT_BLOCK": incident.get("block", ""),
        "CS_ADDCOMPLAINT_HEALTH_CARE_CENTRE": incident.get("tenantId", ""),
        "CS_COMPLAINT_COMMENTS": incident.get("comments", ""),
        "CS_ADDCOMPLAINT_HEALTH_CARE_SUB_TYPE": incident.get("phcSubType", ""),
        "CS_COMPLAINT_FILED_DATE": formatted_date
    }
    systemFunctional = update_data.get("systemFunctional")
    incident["systemFunctional"] = systemFunctional

    audit = {
        "details": incident.get("auditDetails", {}),
        "incidentType": original_subtype
    }

    return {
        "details": details,
        "workflow": workflow,
        "incident": incident,
        "audit": audit,
        "RequestInfo": request_info
    }