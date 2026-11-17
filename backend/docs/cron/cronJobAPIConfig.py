import requests
import sys
import json
import shlex
from urllib.parse import urlencode

# Hosts
USER_HOST = "http://egov-user.core-dev:8080"
WORKFLOW_HOST = "http://egov-workflow-v2.core-dev:8080"

# Business services to loop through
business_services = ["Incident","Incident_Low", "Incident_High", "Incident_Medium"]

# Ensure at least one tenant ID is provided
if len(sys.argv) < 2:
    print("Usage: python script.py <tenant_id_1> <tenant_id_2> ...")
    sys.exit(1)

tenant_ids = sys.argv[1:]

for tenant_id in tenant_ids:
    print(f"\n Processing tenant ID: {tenant_id}")

    # Step 1: Fetch SYSTEM user
    user_url = f"{USER_HOST}/user/v1/_search?tenantId={tenant_id}"
    user_payload = {
        "requestInfo": {
            "apiId": "Rainmaker",
            "ver": ".01",
            "ts": None,
            "action": "POST",
            "did": None,
            "key": None,
            "msgId": "8c11c5ca-03bd-11e7-93ae-92361f002671",
            "userInfo": {
                "id": 32
            },
            "authToken": "5eb3655f-31b1-4cd5-b8c2-4f9c033510d4"
        },
        "tenantId": tenant_id,
        "userType": "SYSTEM",
        "userName": "CRONJOB",
        "pageSize": "1",
        "roleCodes": ["SYSTEM"]
    }

    headers = {'Content-Type': 'application/json'}
    user_response = requests.post(user_url, headers=headers, data=json.dumps(user_payload))

    users = user_response.json().get('user', [])
    if not users:
        print(f"System user not found for tenant: {tenant_id}")
        continue

    userInfo = users[0]

    RequestInfo = {
        "apiId": "Rainmaker",
        "ver": ".01",
        "action": "",
        "did": "1",
        "key": "",
        "msgId": "20170310130900|en_IN",
        "requesterId": "",
        "userInfo": userInfo
    }

    # Step 2: Loop through business services
    for service in business_services:
        print(f"Escalating service: {service}")

        method = "POST"
        url = f"{WORKFLOW_HOST}/egov-workflow-v2/egov-wf/auto/{service}/_escalate"
        params = {
            "tenantId": tenant_id,
            "businessService": service
        }
        payload = {"RequestInfo": RequestInfo}

        # Print equivalent curl
        full_url = f"{url}?{urlencode(params)}"
        curl_parts = [f"curl -X {method}", shlex.quote(full_url)]
        for k, v in headers.items():
            curl_parts.append(f"-H {shlex.quote(f'{k}: {v}')}")
        curl_parts.append(f"--data {shlex.quote(json.dumps(payload))}")

        print("\n🌀 Generated curl command:")
        print(" ".join(curl_parts))

        # Perform actual request
        res = requests.request(method, url, params=params, headers=headers, data=json.dumps(payload))
        print(f"🔄 Response Code: {res.status_code}")
        print(f"📩 Response Body: {res.text}\n")