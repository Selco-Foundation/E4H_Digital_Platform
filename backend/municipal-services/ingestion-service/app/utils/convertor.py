import json
from typing import Dict, Any, Optional, List

from pandas import Series
from pydantic import ValidationError

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


def create_facility_with_supervisor_update_payload(request_info: RequestInfo, row: Series):
    return {
        'country': row.get('Country', ''),
        'state': row.get('State', ''),
        'district': row.get('District', ''),
        'block': row.get('Block', ''),
        'boundary_code': row.get('Boundary Code (Mandatory)', ''),
        'health_centre_name': row.get('Health Centre Name (Mandatory)', ''),
        'type_of_hc': row.get('Type of HC (Mandatory)', ''),
        'hfr_id': row.get('HFR ID', ''),
        'nin_id': row.get('NIN ID', ''),
        'hc_poc_name': row.get('HC PoC Name (Mandatory)', ''),
        'hc_poc_designation': row.get('HC PoC Designation (Optional)', ''),
        'hc_poc_contact': row.get('HC PoC Contact number (Mandatory)', ''),
        'latitude': row.get('Latitude', ''),
        'longitude': row.get('Longitude', ''),
        'address': row.get('Address', ''),
        'supervisor': {
            'role': row.get('Role (Mandatory)', ''),
            'name': row.get('Name (Mandatory)', ''),
            'phone': row.get('Phone Number (Mandatory)', ''),
            'email': row.get('Email Address (Mandatory)', '')
        }
    }
