FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Copy script
COPY run_user_analytics_report.py .

# Install dependencies
RUN pip install requests

# Default entrypoint, tenant IDs will be passed as CMD
ENTRYPOINT ["python", "run_user_analytics_report.py"]

