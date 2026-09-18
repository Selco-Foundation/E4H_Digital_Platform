from __future__ import annotations
from fastapi import APIRouter

from app.api.endpoints import file_ingestion, template_generation, health_check, document_merge

api_router = APIRouter()

api_router.include_router(file_ingestion.router, prefix="/ingestion-service/ingest", tags=["Ingestion"])
api_router.include_router(template_generation.router, prefix="/ingestion-service/template", tags=["Template"])
api_router.include_router(health_check.router, prefix="/ingestion-service/health", tags=["Health"])
api_router.include_router(document_merge.router, prefix="/ingestion-service/document", tags=["Document"])
