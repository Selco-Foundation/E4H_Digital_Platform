import requests
import sys

# Endpoint and headers
url = "http://im-services-analytics.core-dev:8080/im-services-analytics/v1/carbon/trigger?tenantId=in"
headers = {
    "Content-Type": "application/json",
}

# Empty JSON body (trigger endpoint doesn't require payload)
data = {}

try:
    print(f"Calling CO2 carbon emission trigger endpoint: {url}")
    response = requests.post(url, headers=headers, json=data, timeout=600)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")

    if response.status_code == 200:
        print("CO2 carbon emission calculation executed successfully")
    else:
        print(f"CO2 carbon emission trigger returned status {response.status_code}")
        sys.exit(1)

except Exception as e:
    print(f"Error calling CO2 carbon emission trigger endpoint: {e}")
    sys.exit(1)
