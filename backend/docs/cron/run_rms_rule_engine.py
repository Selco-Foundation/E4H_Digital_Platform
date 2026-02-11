import requests
import threading

url = 'http://rms-service.core-dev:8080/rms-service/v1/trigger'
headers = {'Content-Type': 'application/json'}

def trigger():
    try:
        requests.post(url, headers=headers, json={})
    except:
        pass  # ignore errors (fire-and-forget)

print("Triggering RMS rule engine...")
threading.Thread(target=trigger, daemon=True).start()

print("Request sent. Exiting script.")

