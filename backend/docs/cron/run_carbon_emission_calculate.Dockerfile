FROM python:3.10-slim

WORKDIR /app

RUN pip install requests
COPY run_carbon_emission_calculate.py .

ENTRYPOINT ["python", "run_carbon_emission_calculate.py"]
