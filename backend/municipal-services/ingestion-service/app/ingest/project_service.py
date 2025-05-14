import os
from typing import Dict, Any

from sqlalchemy import null, false

from app.schemas.request_info import RequestInfo
from app.utils.convertor import to_dict, convert_response_to_facility
from app.utils.facility_service_client import FacilityServiceClient
from app.utils.project_service_client import ProjectServiceClient

project_service_url = os.getenv("PROJECT_SERVICE_URL")
facility_service_url = os.getenv("FACILITY_SERVICE_URL")


class ProjectService:

    def __init__(self):
        self.project_client = ProjectServiceClient(project_service_url)
        self.facility_client = FacilityServiceClient(facility_service_url)
        self.facility_service_url = facility_service_url
        self.project_service_url = project_service_url

    def get_facilities(self, request_info: RequestInfo, parent_project_id: str):
        # Prepare the search payload
        search_payload = {
            "RequestInfo": to_dict(request_info),
            "ProjectFacility": {
                "projectId": [parent_project_id]
            }
        }

        # Call the search_project_facilities method
        # response = self.project_client.search_project_facilities(search_payload, tenant_id="in")
        response = {
            "ResponseInfo": {
                "apiId": "facility-api",
                "ver": "1.0",
                "ts": 1747127858298,
                "resMsgId": "msg-003",
                "msgId": "msg-003",
                "status": "successful"
            },
            "TotalCount": 2,
            "ProjectFacilities": [
                {
                    "id": "PROJFACILITY/2025/000009",
                    "tenantId": "in",
                    "source": null,
                    "rowVersion": 1,
                    "applicationId": null,
                    "hasErrors": false,
                    "additionalFields": null,
                    "auditDetails": {
                        "createdBy": "stribi",
                        "lastModifiedBy": "stribi",
                        "createdTime": 1747125449484,
                        "lastModifiedTime": 1747125449484
                    },
                    "facilityId": "facility-001",
                    "projectId": "project-001",
                    "isDeleted": false
                },
                {
                    "id": "PROJFACILITY/2025/000010",
                    "tenantId": "in",
                    "source": null,
                    "rowVersion": 1,
                    "applicationId": null,
                    "hasErrors": false,
                    "additionalFields": null,
                    "auditDetails": {
                        "createdBy": "stribi",
                        "lastModifiedBy": "stribi",
                        "createdTime": 1747127561425,
                        "lastModifiedTime": 1747127561425
                    },
                    "facilityId": "facility-002",
                    "projectId": "project-001",
                    "isDeleted": false
                }
            ]
        }

        facility_data = response.get("ProjectFacilities", [])

        facilities = []
        for facility_item in facility_data:
            facility_id = facility_item.get("facilityId")
            if facility_id:
                response = self.facility_client.search_facility("in", facility_id)
                facilities.append(convert_response_to_facility(response[0]))

        return facilities
