"""The geography the signed-in caller may act on in a bulk facility upload.

A facility administrator is scoped to the boundaries in their HRMS jurisdictions. The bulk screens
narrow their own dropdowns to those, but that is a convenience: a state-only selection is sent as
the state's code, and anything posted directly skips the screen altogether. So the template and the
upload apply the same limit here.

Who the caller is comes from the auth token, not from the `userInfo` the request carries. That
object arrives as a form field in a multipart request, which the gateway does not rewrite, so a
hand-made request could name any user in it.

**This fails closed.** A caller whose identity or jurisdictions cannot be determined, or who holds
no jurisdiction, is refused rather than given the whole country.

The decisions are pure and live in `CallerScope`; `resolve_caller_scope` is the only part that
calls services.
"""
import os
from dataclasses import dataclass
from typing import Any, Dict, Iterable, List, Optional, Sequence, Tuple

from app.schemas.request_info import RequestInfo
from app.utils.geography_scope import is_within_scope
from app.utils.hrms_service_client import HRMSServiceClient
from app.utils.user_service_client import UserServiceClient

# The role whose jurisdictions bound a bulk facility upload.
FACILITY_ADMIN_ROLE = "FACILITY_ADMIN"

# This service, or a scheduled job, acting for itself (the identity facility_service_client builds).
SYSTEM_USER_ROLE = "SYSTEM_USER"
SYSTEM_USER_TYPE = "SYSTEM"

_COUNTRY_LEVEL = "country"


class CallerScopeError(Exception):
    """The caller may not proceed. `status_code` is the HTTP status the endpoint should answer with."""

    def __init__(self, status_code: int, message: str):
        super().__init__(message)
        self.status_code = status_code
        self.message = message


@dataclass(frozen=True)
class CallerScope:
    unrestricted: bool
    codes: Tuple[str, ...] = ()

    def allows(self, boundary_code: Optional[str]) -> bool:
        """Whether this boundary sits at or below something the caller may act on."""
        return self.unrestricted or is_within_scope(boundary_code or "", self.codes)

    def filter_blocks(self, blocks: Iterable[Any]) -> List[Any]:
        """The blocks (anything with a `.code`) inside the caller's scope."""
        if self.unrestricted:
            return list(blocks)
        return [block for block in blocks if self.allows(getattr(block, "code", ""))]


UNRESTRICTED = CallerScope(unrestricted=True)


def _role_codes(roles: Optional[Iterable[Any]]) -> List[str]:
    codes = []
    for role in roles or []:
        code = role.get("code") if isinstance(role, dict) else getattr(role, "code", None)
        if code:
            codes.append(str(code))
    return codes


def is_system_caller(user: Optional[Dict[str, Any]]) -> bool:
    """This service acting for itself: user type SYSTEM and the SYSTEM_USER role, both required."""
    if not user:
        return False
    return (str(user.get("type") or "").upper() == SYSTEM_USER_TYPE
            and SYSTEM_USER_ROLE in _role_codes(user.get("roles")))


def scope_from_jurisdictions(jurisdictions: Optional[Sequence[Dict[str, Any]]],
                             role_code: str = FACILITY_ADMIN_ROLE) -> CallerScope:
    """The scope the active jurisdictions give for `role_code`.

    A jurisdiction with no roles applies to every role the user holds, so it counts; one that names
    other roles only does not. A country-level jurisdiction covers everything. No usable
    jurisdiction at all is an error, never an empty scope that reads as "no limit".
    """
    codes: List[str] = []
    for jurisdiction in jurisdictions or []:
        if not jurisdiction or jurisdiction.get("isActive") is False:
            continue
        boundary = str(jurisdiction.get("boundary") or "").strip()
        if not boundary:
            continue
        roles = _role_codes(jurisdiction.get("roles"))
        if roles and role_code not in roles:
            continue
        if str(jurisdiction.get("boundaryType") or "").strip().lower() == _COUNTRY_LEVEL:
            return UNRESTRICTED
        codes.append(boundary)

    if not codes:
        raise CallerScopeError(403, "You have no jurisdiction assigned for bulk facility upload")
    return CallerScope(unrestricted=False, codes=tuple(dict.fromkeys(codes)))


def resolve_caller_scope(request_info: RequestInfo) -> CallerScope:
    """Who the caller is (from the token), and the boundaries they may act on."""
    token = request_info.auth_token if request_info else None
    if not token:
        raise CallerScopeError(401, "Unauthorized: no auth token on the request")

    user_service_url = os.getenv("USER_SERVICE_URL")
    hrms_service_url = os.getenv("HRMS_SERVICE_URL")
    if not user_service_url or not hrms_service_url:
        raise CallerScopeError(
            503, "Cannot check your jurisdiction: USER_SERVICE_URL or HRMS_SERVICE_URL is not configured")

    try:
        user = UserServiceClient(user_service_url).get_user_by_token(token)
    except Exception:
        raise CallerScopeError(503, "Could not verify who you are: the user service did not answer")
    if not user:
        raise CallerScopeError(401, "Unauthorized: the auth token is not valid")

    if is_system_caller(user):
        return UNRESTRICTED

    if FACILITY_ADMIN_ROLE not in _role_codes(user.get("roles")):
        raise CallerScopeError(403, f"The {FACILITY_ADMIN_ROLE} role is required for bulk facility upload")

    uuid = user.get("uuid")
    if not uuid:
        raise CallerScopeError(503, "Could not determine your user id")
    try:
        employee = HRMSServiceClient(hrms_service_url).get_employee_by_uuid(uuid, request_info)
    except Exception:
        raise CallerScopeError(503, "Could not read your jurisdictions: HRMS did not answer")
    if not employee:
        raise CallerScopeError(403, "You have no jurisdiction assigned for bulk facility upload")

    return scope_from_jurisdictions(employee.get("jurisdictions"))
