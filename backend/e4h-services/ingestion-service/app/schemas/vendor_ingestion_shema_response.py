from typing import Optional, List, Any

from pydantic import BaseModel


class MDMSAuditDetails(BaseModel):
    createdBy: Optional[str] = None
    lastModifiedBy: Optional[str] = None
    createdTime: Optional[int] = None
    lastModifiedTime: Optional[int] = None


class MDMSDataSource(BaseModel):
    path: Optional[str] = None
    master: Optional[str] = None
    module: Optional[str] = None
    filterType: Optional[str] = None


class MDMSColumn(BaseModel):
    name: Optional[str] = None
    type: Optional[str] = None
    required: Optional[bool] = None
    pattern: Optional[str] = None
    mdmsSource: Optional[MDMSDataSource] = None


class MDMSData(BaseModel):
    id: Optional[int] = None
    columns: Optional[List[MDMSColumn]] = None

    class Config:
        extra = "allow"


class MDMS(BaseModel):
    id: Optional[str] = None
    tenantId: Optional[str] = None
    schemaCode: Optional[str] = None
    uniqueIdentifier: Optional[str] = None
    data: Optional[MDMSData] = None
    isActive: Optional[bool] = None
    auditDetails: Optional[MDMSAuditDetails] = None


class ResponseInfo(BaseModel):
    apiId: Optional[Any] = None
    ver: Optional[Any] = None
    ts: Optional[Any] = None
    resMsgId: Optional[str] = None
    msgId: Optional[Any] = None
    status: Optional[str] = None


class IngestionSchemaResponse(BaseModel):
    response_info: Optional[ResponseInfo] = None
    mdms: Optional[List[MDMS]] = None