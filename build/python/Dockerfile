FROM python:3.12-slim

WORKDIR /app

ARG WORK_DIR

RUN pip install uv


COPY ${WORK_DIR}/pyproject.toml ./
COPY ${WORK_DIR}/uv.lock ./

RUN apt-get update && apt-get install -y \
    libpq-dev \
    python3-dev \
    build-essential && uv sync

COPY ${WORK_DIR}/app ./
COPY ${WORK_DIR}/.env ./

EXPOSE 8080

CMD ["uv", "run", "python", "-m", "app.main"]