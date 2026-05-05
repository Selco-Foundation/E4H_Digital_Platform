import os
import sys
import requests

# Endpoint and headers
url = os.getenv("RMS_PAUSE_EXPIRE_URL", "http://rms-service.core-dev:8080/rms-service/v1/ticket/pause/_expire")
auth_token = os.getenv("RMS_PAUSE_EXPIRE_AUTH_TOKEN", "")
tenant_id = os.getenv("RMS_PAUSE_EXPIRE_TENANT_ID", "in")
limit = int(os.getenv("RMS_PAUSE_EXPIRE_LIMIT", "500"))

headers = {
    "Content-Type": "application/json"
}
if auth_token:
    headers["Authorization"] = f"Bearer {auth_token}"

payload = {
    "RequestInfo": {
        "apiId": "Rainmaker",
        "authToken": auth_token,
        "userInfo": {
            "tenantId": tenant_id,
            "name": "system-cron"
        }
    },
    "limit": limit
}

try:
    print(f"Calling RMS pause expiry reconciliation endpoint: {url}")
    response = requests.post(url, headers=headers, json=payload, timeout=60)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")

    if response.status_code == 200:
        print("RMS pause expiry reconciliation executed successfully")
    else:
        print(f"RMS pause expiry reconciliation returned status {response.status_code}")
        sys.exit(1)

except Exception as e:
    print(f"Error calling RMS pause expiry reconciliation endpoint: {e}")
    sys.exit(1)

