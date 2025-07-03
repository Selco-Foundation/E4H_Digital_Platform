from typing import List

from fastapi import HTTPException
from starlette import status
from app.schemas.request_info import RequestInfo
from app.schemas.role import Role


async def check_roles(
        allowed_roles: List[Role],
        request_info: RequestInfo
):
    if request_info.user_info.roles not in allowed_roles:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You don't have the required role to access this resource",
        )