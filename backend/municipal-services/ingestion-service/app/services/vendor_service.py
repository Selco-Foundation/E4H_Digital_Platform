from typing import List, Dict, Any

from app.core.logging import AppLogger
from app.schemas.vendor import Vendor
from app.utils.api_requests import call_api

logger = AppLogger().get_logger()

def create_vendor_in_vendor_service(vendors: List[Vendor]) -> Dict[str, Any] | None:
    vendor_service_url = "http://vendor-service:8081/vendors"
    headers = {'Content-Type': 'application/json'}
    vendor_data_list = [vendor.to_dict() for vendor in vendors]
    data = {"vendors": vendor_data_list}

    success, response_data, status_code = call_api(
        method='POST',
        url=vendor_service_url,
        headers=headers,
        data=data
    )

    if success:
        logger.info(f"Vendors created successfully. Response: {response_data}")
        return response_data
    else:
        logger.error(f"Failed to create vendors. Status code: {status_code}, Response: {response_data}")
        return None
