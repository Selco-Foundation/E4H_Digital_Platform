import os

from app.core.logging import AppLogger
from app.schemas.request_info import RequestInfo
from app.utils.convertor import convert_response_to_facility
from app.utils.facility_service_client import FacilityServiceClient
from app.utils.project_service_client import ProjectServiceClient

logger = AppLogger().get_logger()

project_service_url = os.getenv("PROJECT_SERVICE_URL")
facility_service_url = os.getenv("FACILITY_SERVICE_URL")


class ProjectService:

    def __init__(self):
        self.project_client = ProjectServiceClient(project_service_url)
        self.facility_client = FacilityServiceClient(facility_service_url)
        self.facility_service_url = facility_service_url
        self.project_service_url = project_service_url

    def get_facilities(self, request_info: RequestInfo, parent_project_id: str, role_type: str):
        logger.trace(f"Getting facilities for project: {parent_project_id}, role_type: {role_type}")
        # Prepare the search payload
        search_payload = {
            "RequestInfo":request_info.model_dump(by_alias=True, exclude_none=True),
            "ProjectFacility": {
                "projectId": [parent_project_id]
            }
        }

        # Call the search_project_facilities method
        logger.info(f"Searching project facilities for project: {parent_project_id}")
        response = self.project_client.search_project_facilities(search_payload, tenant_id="in")
        facility_data = response["ProjectFacilities"]
        logger.debug(f"Found {len(facility_data)} project facilities")

        facilities = []
        for facility_item in facility_data:
            facility_id = facility_item.get("facilityId")
            if facility_id:
                logger.trace(f"Fetching facility details: facility_id={facility_id}")
                response = self.facility_client.search_facility(tenant_id="in", facility_id=facility_id)
                if response:
                    for facility_data in response.get("facilities", []):
                        facilities.append(convert_response_to_facility(facility_data, role_type))

        logger.info(f"Retrieved {len(facilities)} facilities for project {parent_project_id} with role_type {role_type}")
        return facilities
