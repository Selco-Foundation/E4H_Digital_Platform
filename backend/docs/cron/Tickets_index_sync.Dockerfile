FROM python:3.10-slim

WORKDIR /app

RUN pip install requests urllib3

COPY Tickets_index_sync.py .

# Default entrypoint, tenant IDs will be passed as CMD
ENTRYPOINT ["python", "Tickets_index_sync.py"]
