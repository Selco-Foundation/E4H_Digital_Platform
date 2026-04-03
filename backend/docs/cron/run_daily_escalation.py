import requests
import json
import sys

# Endpoint and headers
url = 'http://im-services-analytics.core-dev:8080/im-services-analytics/v1/escalation-emails/daily'
headers = {
    'Content-Type': 'application/json'
}

# Base payload template
base_data = {
    "RequestInfo": {
        "apiId": "im-services-analytics",
        "ver": "1.0",
        "ts": None,  # Will be set to current timestamp
        "action": "_create",
        "did": "cronjob-daily-escalation",
        "key": "cronjob-key",
        "msgId": None,  # Will be set to UUID
        "authToken": "cronjob-token",
        "userInfo": {
            "id": None,
            "uuid": None,  # Will be set to UUID
            "userName": "CRONJOB_DAILY_ESCALATION",
            "name": "Cron Job - Daily Escalation",
            "mobileNumber": "0000000000",
            "emailId": "cronjob@e4h.com",
            "locale": "en_IN",
            "type": "SYSTEM",
            "roles": [],
            "active": True,
            "tenantId": "in"
        },
        "plainAccessRequest": {}
    }
}

# Set timestamp and message ID
import time
import uuid

base_data["RequestInfo"]["ts"] = int(time.time() * 1000)
base_data["RequestInfo"]["msgId"] = str(uuid.uuid4())
base_data["RequestInfo"]["userInfo"]["uuid"] = str(uuid.uuid4())

# Make the request
try:
    print(f"Calling daily escalation endpoint: {url}")
    response = requests.post(url, headers=headers, json=base_data, timeout=300)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")
    
    if response.status_code == 200:
        print("Daily escalation processing completed successfully")
    else:
        print(f"Daily escalation processing returned status {response.status_code}")
        sys.exit(1)
        
except Exception as e:
    print(f"Error calling daily escalation endpoint: {e}")
    sys.exit(1)

