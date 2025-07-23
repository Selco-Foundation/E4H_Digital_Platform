import requests
import json
import sys

# Get tenant IDs from command-line arguments
tenant_ids = sys.argv[1:]

if not tenant_ids:
    print("Usage: python run_sla.py <tenant_id1> <tenant_id2> ...")
    sys.exit(1)

# Endpoint and headers
url = 'http://im-services-analytics.core-dev:8080/im-services-analytics/v1/computeSLA?transform=false'
headers = {
    'Content-Type': 'application/json'
}

# Base payload template
base_data = {
    "RequestInfo": {
        "apiId": "Rainmaker",
        "authToken": "79967889-fbf5-42c6-9bd3-4adc0dbe7692",
        "userInfo": {
            "id": 95,
            "uuid": "cd831d19-3799-4e73-a52a-237930f1e450",
            "userName": "7346864311",
            "name": "Akhila",
            "mobileNumber": "9901224633",
            "emailId": None,
            "locale": None,
            "type": "EMPLOYEE",
            "roles": [],
            "active": True,
            "tenantId": ""  # to be filled per tenant
        },
        "msgId": "1744021633700|en_IN",
        "plainAccessRequest": {}
    },
    "tenantId": ""  # to be filled per tenant
}

# Roles template
role_templates = [
    {"name": "Complainant", "code": "COMPLAINANT", "tenantId": ""},
    {"name": "Employee", "code": "EMPLOYEE", "tenantId": ""},
    {"name": "Complaint Assessor", "code": "COMPLAINT_ASSESSOR", "tenantId": ""},
    {"name": "Super User", "code": "SUPERUSER", "tenantId": ""}
]

# Loop through tenant IDs
for tenant_id in tenant_ids:
    # Deep copy to avoid modifying shared data
    data = json.loads(json.dumps(base_data))
    data["tenantId"] = tenant_id
    data["RequestInfo"]["userInfo"]["tenantId"] = tenant_id
    data["RequestInfo"]["userInfo"]["roles"] = [
        {**role, "tenantId": tenant_id} for role in role_templates
    ]

    # Make the request
    try:
        response = requests.post(url, headers=headers, json=data)
        print(f"[{tenant_id}] Status: {response.status_code}")
        print(response.text[:500])  # Print first 500 chars to avoid overload
    except Exception as e:
        print(f"[{tenant_id}] Error: {e}")
