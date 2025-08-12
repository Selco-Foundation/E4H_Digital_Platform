from dataclasses import dataclass
from typing import Optional, Dict, Any


@dataclass
class Vendor:
    country_boundary_code: str
    vendor_name: str
    vendor_type: str
    vendor_subtype: Optional[str]
    identifier_type: str
    identifier_value: str
    hq_address: str
    pincode: str
    poc_phone: str
    poc_name: str

    def __str__(self):
        return f"Vendor(name={self.vendor_name}, country={self.country_boundary_code})"

    def to_dict(self) -> Dict[str, Any]:
        return {
            "country_boundary_code": self.country_boundary_code,
            "vendor_name": self.vendor_name,
            "vendor_type": self.vendor_type,
            "vendor_subtype": self.vendor_subtype,
            "identifier_type": self.identifier_type,
            "identifier_value": self.identifier_value,
            "hq_address": self.hq_address,
            "pincode": self.pincode,
            "poc_phone": self.poc_phone,
            "poc_name": self.poc_name,
        }

