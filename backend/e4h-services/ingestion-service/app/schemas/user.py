from typing import List, Optional
from pydantic import BaseModel, Field
from app.schemas.role import Role

class User(BaseModel):
    id: Optional[int] = None
    user_name: Optional[str] = Field(default=None, max_length=180, alias="userName")
    name: Optional[str] = Field(default=None, max_length=250)
    type: Optional[str] = Field(default=None, max_length=50)
    mobile_number: Optional[str] = Field(default=None, max_length=150, alias="mobileNumber")
    email_id: Optional[str] = Field(default=None, max_length=300, alias="emailId")
    roles: Optional[List[Role]] = []
    tenant_id: Optional[str] = Field(default=None, max_length=256, alias="tenantId")
    uuid: Optional[str] = Field(default=None, max_length=36, alias="uuid")