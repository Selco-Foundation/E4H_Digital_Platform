import requests
import sys

# Endpoint and headers
url = 'http://rms-service.core-dev:8080/rms-service/v1/trigger'
headers = {
    'Content-Type': 'application/json'
}

# Empty JSON body (RMS trigger endpoint doesn't require payload)
data = {}

try:
    print(f"Calling RMS rule engine trigger endpoint: {url}")
    response = requests.post(url, headers=headers, json=data, timeout=300)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")

    if response.status_code == 200:
        print("RMS rule engine workflow executed successfully")
    else:
        print(f"RMS rule engine trigger returned status {response.status_code}")
        sys.exit(1)

except Exception as e:
    print(f"Error calling RMS rule engine trigger endpoint: {e}")
    sys.exit(1)



