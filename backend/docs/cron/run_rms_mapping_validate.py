#!/usr/bin/env python3
import requests
import sys

# Endpoint and headers
url = 'http://rms-service.core-dev:8080/rms-service/v1/mapping/validate'
headers = {
    'Content-Type': 'application/json'
}

# Empty JSON body
data = {}

try:
    print(f"Calling RMS mapping validate endpoint: {url}")
    response = requests.post(url, headers=headers, json=data, timeout=300)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")

    if response.status_code == 200:
        print("RMS mapping validation completed successfully")
    else:
        print(f"RMS mapping validation returned status {response.status_code}")
        sys.exit(1)

except Exception as e:
    print(f"Error calling RMS mapping validate endpoint: {e}")
    sys.exit(1)



