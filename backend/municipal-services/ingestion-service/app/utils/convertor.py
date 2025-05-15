import datetime
import json
import time
from typing import Dict, Any, Optional, List

import pandas as pd
from pandas import Series
from pydantic import ValidationError
from sqlalchemy import false

from app.schemas.boundary import Boundary
from app.schemas.request_info import RequestInfo
from app.schemas.vendor import Vendor
from app.schemas.vendor_ingestion_shema_response import IngestionSchemaResponse, MDMS, ResponseInfo, \
    MDMSDataSource, MDMSColumn, MDMSData, MDMSAuditDetails


def request_info_from_json(request_info_str: str) -> RequestInfo:
    """
    Parses a JSON string and constructs a RequestInfo object using pydantic.
    Handles nested objects (PlainAccessRequest, User) automatically.

    Args:
        request_info_str: The JSON string to parse.

    Returns:
        A RequestInfo object.

    Raises:
        json.JSONDecodeError: If the input string is not valid JSON.
        pydantic.ValidationError: If the parsed JSON does not conform to the
                                  RequestInfo pydantic model.
    """
    try:
        data: Dict[str, Any] = json.loads(request_info_str)
        return RequestInfo(**data)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON string: {e}")
        raise
    except Exception as e:
        print(f"Error: Pydantic model creation failed: {e}")
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
                print(f"Validation Error for ResponseInfo: {e}")
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
                    print(f"Validation Error for MDMS item: {e}")
                    # Skip invalid item

        # Create response object with proper field names
        try:
            return IngestionSchemaResponse(
                response_info=response_info_data,
                mdms=mdms_objects if mdms_objects else None
            )
        except ValidationError as e:
            print(f"Validation Error when creating response object: {e}")
            # Try with explicit field names matching the class definition
            return IngestionSchemaResponse(**{
                "ResponseInfo": response_info_data,
                "mdms": mdms_objects if mdms_objects else None
            })

    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON string. Details: {e}")
        return None
    except ValidationError as e:
        print(f"Error: Data validation failed. Details: {e}")
        return None
    except Exception as e:
        print(f"An unexpected error occurred: {e}")
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
            "code": vendor.vendor_code,
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


def get_project_creation_payload(request_info: RequestInfo, project_name: str, project_type: str):
    current_date = datetime.datetime.now()
    one_year_later = current_date.replace(year=current_date.year + 1)
    current_timestamp = int(time.mktime(current_date.timetuple()) * 1000)
    one_year_later_timestamp = int(time.mktime(one_year_later.timetuple()) * 1000)

    return {
        "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
        "Projects": [{
            "tenantId": "in",
            "name": project_name,
            "projectType": project_type,
            "startDate": current_timestamp,
            "endDate": one_year_later_timestamp
        }],
        "isCascadingProjectDateUpdate": False,
        "apiOperation": "CREATE"
    }

def get_user_creation_payload(request_info:RequestInfo, row:Series):
    return {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "Employees": [
                {
                    "tenantId": "in",
                    "user": {
                        "name": row.get("Name",""),
                        "mobileNumber": row.get("Phone Number",""),
                        "emailId":row.get("Email Address",""),
                        "roles": [
                            {"code": "INSTALLATION_SUPERVISOR", "name": "Installation supervisor"},
                            {"code": "INSTALLATION_REPORT_VIEWER", "name": "Installation report viewer"},
                            {"code": "HRMS_ADMIN", "name": "Hrms admin"}
                        ],
                        "tenantId": "in",
                    },
                    "code": row.get("Name",""),
                    "jurisdictions": [
                        {
                            "hierarchy": "ADMIN",
                            "roles": [
                                {"value": "INSTALLATION_SUPERVISOR", "label": "Installation supervisor"},
                                {"value": "INSTALLATION_REPORT_VIEWER", "label": "Installation report viewer"},
                                {"value": "HRMS_ADMIN", "label":"Hrms admin"}
                            ],
                            "boundaryType": "City",
                            "boundary": "in",
                            "furnishedRolesList": "INSTALLATION_SUPERVISOR, INSTALLATION_REPORT_VIEWER, HRMS_ADMIN",
                            "tenantId": "in",
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
            "isDeleted": false,
            "tenantId": "in"
        }
    }

def safe_get(row, key, default=None):
    val = row.get(key, default)
    return default if pd.isna(val) else val


def create_facility_payload(request_info: RequestInfo, row: Series):
    return {
        'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
        'facility': {
            'tenant_id': 'in',
            'facility_name': safe_get(row, 'Health Centre Name (Mandatory)'),
            'facility_type': safe_get(row, 'Type of HC (Mandatory)'),
            'facility_category': safe_get(row, 'Category', 'HEALTH'),
            'facility_ownership': safe_get(row, 'Ownership', 'GOVERNMENT'),
            'facility_region': safe_get(row, 'Region', 'RURAL'),
            'isActive': True,
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
                'boundaryCode': safe_get(row, 'Boundary Code (Mandatory)'),
                'vendorCode': safe_get(row, 'Vendor Code (Mandatory)'),
                'solutionDesignType': safe_get(row, 'Solution Design Type (Mandatory)'),
                'pocName': safe_get(row, 'HC PoC Name (Mandatory)'),
                'pocDesignation': safe_get(row, 'HC PoC Designation'),
                'pocContact': safe_get(row, 'HC PoC Contact number (Mandatory)'),
                'hfrId': safe_get(row, 'HFR ID'),
                'ninId': safe_get(row, 'NIN ID')
            }
        }
    }



def convert_response_to_facility(response: Dict[str, Any]):
    return {
        "Country": "India",
        "State": response["address"]["state"],
        "District": response["address"]["district"],
        "Block": response["address"]["block"],
        "Boundary Code (Mandatory)": response["facility_details"]["boundaryCode"],
        "Health Centre Name (Mandatory)": response["facility_name"],
        "Type of HC (Mandatory)": response["facility_type"],
        "HFR ID": response["facility_details"]["hfrId"],
        "NIN ID": "",
        "Facility ID": response["facility_id"],
        "HC PoC Name (Mandatory)": response["facility_details"]["pocName"],
        "HC PoC Designation": "",
        "HC PoC Contact Number (Mandatory)": response["facility_details"]["pocContact"],
        "Latitude": response["address"]["latitude"],
        "Longitude": response["address"]["longitude"],
        "Address": response["address"]["addressNumber"] + " " + response["address"]["addressLine1"] + " "
                   + response["address"]["addressLine2"] + " " + response["address"]["landmark"] + " "
                   + response["address"]["city"] + " " + response["address"]["pincode"],
        "Role": "Supervisor",
        "Name": "",
        "Gender": "",
        "Phone Number": "",
        "Email Address": ""
    }
