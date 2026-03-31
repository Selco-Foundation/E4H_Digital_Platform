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

    def search_fieldPlan(self, request_info: RequestInfo, fieldplan_id: str) -> Dict[str, Any]:
        tenant_id = "in"
        limit = 1000
        offset = 0
        all_facilities = []

        url = f"{self.fieldPlan_service_url}/field-planner/v1/field-plans/_search"
        headers = {
            "Content-Type": "application/json"
        }

        try:
            # First request to get total count
            payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "FieldPlans": {
                    "ids": [fieldplan_id],
                    "tenantId": tenant_id
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
            all_facilities.extend(data.get("FieldPlans", []))

            # If more pages are present, fetch them
            while len(all_facilities) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.post(url, headers=headers, json=payload, params=params)
                response.raise_for_status()
                data = response.json()
                all_facilities.extend(data.get("FieldPlans", []))

            return {
                "TotalCount": total_count,
                "FieldPlans": all_facilities
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


    def unlink_fieldplan_facility(self, request_info: RequestInfo, fieldplan_id: str, facility_id: str,
                                  fieldplan_facility_data: Dict[str, Any] = None):
        """
        Unlink a facility from a field plan by setting isDeleted to True
        """
        try:
            # Use provided project_facility_data if available, otherwise search for it
            if fieldplan_facility_data:
                target_facility = fieldplan_facility_data
                print(f"Using provided FieldPlanFacility data for facility {facility_id}")
            else:
                # Fallback: Use existing search method to find the FieldPlanFacility record
                print(f"Searching for FieldPlanFacility record for facility {facility_id}")
                search_response = self.search_fieldplan_facility(request_info, fieldplan_id)
                fieldplan_facilities = search_response.get("FieldPlanFacilities", [])

                # Find the specific facility in the results
                target_facility = None
                for pf in fieldplan_facilities:
                    if pf.get("facilityId") == facility_id:
                        target_facility = pf
                        break

                if not target_facility:
                    print(f"No FieldPlanFacility record found for facility {facility_id} and field plan {fieldplan_id}")
                    return None

            fieldplan_facility_id = target_facility.get("id")

            if not fieldplan_facility_id:
                print("No ID found for FieldPlanFacility record")
                return None

            print(f"Found FieldPlanFacility record with ID: {fieldplan_facility_id}")

            # Now update the record to set isDeleted = True
            update_url = f"{self.fieldPlan_service_url}/field-planner/v1/field-plans/facility/_unassign"
            update_headers = {
                "Content-Type": "application/json"
            }

            # Build FieldPlanFacility payload - only include rowVersion if present
            fieldplan_facility_payload = {
                'id': fieldplan_facility_id,
                'facilityId': facility_id,
                'fieldPlanId': fieldplan_id,
                'isdeleted': True,
                'tenantId': 'in'
            }

            update_payload = {
                'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
                'FieldPlanFacility': fieldplan_facility_payload
            }

            update_response = requests.post(update_url, headers=update_headers, json=update_payload)
            update_response.raise_for_status()
            print(f"Field Plan Facility unlinked successfully: {json.loads(update_response.text)}")
            return update_response

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