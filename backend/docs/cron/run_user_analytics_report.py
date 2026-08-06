import requests
import sys

# Triggers the weekly user-analytics report. Generating it also mails the workbook to every
# HRMS holder of the USER_ANALYTICS_REPORT role, so the cron only needs to fire the call and
# can discard the returned workbook.
#
# No weekStartDate is sent, so the service reports the week relative to whenever the cron runs.

url = 'http://im-services-analytics.core-dev:8080/im-services-analytics/v1/user-analytics/_report'

print("Triggering user analytics report")

# The report is not user-scoped and the RequestInfo body is optional, so the call goes out bare.
try:
    response = requests.post(url)
    print(f"Status: {response.status_code}")

    if response.status_code != 200:
        print(f"Response Body: {response.text[:500]}")
        sys.exit(1)

    print(f"Report generated and mailed ({len(response.content)} bytes)")
except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
