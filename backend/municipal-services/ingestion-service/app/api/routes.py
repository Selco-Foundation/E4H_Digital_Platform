from __future__ import annotations
from fastapi import APIRouter

from app.api.endpoints import file_ingestion

api_router = APIRouter()

api_router.include_router(file_ingestion.router, prefix="/ingestion-service", tags=["Ingestion"])
