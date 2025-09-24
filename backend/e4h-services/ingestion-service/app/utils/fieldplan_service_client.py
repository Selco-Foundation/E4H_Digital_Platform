import json
from typing import Dict, Any

import requests

from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import ResponseInfo


class FieldPlanServiceClient:
    def __init__(self, fieldPlan_service_url: str):
        self.fieldPlan_service_url = fieldPlan_service_url

    def create_fieldPlan_facility(self, request_info: RequestInfo, fieldPlan_id: str, facility_id: str):
        url = f"{self.fieldPlan_service_url}/field-planner/v1/field-plans/facility/_create"
        headers = {
            "Content-Type": "application/json"
        }

        payload = {
            'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
            'fieldPlanFacility': {
                'facilityId': facility_id,
                'fieldPlanId': fieldPlan_id,
                'isDeleted': False,
                'tenantId': 'in'
            }
        }
        try:
            response = requests.post(url, headers=headers, json=payload)
            print(f"FieldPlan Facility called successfully: {json.loads(response.text)}")
            return response

        except requests.exceptions.HTTPError as http_err:
            print(f"HTTP error occurred: {http_err}")
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            print(f"Connection error occurred: {conn_err}")
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            print(f"Timeout error occurred: {timeout_err}")
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            print(f"An error occurred: {req_err}")
            raise req_err