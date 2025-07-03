import json
from typing import List

from fastapi import HTTPException, Depends

from app.core.user_role import UserRole
from app.schemas.request_info import RequestInfo
from app.schemas.role import Role


def get_authorized_request_info(request_info: RequestInfo) -> RequestInfo:
    try:
        if not request_info.user_info or not request_info.user_info.roles:
            raise HTTPException(
                status_code=401,
                detail="Unauthorized: User information or roles are missing",
            )

        user_roles: List[Role] = request_info.user_info.roles
        for role_obj in user_roles:
            for user_role in UserRole:
                if role_obj.name == user_role.value:
                    return request_info

        raise HTTPException(
            status_code=403,
            detail=f"User role(s) '{[role.name for role in user_roles]}' not authorized for this operation. Allowed roles: {[role.value for role in UserRole]}",
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Authorization error: {str(e)}"
        )