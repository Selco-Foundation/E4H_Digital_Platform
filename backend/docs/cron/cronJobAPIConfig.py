import requests
import sys
import json
import shlex
from urllib.parse import urlencode

# Command-line arguments
argumentList = sys.argv
if len(argumentList) < 3:
    print("Usage: python script.py <job_name> <tenant_id>")
    sys.exit(1)

job_name = argumentList[1]
tenant_id = argumentList[2]

# Calls MDMS service to fetch cron job API endpoints configuration
mdms_url = "http://egov-mdms-service.core-dev:8080/egov-mdms-service/v1/_search"
mdms_payload = {
    "RequestInfo": {
        "apiId": "asset-services",
        "ver": None,
        "ts": None,
        "action": None,
        "did": None,
        "key": None,
        "msgId": "search with from and to values",
        "authToken": "f81648a6-bfa0-4a5e-afc2-57d751f256b7"
    },
    "MdmsCriteria": {
        "tenantId": tenant_id,
        "moduleDetails": [
            {
                "moduleName": "common-masters",
                "masterDetails": [
                    {
                        "name": "CronJobAPIConfig"
                    }
                ]
            }
        ]
    }
}
mdms_headers = {'Content-Type': 'application/json'}
response = requests.post(mdms_url, headers=mdms_headers, data=json.dumps(mdms_payload))

# Convert the response to JSON
mdms_data = response.json()

# Call user search to fetch SYSTEM user
user_url = f"http://egov-user.core-dev:8080/user/v1/_search?tenantId={tenant_id}"
user_payload = {
    "requestInfo": {
        "apiId": "Rainmaker",
        "ver": ".01",
        "ts": None,
        "action": "POST",
        "did": None,
        "key": None,
        "msgId": "8c11c5ca-03bd-11e7-93ae-92361f002671",
        "userInfo": {
            "id": 32
        },
        "authToken": "5eb3655f-31b1-4cd5-b8c2-4f9c033510d4"

    },
    "tenantId": tenant_id,
    "userType": "SYSTEM",
    "userName": "CRONJOB",
    "pageSize": "1",
    "roleCodes": ["SYSTEM"]
}
user_headers = {'Content-Type': 'application/json'}
user_response = requests.post(user_url, headers=user_headers, data=json.dumps(user_payload))
users = user_response.json()['user']

if len(users) == 0:
    raise Exception("System user not found")
else:
    userInfo = users[0]

RequestInfo = {
    "apiId": "Rainmaker",
    "ver": ".01",
    "action": "",
    "did": "1",
    "key": "",
    "msgId": "20170310130900|en_IN",
    "requesterId": "",
    "userInfo": userInfo
}

# Looping through each entry in the config
for data in mdms_data["MdmsRes"]["common-masters"]["CronJobAPIConfig"]:

    if data["active"].lower() == "true" and data["jobName"] == job_name:
        method = data["method"]
        url = f"http://egov-workflow-v2.core-dev:8080/egov-workflow-v2/egov-wf/auto/Incident/_escalate?tenantId={tenant_id}"  # override

        headers = data.get("header", {})
        payload = data.get("payload", None)
        params = data.get("params", None)

        if payload and "RequestInfo" in payload:
            if payload["RequestInfo"] == "{DEFAULT_REQUESTINFO}":
                payload["RequestInfo"] = RequestInfo

        full_url = url
        if params:
            full_url += '&' + urlencode(params)

        # Print the equivalent curl command
        curl_parts = [f"curl -X {method}", shlex.quote(full_url)]
        for k, v in headers.items():
            curl_parts.append(f"-H {shlex.quote(f'{k}: {v}')}")
        if payload:
            curl_parts.append(f"--data {shlex.quote(json.dumps(payload))}")

        print("\n Generated curl command:\n")
        print(" ".join(curl_parts) + "\n")

        # Perform actual request
        res = requests.request(method, url, params=params, headers=headers, data=json.dumps(payload))