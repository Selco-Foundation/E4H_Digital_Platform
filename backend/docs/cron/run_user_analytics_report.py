import os
import requests
import sys

# Triggers the weekly user-analytics report. Generating it also mails the workbook to every
# HRMS holder of the USER_ANALYTICS_REPORT role, so the cron only needs to fire the call and
# can discard the returned workbook.
#
# No weekStartDate is sent, so the service reports the week relative to whenever the cron runs.

# Defaults to the in-cluster service. Override with ANALYTICS_URL to run against a local
# instance, e.g. ANALYTICS_URL=http://localhost:8099/im-services-analytics/v1/user-analytics/_report
url = os.environ.get(
    'ANALYTICS_URL',
    'http://im-services-analytics.core-dev:8080/im-services-analytics/v1/user-analytics/_report')

# The report is not user-scoped, so RequestInfo carries no real caller. It is sent only to keep
# the call shaped like every other POST behind the gateway.
request_body = {
    "RequestInfo": {
        "apiId": "Rainmaker",
        "ver": ".01",
        "msgId": "20170310130900|en_IN",
        "authToken": "<AUTH_TOKEN>"
    }
}

print("Triggering user analytics report")

try:
    response = requests.post(url, json=request_body)
    print(f"Status: {response.status_code}")

    if response.status_code != 200:
        print(f"Response Body: {response.text[:500]}")
        sys.exit(1)

    print(f"Report generated and mailed ({len(response.content)} bytes)")
except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
