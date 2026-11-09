from elasticsearch import Elasticsearch
import psycopg2

# --- Connect to Elasticsearch ---
es = Elasticsearch(
    hosts=["https://elasticsearch-master.backbone-dev:9200"],  
    basic_auth=("elastic", "8fwbD6HbJh6HU0oddsHm8TEI") 
)

# Fetch all docs from index (<10k limit)
res = es.search(
    index="computed-sla-im-services",
    size=10000,
    body={"query": {"match_all": {}}}
)

# Collect _id values from ES
es_ids = {hit["_id"] for hit in res["hits"]["hits"]}
print(f"Fetched {len(es_ids)} IDs from Elasticsearch")

# --- Connect to Postgres DB ---
try:
    conn = psycopg2.connect(
        host="selco-prod-db.c1yks4g2c2zp.ap-south-1.rds.amazonaws.com",
        port="5432",
        dbname="selcouatdb",
        user="selcouatadmin",
        password="selcouat1234"
    )
    cursor = conn.cursor()

    # Delete rows where incidentid not in ES
    query = """
        DELETE FROM eg_incident_v2
        WHERE incidentid NOT IN %s
    """
    cursor.execute(query, (tuple(es_ids),))
    deleted = cursor.rowcount
    conn.commit()

    print(f"Deleted {deleted} rows from eg_incident_v2")

    cursor.close()
    conn.close()
except Exception as e:
    print("DB Error:", e)
