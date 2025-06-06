from typing import Optional
from pydantic import BaseModel, Field
from app.schemas.user import User
from app.schemas.plain_access_object import PlainAccessRequest

class RequestInfo(BaseModel):
    api_id: Optional[str] = Field(default=None, alias="apiId")
    ver: Optional[str] = Field(default=None, alias="ver")
    ts: Optional[int] = None
    action: Optional[str] = Field(default=None, alias="action")
    did: Optional[str] = Field(default=None, alias="did")
    key: Optional[str] = Field(default=None, alias="key")
    msg_id: Optional[str] = Field(default=None, alias="msgId")
    auth_token: Optional[str] = Field(default=None, alias="authToken")
    correlation_id: Optional[str] = Field(default=None, alias="correlationId")
    plain_access_request: Optional[PlainAccessRequest] = Field(default=None, alias="plainAccessRequest")
    user_info: Optional[User] = Field(default=None, alias="userInfo")