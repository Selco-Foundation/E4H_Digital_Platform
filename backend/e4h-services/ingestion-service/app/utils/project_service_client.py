import json
from typing import Dict, Any

import requests

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
from app.schemas.vendor_ingestion_shema_response import ResponseInfo

logger = AppLogger().get_logger()


class ProjectServiceClient:
    def __init__(self, project_service_url: str):
        self.project_service_url = project_service_url

    def update_facility_with_supervisor(self, facility_payload:Dict[str,Any]):
        url = f"{self.project_service_url}/facility/supervisor/v1/_update"
        headers = {
            "Content-Type": "application/json"
        }
        payload = facility_payload
        logger.trace(f"Updating facility with supervisor: {url}")
        try:
            response = requests.post(url, headers=headers, json=payload)
            response.raise_for_status()
            logger.info("Facility with supervisor updated successfully")
            logger.debug(f"Update response status: {response.status_code}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error updating facility with supervisor: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error updating facility with supervisor: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error updating facility with supervisor: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error updating facility with supervisor: {req_err}", exc_info=True)
            raise req_err

    def create_project(self, project_payload: Dict[str, Any]):
        url = f"{self.project_service_url}/project/v1/_create"
        headers = {
            "Content-Type": "application/json"
        }
        logger.trace(f"Creating project: {url}")
        try:
            response = requests.post(url, headers=headers, json=project_payload)
            logger.info("Project created successfully")
            logger.debug(f"Project creation response status: {response.status_code}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating project: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating project: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating project: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating project: {req_err}", exc_info=True)
            raise req_err

    def search_project_facilities(self, search_payload: Dict[str, Any], tenant_id: str, limit: int = 1000,
                                  offset: int = 0, include_deleted: bool = False):
        url = f"{self.project_service_url}/project/facility/v1/_search"
        params = {
            "tenantId": tenant_id,
            "limit": limit,
            "offset": offset,
            "includeDeleted": str(include_deleted).lower()
        }

        headers = {
            "Content-Type": "application/json"
        }

        logger.trace(f"Searching project facilities: {url}")
        try:
            response = requests.post(url, headers=headers, params=params, json=search_payload)
            response.raise_for_status()
            result = json.loads(response.text)
            logger.info(f"Project facilities search completed: {result.get('TotalCount', 0)} facilities found")
            logger.debug(f"Search response status: {response.status_code}")
            return result

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error searching project facilities: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching project facilities: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching project facilities: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching project facilities: {req_err}", exc_info=True)
            raise req_err

    def create_project_staff(self, project_staff_payload: Dict[str, Any]):
        url = f"{self.project_service_url}/project/staff/v1/_create"
        headers = {
            "Content-Type": "application/json"
        }

        logger.trace(f"Creating project staff: {url}")
        try:
            response = requests.post(url, headers=headers, json=project_staff_payload)
            response.raise_for_status()
            logger.info("Project staff created successfully")
            logger.debug(f"Project staff creation response status: {response.status_code}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating project staff: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating project staff: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating project staff: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating project staff: {req_err}", exc_info=True)
            raise req_err

    def search_project_staff_by_id(self, project_staff_payload: Dict[str, Any]):
        url = f"{self.project_service_url}/project/staff/v1/_search"
        headers = {
            "Content-Type": "application/json"
        }
        params = {
            "tenantId": "in",
            "limit": "2",
            "offset": "0",
            "includeDeleted": "true"
        }

        logger.trace(f"Searching project staff: {url}")
        try:
            response = requests.post(url, headers=headers, params=params, json=project_staff_payload)
            response.raise_for_status()
            logger.info("Project staff search completed successfully")
            logger.debug(f"Search response status: {response.status_code}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error searching project staff: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching project staff: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching project staff: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching project staff: {req_err}", exc_info=True)
            raise req_err

    def search_project_facility(self, request_info: RequestInfo, project_id: str) -> Dict[str, Any]:
        tenant_id = "in"
        limit = 1000
        offset = 0
        all_facilities = []

        url = f"{self.project_service_url}/project/facility/v1/_search"
        headers = {
            "Content-Type": "application/json"
        }

        try:
            # First request to get total count
            payload = {
                "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
                "ProjectFacility": {
                    "projectId": [project_id]
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
            all_facilities.extend(data.get("ProjectFacilities", []))

            # If more pages are present, fetch them
            while len(all_facilities) < total_count:
                offset += limit
                params["offset"] = offset
                response = requests.post(url, headers=headers, json=payload, params=params)
                response.raise_for_status()
                data = response.json()
                all_facilities.extend(data.get("ProjectFacilities", []))

            return {
                "TotalCount": total_count,
                "ProjectFacilities": all_facilities
            }

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error searching project facility: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching project facility: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching project facility: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching project facility: {req_err}", exc_info=True)
            raise req_err

    def create_project_facility(self, request_info: RequestInfo, project_id: str, facility_id: str):
        url = f"{self.project_service_url}/project/facility/v1/_create"
        headers = {
            "Content-Type": "application/json"
        }

        payload = {
            'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
            'ProjectFacility': {
                'facilityId': facility_id,
                'projectId': project_id,
                'isDeleted': False,
                'tenantId': 'in'
            }
        }
        logger.trace(f"Creating project facility: project_id={project_id}, facility_id={facility_id}")
        try:
            response = requests.post(url, headers=headers, json=payload)
            logger.info(f"Project facility created successfully: project_id={project_id}, facility_id={facility_id}")
            logger.debug(f"Create response: {json.loads(response.text)}")
            return response

        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error creating project facility: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error creating project facility: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error creating project facility: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error creating project facility: {req_err}", exc_info=True)
            raise req_err

    def search_project(self, request_info: RequestInfo, project_id: str):
        url = f"{self.project_service_url}/project/v2/_search"
        headers = {
            "Content-Type":"application/json"
        }
        params = {
            "tenantId": "in",
            "limit": 1,
            "offset": 0,
            "includeAncestors": "false",
            "includeDescendants": "false"
        }
        payload = {
            'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
            'Project': {
                'id': [project_id]
            }
        }
        logger.trace(f"Searching project: project_id={project_id}")
        try:
            response = requests.post(url, params=params, headers=headers, json=payload)
            logger.info(f"Project fetched successfully: project_id={project_id}")
            logger.debug(f"Search response status: {response.status_code}")
            return json.loads(response.text)
        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error searching project: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error searching project: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error searching project: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error searching project: {req_err}", exc_info=True)
            raise req_err

    def update_workflow(self, request_info: RequestInfo, project_id: str, action: str):
        url = f"{self.project_service_url}/project/v1/project/workflow/update"
        headers = {
            "Content-Type":"application/json"
        }
        payload = {
            'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
            'Project': {
                'projectId': [project_id],
                'action': [action]
            }
        }
        logger.trace(f"Updating workflow: project_id={project_id}, action={action}")
        try:
            response = requests.post(url, headers=headers, json=payload)
            logger.info(f"Workflow state updated successfully: project_id={project_id}, action={action}")
            logger.debug(f"Workflow update response status: {response.status_code}")
            return json.loads(response.text)
        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error updating workflow: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error updating workflow: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error updating workflow: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error updating workflow: {req_err}", exc_info=True)
            raise req_err

    def unlink_project_facility(self, request_info: RequestInfo, project_id: str, facility_id: str, project_facility_data: Dict[str, Any] = None):
        """
        Unlink a facility from a project by setting isDeleted to True
        """
        try:
            logger.trace(f"Unlinking project facility: project_id={project_id}, facility_id={facility_id}")
            # Use provided project_facility_data if available, otherwise search for it
            if project_facility_data:
                target_facility = project_facility_data
                logger.debug(f"Using provided ProjectFacility data for facility {facility_id}")
            else:
                # Fallback: Use existing search method to find the ProjectFacility record
                logger.debug(f"Searching for ProjectFacility record for facility {facility_id}")
                search_response = self.search_project_facility(request_info, project_id)
                project_facilities = search_response.get("ProjectFacilities", [])
                
                # Find the specific facility in the results
                target_facility = None
                for pf in project_facilities:
                    if pf.get("facilityId") == facility_id:
                        target_facility = pf
                        break
                
                if not target_facility:
                    logger.warning(f"No ProjectFacility record found for facility {facility_id} and project {project_id}")
                    return None
            
            project_facility_id = target_facility.get("id")
            row_version = target_facility.get("rowVersion")
            
            if not project_facility_id:
                logger.warning("No ID found for ProjectFacility record")
                return None
            
            logger.debug(f"Found ProjectFacility record with ID: {project_facility_id}, rowVersion: {row_version}")
            
            # Now update the record to set isDeleted = True
            update_url = f"{self.project_service_url}/project/facility/v1/_update"
            update_headers = {
                "Content-Type": "application/json"
            }

            # Build ProjectFacility payload - only include rowVersion if present
            project_facility_payload = {
                'id': project_facility_id,
                'facilityId': facility_id,
                'projectId': project_id,
                'isDeleted': True,
                'tenantId': 'in'
            }
            
            # Only add rowVersion if it exists in the source record
            if row_version is not None:
                project_facility_payload['rowVersion'] = row_version

            update_payload = {
                'RequestInfo': request_info.model_dump(by_alias=True, exclude_none=True),
                'ProjectFacility': project_facility_payload
            }
            
            update_response = requests.post(update_url, headers=update_headers, json=update_payload)
            update_response.raise_for_status()
            logger.info(f"Project facility unlinked successfully: project_id={project_id}, facility_id={facility_id}")
            logger.debug(f"Unlink response: {json.loads(update_response.text)}")
            return update_response
            
        except requests.exceptions.HTTPError as http_err:
            logger.error(f"HTTP error unlinking project facility: {http_err}", exc_info=True)
            raise http_err
        except requests.exceptions.ConnectionError as conn_err:
            logger.error(f"Connection error unlinking project facility: {conn_err}", exc_info=True)
            raise conn_err
        except requests.exceptions.Timeout as timeout_err:
            logger.error(f"Timeout error unlinking project facility: {timeout_err}", exc_info=True)
            raise timeout_err
        except requests.exceptions.RequestException as req_err:
            logger.error(f"Request error unlinking project facility: {req_err}", exc_info=True)
            raise req_err