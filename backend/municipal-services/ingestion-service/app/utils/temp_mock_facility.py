from typing import List, Dict, Any


def mockedData() -> List[Dict[str, Any]]:
    return [
        {
            "tenant_id": "in",
            "facility_id": "FAC/2025/000050",
            "facility_category": "HEALTH",
            "facility_type": "PHC",
            "facility_name": "HCC MB",
            "facility_ownership": "GOVERNMENT",
            "facility_region": "RURAL",
            "address": {
                "tenantId": "in",
                "state": "Telangana",
                "district": "Hyderabad",
                "block": "Kondapur"
            },
            "facility_details": {
                "hfrId": 12347,
                "pocName": "ABC",
                "pocContact": 1234567890,
                "vendorCode": "Ven123",
                "boundaryCode": "India_Telangana_Hyderabad_Kondapur",
                "solutionDesignType": "ABC"
            },
            "wfStatus": "CREATED",
            "isActive": True
        },
        {
            "tenant_id": "in",
            "facility_id": "FAC/2025/000051",
            "facility_category": "HEALTH",
            "facility_type": "PHC",
            "facility_name": "HCC AM",
            "facility_ownership": "GOVERNMENT",
            "facility_region": "RURAL",
            "address": {
                "tenantId": "in",
                "state": "Telangana",
                "district": "Hyderabad",
                "block": "Gachibowli"
            },
            "facility_details": {
                "hfrId": 12348,
                "pocName": "ABC",
                "pocContact": 1234567890,
                "vendorCode": "Ven123",
                "boundaryCode": "India_Telangana_Hyderabad_Kondapur",
                "solutionDesignType": "ABC"
            },
            "wfStatus": "CREATED",
            "isActive": True
        }
]