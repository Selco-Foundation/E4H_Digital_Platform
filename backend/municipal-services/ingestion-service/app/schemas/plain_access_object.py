from typing import List, Optional
from pydantic import BaseModel, Field

class PlainAccessRequest(BaseModel):
    record_id: Optional[str] = Field(default=None, alias="recordId")
    plain_request_fields: List[str] = Field(default_factory=list, alias="plainRequestFields")