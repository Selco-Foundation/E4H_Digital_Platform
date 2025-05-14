from typing import Dict, Any

import requests
from sqlalchemy import null, true


class FacilityServiceClient:
    def __init__(self, facility_service_url: str):
        self.facility_service_url = facility_service_url

    def create_facility(self, facility_payload: Dict[str, Any]):
        url = f"{self.facility_service_url}/facility-service/v2/facility/create"
        headers = {
            "Content-Type": "application/json"
        }
        payload = facility_payload
        try:
            response = requests.post(url, headers=headers, json=payload)
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

    def search_facility(self, tenant_id: str, facility_id: str):

        url = f"{self.facility_service_url}/facility-service/v2/facility/search"
        params = {"tenant_id": tenant_id}

        # Add optional facility_id parameter if provided
        if facility_id:
            params["facility_id"] = "FAC/2025/000039"

        headers = {
            "Accept": "application/json"
        }

        try:
            # response = requests.get(url, headers=headers, params=params)
            response = [
                {
                    "tenant_id": "in",
                    "facility_id": "FAC/2025/000039",
                    "facility_category": "HEALTH",
                    "facility_type": "PHC",
                    "facility_subtype": null,
                    "facility_name": "Gejjalgetta PHC",
                    "facility_ownership": "GOVERNMENT",
                    "facility_region": "RURAL",
                    "address": {
                        "tenantId": "in",
                        "latitude": 17.385044,
                        "longitude": 78.486671,
                        "addressId": null,
                        "addressNumber": "12",
                        "addressLine1": "Main Road",
                        "addressLine2": "Colony Area",
                        "landmark": "Near Water Tank",
                        "city": "Hyderabad",
                        "pincode": "500001",
                        "detail": null,
                        "state": null,
                        "district": null,
                        "block": null
                    },
                    "facility_details": {
                        "hfrId": "HFR123",
                        "pocName": "Dr Asha",
                        "pocContact": "9876543210",
                        "vendorCode": "VND001",
                        "boundaryCode": "India_Telangana_Hyderabad_Gachibowli",
                        "solutionDesignType": "type_7"
                    },
                    "wfStatus": "CREATED",
                    "additionalDetails": null,
                    "isActive": true
                }
            ]
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
