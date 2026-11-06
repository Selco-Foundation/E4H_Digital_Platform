from fastapi import APIRouter
from fastapi_health import health

router = APIRouter()

def is_healthy():
    return True

router.add_api_route("", health([is_healthy]))
