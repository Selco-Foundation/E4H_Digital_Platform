import requests
import json
import sys
import time
import uuid
import os

# Get tenant IDs from command-line arguments
tenant_ids = sys.argv[1:] if len(sys.argv) > 1 else []

# Service host - will be overridden by environment variable or default
SERVICE_HOST = "http://amc-service.core-dev:8080"
if os.getenv("AMC_SCHEDULER_SERVICE_HOST"):
    SERVICE_HOST = os.getenv("AMC_SCHEDULER_SERVICE_HOST")

headers = {
    'Content-Type': 'application/json'
}

# Base RequestInfo template
base_request_info = {
    "RequestInfo": {
        "apiId": "Rainmaker",
        "ver": "1.0",
        "ts": None,  # Will be set to current timestamp
        "action": "_update",
        "did": "cronjob-visit-scheduling",
        "key": "cronjob-key",
        "msgId": None,  # Will be set to UUID
        "authToken": "cronjob-token",
        "userInfo": {
            "id": 19974,
            "uuid": "32348ff1-491b-427e-b98e-bab6b2dfd8f5",
            "userName": "1234567894",
            "name": "Babs AMC",
            "mobileNumber": "9909994199",
            "emailId": "babs.r@beehyv.com",
            "locale": "en_IN",
            "type": "EMPLOYEE",
            "roles": [],
            "active": True,
            "tenantId": ""  # to be filled per tenant
        },
        "plainAccessRequest": {}
    }
}

# Roles template
role_templates = [
    {"name": "Employee", "code": "EMPLOYEE", "tenantId": ""},
    {"name": "System User", "code": "SYSTEM_USER", "tenantId": ""},
    {"name": "AMC Field Staff", "code": "AMC_FIELD_STAFF", "tenantId": ""},
    {"name": "AMC SPOC", "code": "AMC_SPOC", "tenantId": ""},
    {"name": "AMC Reviewer", "code": "AMC_REVIEWER", "tenantId": ""}
]

# Process tenants
if not tenant_ids:
    tenant_ids = ["in"]  # Default tenant

for tenant_id in tenant_ids:
    print("Processing tenant ID: {tenant_id}")
    
    # Deep copy to avoid modifying shared data
    request_info = json.loads(json.dumps(base_request_info))
    request_info["RequestInfo"]["ts"] = int(time.time() * 1000)
    request_info["RequestInfo"]["msgId"] = str(uuid.uuid4())
    request_info["RequestInfo"]["userInfo"]["tenantId"] = tenant_id
    request_info["RequestInfo"]["userInfo"]["roles"] = [
        {**role, "tenantId": tenant_id} for role in role_templates
    ]
    
    # Step 1: Search for all DRAFT visits
    print("Searching for DRAFT visits...")
    search_url = f'{SERVICE_HOST}/asset-amc/v1/visit/_search?tenantId={tenant_id}&limit=1000&offset=0'
    search_request = {
        "RequestInfo": request_info["RequestInfo"],
        "searchCriteria": {
            "tenantId": tenant_id,
            "statuses": ["DRAFT"]
        }
    }
    
    try:
        response = requests.post(search_url, headers=headers, json=search_request, timeout=60)
        if response.status_code == 200:
            data = response.json()
            visits = data.get("ScheduledVisits", [])
            total_count = data.get("TotalCount", 0)
            if total_count > 0 and len(visits) == 0:
                print("Warning: TotalCount={total_count} but no visits found in response. Response keys: {list(data.keys())}")
        else:
            print("Search returned status {response.status_code}: {response.text[:200]}")
            visits = []
    except Exception as e:
        print("Error searching visits: {e}")
        visits = []
    
    print("Found {len(visits)} DRAFT visits")
    
    if len(visits) == 0:
        print("No DRAFT visits found for tenant {tenant_id}")
        continue
    
    # Step 2: Call /_update for each visit
    print("Updating visits (service will check notice period and apply SCHEDULE if needed)...")
    update_url = f'{SERVICE_HOST}/asset-amc/v1/visit/_update'
    success_count = 0
    
    for visit in visits:
        visit_id = visit.get("id")
        visit_to_update = {
            "id": visit.get("id"),
            "tenantId": visit.get("tenantId"),
            "amcConfigurationId": visit.get("amcConfigurationId"),
            "facilityId": visit.get("facilityId"),
            "visitNumber": visit.get("visitNumber"),
            "scheduledDate": visit.get("scheduledDate"),
            "status": visit.get("status"),
            "assignments": visit.get("assignments", []),
            "additionalDetails": visit.get("additionalDetails")
        }
        
        update_request = {
            "RequestInfo": request_info["RequestInfo"],
            "ScheduledVisit": [visit_to_update]
        }
        
        try:
            response = requests.post(update_url, headers=headers, json=update_request, timeout=30)
            if response.status_code == 202:  # ACCEPTED
                success_count += 1
                print("Processed visit: {visit_id}")
            else:
                print("Failed to process visit: {visit_id} - Status: {response.status_code}")
        except Exception as e:
            print("Error updating visit {visit_id}: {e}")
    
    print("Completed processing tenant {tenant_id}: {success_count}/{len(visits)} visits processed")

print("Visit scheduling cron job completed")
