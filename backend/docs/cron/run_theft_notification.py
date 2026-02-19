import requests
import os

# Tenant ID - overridden by environment variable or default
TENANT_ID = os.getenv("THEFT_NOTIFICATION_TENANT", "pg")

# Service host - overridden by environment variable or default
SERVICE_HOST = os.getenv("IM_SERVICES_HOST", "http://localhost:8880")

ENDPOINT = f"{SERVICE_HOST}/im-services/v2/theft-notification"

if __name__ == "__main__":
    print(f"Calling theft notification for tenant: {TENANT_ID}")
    try:
        response = requests.get(
            ENDPOINT,
            params={"tenantId": TENANT_ID},
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
