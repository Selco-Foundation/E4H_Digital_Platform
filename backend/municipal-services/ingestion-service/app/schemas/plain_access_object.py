from dataclasses import dataclass, field
from typing import List, Optional

@dataclass
class PlainAccessRequest:
    record_id: Optional[str] = None
    plain_request_fields: List[str] = field(default_factory=list)

    @staticmethod
    def builder():
        return PlainAccessRequestBuilder()


class PlainAccessRequestBuilder:
    def __init__(self):
        self._plain_access_request = PlainAccessRequest()

    def record_id(self, value: str):
        self._plain_access_request.record_id = value
        return self

    def plain_request_fields(self, value: List[str]):
        self._plain_access_request.plain_request_fields = value
        return self

    def build(self):
        return self._plain_access_request
