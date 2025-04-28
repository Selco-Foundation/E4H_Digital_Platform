from typing import Optional
from dataclasses import dataclass, field

@dataclass
class Role:
    id: Optional[int] = None
    name: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 128}}})
    code: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 50}}})
    tenantId: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 256}}})

    @staticmethod
    def builder():
        return RoleBuilder()

    def __eq__(self, other):
        if isinstance(other, Role):
            return (self.id == other.id and
                    self.name == other.name and
                    self.code == other.code and
                    self.tenantId == other.tenantId)
        return NotImplemented

    def __hash__(self):
        return hash((self.id, self.name, self.code, self.tenantId))

    def __str__(self):
        return f"Role(id={self.id}, name='{self.name}', code='{self.code}', tenantId='{self.tenantId}')"

@dataclass
class RoleBuilder:
    id: Optional[int] = None
    name: Optional[str] = None
    code: Optional[str] = None
    tenantId: Optional[str] = None

    def id(self, id: int) -> "RoleBuilder":
        self.id = id
        return self

    def name(self, name: str) -> "RoleBuilder":
        self.name = name
        return self

    def code(self, code: str) -> "RoleBuilder":
        self.code = code
        return self

    def tenantId(self, tenantId: str) -> "RoleBuilder":
        self.tenantId = tenantId
        return self

    def build(self) -> Role:
        return Role(id=self.id, name=self.name, code=self.code, tenantId=self.tenantId)

    def __str__(self):
        return f"RoleBuilder(id={self.id}, name='{self.name}', code='{self.code}', tenantId='{self.tenantId}')"