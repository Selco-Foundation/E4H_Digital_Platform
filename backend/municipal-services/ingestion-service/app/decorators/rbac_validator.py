import json
from fastapi import HTTPException

from app.core.user_role import UserRole
from app.schemas.request_info import RequestInfo

async def get_authorized_request_info(
        request_info: RequestInfo
) -> RequestInfo:
    try:
        user_roles = set(request_info.user_info.roles)
        if not user_roles.intersection({role.value for role in UserRole}):
            raise HTTPException(
                status_code=403,
                detail=f"User role(s) '{request_info.user_info.roles}' not authorized for this operation. Allowed roles: {[role.value for role in UserRole]}"
            )
        return request_info

    except json.JSONDecodeError:
        raise HTTPException(status_code=400, detail="Invalid RequestInfo format")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Authorization error: {str(e)}")

