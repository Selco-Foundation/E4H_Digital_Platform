import json
from typing import Dict, Any, List, Optional

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import ResponseInfo

logger = AppLogger().get_logger()

# Workflow actions that (re)submit the installation report - matches ActivityService's own
# literals on the Java side (see BomPdfService.resolveProjectDate).
SUBMIT_REPORT_ACTIONS = {"SUBMIT_REPORT_A", "SUBMIT_REPORT_B"}


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


    def get_installation_report_submission_dates(
        self,
        request_info: RequestInfo,
        facility_ids: List[str],
        fieldplan_id: Optional[str] = None,
    ) -> Dict[str, int]:
        """
        facilityId -> epoch-millis of the latest SUBMIT_REPORT_A/SUBMIT_REPORT_B workflow action
        - the Installation Report Submission Date, used as the AMC Start Date reference. One bulk
        call for the whole batch (not one per facility), mirroring BomPdfService.resolveProjectDate's
        workflow-history scan on the Java side, minus its freeze-after-approval branch: callers here
        always want the true latest submission, not a value frozen once approved.
        """
        if not facility_ids:
            return {}

        tenant_id = "in"
        limit = 1000
        offset = 0
        all_entries: List[Dict[str, Any]] = []

        url = f"{self.fieldPlan_activity_service_url}/activity/v1/activities/_search"
        headers = {
            "Content-Type": "application/json"
        }

        activity_facility_criteria: Dict[str, Any] = {
            "tenantId": tenant_id,
            "facilityIds": facility_ids,
        }
        if fieldplan_id:
            activity_facility_criteria["fieldPlanIds"] = [fieldplan_id]

        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "ActivityFacility": activity_facility_criteria,
        }
        params = {
            "tenantId": tenant_id,
            "limit": limit,
            "offset": offset,
            "includeDeleted": "false"
        }

        try:
            response = requests.post(url, headers=headers, json=payload, params=params)
            response.raise_for_status()
            data = response.json()
            total_count = data.get("totalCount", 0)
            all_entries.extend(data.get("facility", []))

            while len(all_entries) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.post(url, headers=headers, json=payload, params=params)
                response.raise_for_status()
                data = response.json()
                all_entries.extend(data.get("facility", []))
        except requests.exceptions.RequestException as req_err:
            logger.error(
                f"Error searching activity facilities for installation report submission dates: {req_err}",
                exc_info=True,
            )
            raise req_err

        submission_dates: Dict[str, int] = {}
        for entry in all_entries:
            activity_facility = entry.get("activityFacility") or {}
            facility_id = activity_facility.get("facilityId")
            if not facility_id:
                continue

            latest_submission = None
            for process_instance in entry.get("workflow") or []:
                action = str(process_instance.get("action") or "").upper()
                if action not in SUBMIT_REPORT_ACTIONS:
                    continue
                audit_details = process_instance.get("auditDetails") or {}
                created_time = audit_details.get("createdTime")
                if created_time and (latest_submission is None or created_time > latest_submission):
                    latest_submission = created_time

            if latest_submission is not None:
                existing = submission_dates.get(facility_id)
                if existing is None or latest_submission > existing:
                    submission_dates[facility_id] = latest_submission

        logger.info(f"Resolved installation report submission dates for {len(submission_dates)}/{len(facility_ids)} facility(ies)")
        return submission_dates

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
            logger.error(f"HTTP error searching installation plan activity assignment: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching installation plan activity assignment: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching installation plan activity assignment: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching installation plan activity assignment: {req_err}", exc_info=True)
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