import requests
import json
import time
import uuid
import os

# Tenant ID is fixed to 'in'
tenant_id = "in"

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
            "id": None,
            "uuid": "c8ed7e51-c0e5-4552-a420-76eeeee1e1dc",
            "userName": "CRONJOB_VISIT_SCHEDULING",
            "name": "Cron Job - Visit Scheduling",
            "mobileNumber": "0000000000",
            "emailId": "cronjob@e4h.com",
            "locale": "en_IN",
            "type": "SYSTEM",
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

# Process tenant
print(f"Processing tenant ID: {tenant_id}")

# Deep copy to avoid modifying shared data
request_info = json.loads(json.dumps(base_request_info))
request_info["RequestInfo"]["ts"] = int(time.time() * 1000)
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
            print(f"Warning: TotalCount={total_count} but no visits found in response. Response keys: {list(data.keys())}")
    else:
        print(f"Search returned status {response.status_code}: {response.text[:200]}")
        visits = []
except Exception as e:
    print(f"Error searching visits: {e}")
    visits = []

print(f"Found {len(visits)} DRAFT visits")

if len(visits) == 0:
    print(f"No DRAFT visits found for tenant {tenant_id}")
else:
    # Step 2: Keep only visits whose scheduled date is before one month from today.
    # This includes overdue (past) visits; the service expires the older ones when
    # the latest visit is scheduled.
    print("Filtering visits whose scheduled date is before one month from today...")
    now_ms = int(time.time() * 1000)
    one_month_ms = 30 * 24 * 60 * 60 * 1000
    schedule_window_end_ms = now_ms + one_month_ms

    eligible_visits = []
    for visit in visits:
        scheduled_date = visit.get("scheduledDate")
        if scheduled_date is None:
            continue

        if scheduled_date <= schedule_window_end_ms:
            eligible_visits.append(visit)

    print(f"{len(eligible_visits)} visits have a scheduled date before one month from today")

    # Step 3: For each (facilityId, amcConfigurationId), keep only the highest visit
    # number. Scheduling that single visit lets the service expire all lower-numbered
    # DRAFT/SCHEDULED visits for the same AMC, and avoids the race where multiple
    # visits of the same AMC are scheduled concurrently and overwrite each other.
    highest_visit_by_amc = {}
    for visit in eligible_visits:
        key = (visit.get("facilityId"), visit.get("amcConfigurationId"))
        visit_number = visit.get("visitNumber") or 0
        current = highest_visit_by_amc.get(key)
        if current is None or visit_number > (current.get("visitNumber") or 0):
            highest_visit_by_amc[key] = visit

    visits_to_schedule = list(highest_visit_by_amc.values())
    print(f"{len(visits_to_schedule)} visits selected for scheduling (highest visit number per facility + AMC configuration)")

    if len(visits_to_schedule) == 0:
        print("No visits are due for scheduling within the next month.")
    else:
        # Step 4: Call /_update for each selected visit
        print("Updating selected visits (service will mark them as SCHEDULED)...")
        update_url = f'{SERVICE_HOST}/asset-amc/v1/visit/_update'
        success_count = 0

        for visit in visits_to_schedule:
            visit_id = visit.get("id")
            visit_to_update = {
                "id": visit.get("id"),
                "tenantId": visit.get("tenantId"),
                "amcConfigurationId": visit.get("amcConfigurationId"),
                "facilityId": visit.get("facilityId"),
                "projectId": visit.get("projectId"),
                "visitNumber": visit.get("visitNumber"),
                "scheduledDate": visit.get("scheduledDate"),
                "status": visit.get("status"),
                "actualVisitDate": visit.get("actualVisitDate"),
                "visitReport": visit.get("visitReport"),
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
                    print(f"Processed visit: {visit_id}")
                else:
                    print(f"Failed to process visit: {visit_id} - Status: {response.status_code}")
            except Exception as e:
                print(f"Error updating visit {visit_id}: {e}")

        print(f"Completed processing tenant {tenant_id}: {success_count}/{len(visits_to_schedule)} selected visits processed")

print("Visit scheduling cron job completed")
