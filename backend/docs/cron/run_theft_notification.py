import requests
import os
import time
import uuid

# Tenant ID - overridden by environment variable or default
TENANT_ID = os.getenv("THEFT_NOTIFICATION_TENANT", "in")

# Service host - overridden by environment variable or default
SERVICE_HOST = os.getenv("IM_SERVICES_HOST", "http://localhost:8880")

ENDPOINT = f"{SERVICE_HOST}/im-services/v2/theft-notification"

headers = {
    'Content-Type': 'application/json'
}


def build_theft_notification_request(tenant_id: str) -> dict:
    """Build TheftNotificationRequest body matching the Java API contract."""
    return {
        "RequestInfo": {
            "apiId": "Rainmaker",
            "ver": "1.0",
            "ts": int(time.time() * 1000),
            "action": "_update",
            "did": "cronjob-theft-notification",
            "key": "cronjob-key",
            "msgId": str(uuid.uuid4()),
            "authToken": "cronjob-token",
            "userInfo": {
                "id": None,
                "uuid": "c8ed7e51-c0e5-4552-a420-76eeeee1e1dc",
                "userName": "CRONJOB_THEFT_NOTIFICATION",
                "name": "Cron Job - Theft Notification",
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


if __name__ == "__main__":
    print(f"Calling theft notification for tenant: {TENANT_ID}")
    try:
        body = build_theft_notification_request(TENANT_ID)
        response = requests.post(
            ENDPOINT,
            headers=headers,
            json=body,
            timeout=60
        )
        if response.status_code == 200:
            data = response.json()
            sent = data.get("notificationsSent", 0)
            print(f"Theft notification completed: {sent} SMS sent to CRM")
        else:
            print(f"Theft notification failed - Status: {response.status_code}, Response: {response.text[:200]}")
    except Exception as e:
        print(f"Error calling theft notification: {e}")
