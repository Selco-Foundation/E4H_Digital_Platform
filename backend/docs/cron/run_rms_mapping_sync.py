#!/usr/bin/env python3
import requests
import sys

# Endpoint and headers
url = 'http://rms-service:8885/rms-service/v1/mapping/sync'
headers = {
    'Content-Type': 'application/json'
}

# Empty JSON body
data = {}

try:
    print(f"Calling RMS mapping sync endpoint: {url}")
    response = requests.post(url, headers=headers, json=data, timeout=300)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")

    if response.status_code == 200:
        print("RMS mapping sync completed successfully")
    else:
        print(f"RMS mapping sync returned status {response.status_code}")
        sys.exit(1)

except Exception as e:
    print(f"Error calling RMS mapping sync endpoint: {e}")
    sys.exit(1)



