from fastapi import APIRouter
from fastapi_health import health

from app.core.logging import AppLogger

logger = AppLogger().get_logger()

router = APIRouter()

def is_healthy():
    logger.trace("Health check endpoint called")
    logger.debug("Service is healthy")
    return True

router.add_api_route("", health([is_healthy]))
