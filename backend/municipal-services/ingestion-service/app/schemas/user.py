from typing import List, Optional
from dataclasses import dataclass, field

from app.schemas.role import Role


@dataclass
class User:
    id: Optional[int] = None
    userName: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 180}}})
    name: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 250}}})
    type: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 50}}})
    mobileNumber: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 150}}})
    emailId: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 300}}})
    roles: Optional[List[Role]] = field(default_factory=list)
    tenantId: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 256}}})
    uuid: Optional[str] = field(default=None, metadata={"constraints": {"Size": {"max": 36}}})

    @staticmethod
    def builder():
        return UserBuilder()

    def __str__(self):
        return (f"User(id={self.id}, userName='{self.userName}', name='{self.name}', type='{self.type}', "
                f"mobileNumber='{self.mobileNumber}', emailId='{self.emailId}', roles={self.roles}, "
                f"tenantId='{self.tenantId}', uuid='{self.uuid}')")

    def __eq__(self, other):
        if isinstance(other, User):
            return (self.id == other.id and
                    self.userName == other.userName and
                    self.name == other.name and
                    self.type == other.type and
                    self.mobileNumber == other.mobileNumber and
                    self.emailId == other.emailId and
                    self.roles == other.roles and
                    self.tenantId == other.tenantId and
                    self.uuid == other.uuid)
        return NotImplemented

    def __hash__(self):
        return hash((self.id, self.userName, self.name, self.type, self.mobileNumber, self.emailId, tuple(self.roles) if self.roles else None, self.tenantId, self.uuid))

@dataclass
class UserBuilder:
    id: Optional[int] = None
    userName: Optional[str] = None
    name: Optional[str] = None
    type: Optional[str] = None
    mobileNumber: Optional[str] = None
    emailId: Optional[str] = None
    roles: Optional[List[Role]] = field(default_factory=list)
    tenantId: Optional[str] = None
    uuid: Optional[str] = None

    def id(self, id: int) -> "UserBuilder":
        self.id = id
        return self

    def userName(self, userName: str) -> "UserBuilder":
        self.userName = userName
        return self

    def name(self, name: str) -> "UserBuilder":
        self.name = name
        return self

    def type(self, type: str) -> "UserBuilder":
        self.type = type
        return self

    def mobileNumber(self, mobileNumber: str) -> "UserBuilder":
        self.mobileNumber = mobileNumber
        return self

    def emailId(self, emailId: str) -> "UserBuilder":
        self.emailId = emailId
        return self

    def roles(self, roles: List[Role]) -> "UserBuilder":
        self.roles = roles
        return self

    def tenantId(self, tenantId: str) -> "UserBuilder":
        self.tenantId = tenantId
        return self

    def uuid(self, uuid: str) -> "UserBuilder":
        self.uuid = uuid
        return self

    def build(self) -> User:
        return User(
            id=self.id,
            userName=self.userName,
            name=self.name,
            type=self.type,
            mobileNumber=self.mobileNumber,
            emailId=self.emailId,
            roles=self.roles,
            tenantId=self.tenantId,
            uuid=self.uuid
        )

    def __str__(self):
        return (f"UserBuilder(id={self.id}, userName='{self.userName}', name='{self.name}', type='{self.type}', "
                f"mobileNumber='{self.mobileNumber}', emailId='{self.emailId}', roles={self.roles}, "
                f"tenantId='{self.tenantId}', uuid='{self.uuid}')")