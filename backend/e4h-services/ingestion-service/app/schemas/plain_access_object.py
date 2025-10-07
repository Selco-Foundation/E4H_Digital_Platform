from typing import List, Optional, Any
from pydantic import BaseModel, Field

class PlainAccessRequest(BaseModel):
    record_id: Optional[str] = Field(default=None, alias="recordId")
    plain_request_fields: Optional[List[str]] = Field(default=None, alias="plainRequestFields")
    
    class Config:
        extra = "allow"  # Allow extra fields in the model