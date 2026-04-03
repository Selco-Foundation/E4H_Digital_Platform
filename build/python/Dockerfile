# ===== Build stage =====
FROM python:3.12-slim AS builder

WORKDIR /app

ARG WORK_DIR

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq-dev \
    build-essential \
 && rm -rf /var/lib/apt/lists/*

# Install uv
RUN pip install --no-cache-dir uv

# Copy dependency files first for layer caching
COPY ${WORK_DIR}/pyproject.toml ./
COPY ${WORK_DIR}/uv.lock ./

# Install only prod dependencies
RUN uv sync --frozen --no-dev

# Copy application code
COPY ${WORK_DIR}/app ./app

# ===== Runtime stage =====
FROM python:3.12-slim

WORKDIR /app

# Install only runtime deps
RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq5 \
 && rm -rf /var/lib/apt/lists/*  #only for postgres connection

# Install uv (needed for CMD)
RUN pip install --no-cache-dir uv

# Copy everything from build stage
COPY --from=builder /app /app

EXPOSE 8080

CMD ["uv", "run", "python", "-m", "app.main"]
