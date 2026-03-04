import json
from typing import Dict, Any, List

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import ResponseInfo

logger = AppLogger().get_logger()


class FieldPlanActivityServiceClient:
    def __init__(self, fieldPlan_activity_service_url: str):
        self.fieldPlan_activity_service_url = fieldPlan_activity_service_url

    def create_facility_activity(self, request_info: RequestInfo, fieldPlan, roleToIds, facility_id: str):
        url = f"{self.fieldPlan_activity_service_url}/activity/v1/activities/_assign-staff"
        headers = {
            "Content-Type": "application/json"
        }

        activityId = fieldPlan.get("activities", None)[0].get("code")
        payload = {
            'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
            'ActivitiesFacilities': [{
                'facilityId': facility_id,
                'fieldPlanId': fieldPlan.get("id", None),
                'activityId': activityId,
                'scheduledAt': fieldPlan.get("startDate", None),
                'activatedAt': fieldPlan.get("startDate", None),
                'reviewerUser': roleToIds.get("INSTALLATION_REVIEWER"),
                'spocUser': roleToIds.get("INSTALLATION_SPOC"),
                'tenantId': 'in'
            }]
        }
        logger.trace(f"Creating facility activity: facility_id={facility_id}, fieldplan_id={fieldPlan.get('id', 'unknown')}")
        try:
            response = requests.post(url, headers=headers, json=payload)
            logger.info(f"Facility activity created successfully: facility_id={facility_id}")
            logger.debug(f"Create response: {json.loads(response.text)}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating facility activity: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating facility activity: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating facility activity: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating facility activity: {req_err}", exc_info=True)
            raise req_err

    def search_facility_activity(self, request_info: RequestInfo, fieldplan_id: str, facility_id:str) -> Dict[str, Any]:
        tenant_id = "in"
        limit = 1000
        offset = 0
        all_facilities = []

        url = f"{self.fieldPlan_activity_service_url}/activity/v1/activities/_search"
        headers = {
            "Content-Type": "application/json"
        }

        try:
            # First request to get total count
            payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "ActivityFacility": {
                    "tenantId": tenant_id,
                    "fieldPlanIds": [fieldplan_id],
                    "facilityIds": [facility_id]
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
            total_count = data.get("totalCount", 0)
            all_facilities.extend(data.get("facility", []))

            # If more pages are present, fetch them
            while len(all_facilities) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.post(url, headers=headers, json=payload, params=params)
                response.raise_for_status()
                data = response.json()
                all_facilities.extend(data.get("facility", []))

            return {
                "TotalCount": total_count,
                "FacilityActivities": all_facilities
            }

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error searching facility activity: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching facility activity: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching facility activity: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching facility activity: {req_err}", exc_info=True)
            raise req_err


    def search_fieldplan_activity_assignment(self, request_info: RequestInfo, fieldplan_id: str) -> Dict[str, Any]:
        tenant_id = "in"
        limit = 1000
        offset = 0
        all_facilities = []

        url = f"{self.fieldPlan_activity_service_url}/activity/v1/activities/assignment/_search"
        headers = {
            "Content-Type": "application/json"
        }

        try:
            # First request to get total count
            payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "ActivityAssignment": {
                    "tenantId": tenant_id,
                    "fieldPlanIds": [fieldplan_id]
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
            total_count = data.get("totalCount", 0)
            all_facilities.extend(data.get("ActivityAssignment", []))

            # If more pages are present, fetch them
            while len(all_facilities) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.post(url, headers=headers, json=payload, params=params)
                response.raise_for_status()
                data = response.json()
                all_facilities.extend(data.get("ActivityAssignment", []))

            return {
                "TotalCount": total_count,
                "ActivitiesAssignments": all_facilities
            }

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error searching field plan activity assignment: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching field plan activity assignment: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching field plan activity assignment: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching field plan activity assignment: {req_err}", exc_info=True)
            raise req_err

    def delete_facility_activity(self, request_info: RequestInfo, facility_activity_id: List[str]):
        """
        Delete a facility activity by setting isDeleted to True
        """
        logger.trace(f"Deleting facility activity: facility_activity_id={facility_activity_id}")
        try:
            if not facility_activity_id:
                logger.warning("No ID found for Facility Activity record")
                return None

            logger.debug(f"Found Facility Activity record with ID: {facility_activity_id}")

            # Now update the record to set isDeleted = True
            update_url = f"{self.fieldPlan_activity_service_url}/activity/v1/activities/_delete"
            update_headers = {
                "Content-Type": "application/json"
            }

            # Build FieldPlanFacility payload - only include rowVersion if present
            facility_activity_payload_list = [
                {'id': fa_id, 'isDeleted': True, 'tenantId': 'in'}
                for fa_id in facility_activity_id
            ]

            update_payload = {
                'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
                'ActivitiesFacilities': facility_activity_payload_list
            }

            update_response = requests.post(update_url, headers=update_headers, json=update_payload)
            update_response.raise_for_status()
            logger.info(f"Facility activity deleted successfully: facility_activity_id={facility_activity_id}")
            logger.debug(f"Delete response: {json.loads(update_response.text)}")
            return update_response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error deleting facility activity: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error deleting facility activity: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error deleting facility activity: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error deleting facility activity: {req_err}", exc_info=True)
            raise req_err