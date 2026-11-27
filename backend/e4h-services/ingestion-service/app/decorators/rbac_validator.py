import json
from typing import List

from fastapi import HTTPException, Depends

from app.core.user_role import UserRole
from app.schemas.request_info import RequestInfo
from app.schemas.role import Role
from app.utils.caller_scope import CallerScope, CallerScopeError, resolve_caller_scope


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


def get_caller_scope(request_info: RequestInfo) -> CallerScope:
    """The boundaries the caller may act on in a bulk facility upload, or the HTTP error to answer with.

    See app/utils/caller_scope.py: the caller is identified from the auth token, and an unknown
    identity or an unreadable jurisdiction is refused, not allowed.
    """
    try:
        return resolve_caller_scope(request_info)
    except CallerScopeError as error:
        raise HTTPException(status_code=error.status_code, detail=error.message)
