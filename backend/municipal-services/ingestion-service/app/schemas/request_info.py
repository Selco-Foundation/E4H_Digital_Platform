from dataclasses import dataclass, field
from typing import Optional

from app.schemas.plain_access_object import PlainAccessRequest
from app.schemas.user import User


@dataclass
class RequestInfo:
    api_id: Optional[str] = None
    ver: Optional[str] = None
    ts: Optional[int] = None
    action: Optional[str] = None
    did: Optional[str] = None
    key: Optional[str] = None
    msg_id: Optional[str] = None
    auth_token: Optional[str] = None
    correlation_id: Optional[str] = None
    plain_access_request: Optional[PlainAccessRequest] = None
    user_info: Optional[User] = None

    @staticmethod
    def builder():
        return RequestInfoBuilder()


class RequestInfoBuilder:
    def __init__(self):
        self._request_info = RequestInfo()

    def api_id(self, value: str):
        self._request_info.api_id = value
        return self

    def ver(self, value: str):
        self._request_info.ver = value
        return self

    def ts(self, value: int):
        self._request_info.ts = value
        return self

    def action(self, value: str):
        self._request_info.action = value
        return self

    def did(self, value: str):
        self._request_info.did = value
        return self

    def key(self, value: str):
        self._request_info.key = value
        return self

    def msg_id(self, value: str):
        self._request_info.msg_id = value
        return self

    def auth_token(self, value: str):
        self._request_info.auth_token = value
        return self

    def correlation_id(self, value: str):
        self._request_info.correlation_id = value
        return self

    def plain_access_request(self, value: PlainAccessRequest):
        self._request_info.plain_access_request = value
        return self

    def user_info(self, value: User):
        self._request_info.user_info = value
        return self

    def build(self):
        return self._request_info
