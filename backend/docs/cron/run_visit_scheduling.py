import requests
import json
import sys
import time
import uuid

# Get tenant IDs from command-line arguments
tenant_ids = sys.argv[1:] if len(sys.argv) > 1 else []

# Service host - will be overridden by environment variable or default
SERVICE_HOST = "http://amc-service.core-dev:8080"

# Override with environment variable if set
import os
if os.getenv("AMC_SCHEDULER_SERVICE_HOST"):
    SERVICE_HOST = os.getenv("AMC_SCHEDULER_SERVICE_HOST")

headers = {
    'Content-Type': 'application/json'
}

# Base RequestInfo template
def create_request_info(tenant_id):
    return {
        "RequestInfo": {
            "apiId": "Rainmaker",
            "ver": "1.0",
            "ts": int(time.time() * 1000),
            "action": "_update",
            "did": "cronjob-visit-scheduling",
            "key": "cronjob-key",
            "msgId": str(uuid.uuid4()),
            "authToken": "cronjob-token",
            "userInfo": {
                "uuid": str(uuid.uuid4()),
                "userName": "CRONJOB_VISIT_SCHEDULING",
                "name": "Cron Job - Visit Scheduling",
                "mobileNumber": "0000000000",
                "emailId": "cronjob@e4h.com",
                "locale": "en_IN",
                "type": "SYSTEM",
                "roles": [],
                "active": True,
                "tenantId": tenant_id
            },
            "plainAccessRequest": {}
        }
    }

def search_draft_visits(tenant_id, request_info):
    """Search for all DRAFT visits"""
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
                print(f"⚠️  Warning: TotalCount={total_count} but no visits found in response. Response keys: {list(data.keys())}")
            return visits
        else:
            print(f"⚠️  Search returned status {response.status_code}: {response.text[:200]}")
            return []
    except Exception as e:
        print(f"❌ Error searching visits: {e}")
        return []

def update_visit(visit, request_info):
    """Call /asset-amc/v1/visit/_update for a visit
    The service will internally check notice period from MDMS and apply SCHEDULE action if needed
    """
    update_url = f'{SERVICE_HOST}/asset-amc/v1/visit/_update'
    
    # Prepare visit update - the service will check if it needs scheduling
    visit_to_update = {
        "id": visit.get("id"),
        "tenantId": visit.get("tenantId"),
        "amcConfigurationId": visit.get("amcConfigurationId"),
        "facilityId": visit.get("facilityId"),
        "visitNumber": visit.get("visitNumber"),
        "scheduledDate": visit.get("scheduledDate"),
        "status": visit.get("status"),  # Keep current status, service will update if needed
        "assignments": visit.get("assignments", []),
        "additionalDetails": visit.get("additionalDetails")
    }
    
    update_request = {
        "RequestInfo": request_info["RequestInfo"],
        "ScheduledVisit": [visit_to_update]
    }
    
    try:
        response = requests.post(update_url, headers=headers, json=update_request, timeout=30)
        return response.status_code == 202  # ACCEPTED
    except Exception as e:
        print(f"❌ Error updating visit {visit.get('id')}: {e}")
        return False

# Process tenants
if not tenant_ids:
    tenant_ids = ["in"]  # Default tenant

for tenant_id in tenant_ids:
    print(f"\n🔄 Processing tenant ID: {tenant_id}")
    
    request_info = create_request_info(tenant_id)
    
    # Step 1: Search for all DRAFT visits
    print("🔍 Searching for DRAFT visits...")
    visits = search_draft_visits(tenant_id, request_info)
    print(f"✅ Found {len(visits)} DRAFT visits")
    
    if len(visits) == 0:
        print(f"ℹ️  No DRAFT visits found for tenant {tenant_id}")
        continue
    
    # Step 2: Call /_update for each visit
    # The service will internally:
    # - Fetch notice period from MDMS (amc.AMCThresholds.amc_visit_notice_period_in_days)
    # - Check if scheduled_date < current_date + notice_period
    # - Apply SCHEDULE workflow action if needed
    print("📝 Updating visits (service will check notice period and apply SCHEDULE if needed)...")
    success_count = 0
    for visit in visits:
        visit_id = visit.get("id")
        if update_visit(visit, request_info):
            success_count += 1
            print(f"✅ Processed visit: {visit_id}")
        else:
            print(f"⚠️  Failed to process visit: {visit_id}")
    
    print(f"\n✅ Completed processing tenant {tenant_id}: {success_count}/{len(visits)} visits processed")

print("\n✅ Visit scheduling cron job completed")
