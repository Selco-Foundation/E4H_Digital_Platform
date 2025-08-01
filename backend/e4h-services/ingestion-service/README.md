# Ingestion Service

A [uv](https://docs.astral.sh/uv/) managed python project for various ingestion driven processes in Asset Management.

# Installation

Install uv from https://docs.astral.sh/uv/ and run `uv sync` from root folder (folder with uv.lock file).

# Running the program

1. Ensure that .env has the right values.
2. Port Forward the relevant services from kubectl
3. Run `uv run -m app.main` from root folder.
