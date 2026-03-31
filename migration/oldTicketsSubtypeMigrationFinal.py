import ssl
from elasticsearch import Elasticsearch, helpers
from elasticsearch.helpers import scan
import urllib3
import pandas as pd
import json
import requests

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# --- Setup Elasticsearch connection ---
def get_es_client(host, user, password):
    context = ssl.create_default_context()
    context.check_hostname = False
    context.verify_mode = ssl.CERT_NONE

    es = Elasticsearch(
        [host],
        http_auth=(user, password),
        ssl_context=context,
        verify_certs=False
    )
    return es

# --- Get Localization Data ---
def get_localization_data():
    url = "http://localhost:8680/localization/messages/v1/_search?module=rainmaker-im&locale=en_IN&tenantId=pg"
    headers = {"Content-Type": "application/json"}
    payload = {
        "RequestInfo": {
            "authToken": "7febd0ac-f7a4-4f53-bddf-49a72e25f600"
        }
    }

    response = requests.post(url, headers=headers, json=payload)
    if response.status_code == 200:
        data = response.json().get("messages", [])
        localization_map = {
            f"{item['code'].strip()}": item["message"]
            for item in data
        }
        return localization_map
    else:
        print(f"❌ Failed to fetch Localization data: {response.status_code}")
        return []

# --- Read Excel Sheet and save data on JSON object ---
def generateJSONFromExcel():
    df = pd.read_excel('UpdatedSubType_ Master Data.xlsx')
    colonnes_voulues = df[['Existing Issue Type', 'Existing Ticket Sub Type ( Saure eMitra)', 'New Ticket Sub type']]
    json_data = colonnes_voulues.to_dict(orient='records')
    issue_map = {
        f"{capitalize(item['Existing Issue Type'].strip())}|{item['Existing Ticket Sub Type ( Saure eMitra)'].strip()}": item["New Ticket Sub type"]
        for item in json_data
    }
    return issue_map

# --- Capitalize Value ---
def capitalize(value):
    if not value:
        return value
    return ' '.join(word[0].upper() + word[1:].lower() if len(word) > 1 else word.upper()
                    for word in value.split())

def uppercase(text):
    if not text:
        return text
    return ' '.join(word.upper() for word in text.split())

# --- Main driver ---
def enrich_es_docs(es, index_name):
    query = {
        "query": {
            "match_all": {}
        }
    }

    subTypeMap = generateJSONFromExcel()
    if not subTypeMap:
        return
    
    localizationMap = get_localization_data()
    if not localizationMap:
        return

    results = scan(client=es, index=index_name, query=query)
    for count, doc in enumerate(results, start=1):
        es_id = doc["_id"]
        es_doc = doc["_source"]

        incidentType = es_doc["Data"]["incident"]["incidentType"]
        incidentSubType = es_doc["Data"]["incident"]["incidentSubType"]
        key = f"{capitalize(incidentType.strip())}|{incidentSubType.strip()}"

        newSubTypeValue = subTypeMap.get(key)
        if newSubTypeValue:
            print(f"\n🔁 Doc {count}: Updating {incidentSubType} → {newSubTypeValue}")
            es_doc["Data"]["incident"]["incidentSubType"] = newSubTypeValue
            localizationKey = f"SERVICEDEFS.{uppercase(newSubTypeValue.strip())}"
            localizationValue = localizationMap.get(localizationKey)
            es_doc["Data"]["incident"]["incidentSubType"] = newSubTypeValue
            es_doc["Data"]["incident"]["incidentSubType_localized"] = localizationValue
            try:
                resp = es.index(index=index_name, id=es_id, body=es_doc)
                print(f"✅ Updated doc {count}: {es_id} → {resp['result']}")
            except Exception as e:
                print(f"❌ Failed to update doc {es_id}: {e}")


# --- Execute ---
if __name__ == "__main__":
    es = get_es_client(
        "https://localhost:9200",
        "elastic",
        "8fwbD6HbJh6HU0oddsHm8TEI"
    )
    enrich_es_docs(es, "computed-sla-im-services")
