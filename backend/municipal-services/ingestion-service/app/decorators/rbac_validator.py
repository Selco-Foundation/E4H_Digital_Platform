import json
from fastapi import HTTPException, Depends

from app.core.user_role import UserRole
from app.schemas.request_info import RequestInfo

def get_authorized_request_info(request_info: RequestInfo) -> RequestInfo:
    try:
        if not request_info.user_info or not request_info.user_info.roles:
            raise HTTPException(
                status_code=401,
                detail="Unauthorized: User information or roles are missing",
            )

        user_roles = request_info.user_info.roles
        for role_str in user_roles:
            for user_role in UserRole:
                if role_str["name"] == user_role.value:
                    return request_info

        raise HTTPException(
            status_code=403,
            detail=f"User role(s) '{user_roles}' not authorized for this operation. Allowed roles: {[role.value for role in UserRole]}",
        )

    except json.JSONDecodeError:
        raise HTTPException(status_code=400, detail="Invalid RequestInfo format")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Authorization error: {str(e)}"
        )

