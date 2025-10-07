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
            'FieldPlanFacility': {
                'facilityId': facility_id,
                'fieldPlanId': fieldPlan_id,
                'isdeleted': False,
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

    def search_fieldplan_facility(self, request_info: RequestInfo, fieldplan_id: str) -> Dict[str, Any]:
        tenant_id = "in"
        limit = 1000
        offset = 0
        all_facilities = []

        url = f"{self.fieldPlan_service_url}/field-planner/v1/field-plans/facility/_search"
        headers = {
            "Content-Type": "application/json"
        }

        try:
            # First request to get total count
            payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "FieldPlanFacility": {
                    "fieldPlanId": [fieldplan_id]
                }
            }
            params = {
                "tenantId": tenant_id,
                "limit": limit,
                "offset": offset,
                "includeDeleted": "false"
            }
            response = requests.post(url, headers=headers, json=payload, params=params)
            response.raise_for_status()

            data = response.json()
            total_count = data.get("TotalCount", 0)
            all_facilities.extend(data.get("FieldPlanFacilities", []))

            # If more pages are present, fetch them
            while len(all_facilities) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.post(url, headers=headers, json=payload, params=params)
                response.raise_for_status()
                data = response.json()
                all_facilities.extend(data.get("FieldPlanFacilities", []))

            return {
                "TotalCount": total_count,
                "FieldPlanFacilities": all_facilities
            }

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