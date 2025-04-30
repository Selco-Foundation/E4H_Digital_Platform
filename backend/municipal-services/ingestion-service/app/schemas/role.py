from typing import Optional
from pydantic import BaseModel, Field

class Role(BaseModel):
    id: Optional[int] = None
    name: Optional[str] = Field(default=None, max_length=128)
    code: Optional[str] = Field(default=None, max_length=50)
    tenant_id: Optional[str] = Field(default=None, max_length=256, alias="tenantId")