import ssl
import pandas as pd
from elasticsearch import Elasticsearch
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# --- Setup Elasticsearch connection ---
def get_es_client(host, username, password):
    context = ssl.create_default_context()
    context.check_hostname = False
    context.verify_mode = ssl.CERT_NONE

    es = Elasticsearch(
        host,
        basic_auth=(username, password),  # ✅ use basic_auth (not http_auth)
        ssl_context=context,
        verify_certs=False,
        headers={
            "Accept": "application/vnd.elasticsearch+json; compatible-with=8",
            "Content-Type": "application/vnd.elasticsearch+json; compatible-with=8"
        }
    )
    return es

# --- Update Documents in a Single Index ---
def update_index(es, index_name, doc_id, new_value):
    try:
        doc = es.get(index=index_name, id=doc_id)
        source = doc["_source"]

        # Update both locations
        source["Data"]["systemFunctional"] = new_value
        if "incident" in source["Data"]:
            source["Data"]["incident"]["systemFunctional"] = new_value

        es.index(index=index_name, id=doc_id, document=source)
        print(f"✅ Updated '{index_name}' _id={doc_id} → systemFunctional={new_value}")
    except Exception as e:
        print(f"❌ Failed to update '{index_name}' _id={doc_id}: {e}")

# --- Update Documents using Excel ---
def update_system_functional(es, indexes, excel_path):
    df = pd.read_excel(excel_path)

    for count, row in df.iterrows():
        doc_id = str(row["Ticket No."]).strip()
        solar_status = str(row["Is the solar system working?"]).strip().lower()

        if solar_status == "yes":
            new_value = "FUNCTIONAL"
        elif solar_status == "no":
            new_value = "NON_FUNCTIONAL"
        else:
            print(f"⚠️ [{count+1}] Skipping invalid value for Ticket No. {doc_id}: '{solar_status}'")
            continue

        for index_name in indexes:
            update_index(es, index_name, doc_id, new_value)

# --- Execute Script ---
if __name__ == "__main__":
    es = get_es_client(
        "https://localhost:9200",
        "elastic",
        "8fwbD6HbJh6HU0oddsHm8TEI"
    )
    indexes_to_update = ["computed-sla-im-services", "im-services"]

    excel_path = "Download Ticket Details (3) PG.xlsx"
    update_system_functional(es, indexes_to_update, excel_path)
