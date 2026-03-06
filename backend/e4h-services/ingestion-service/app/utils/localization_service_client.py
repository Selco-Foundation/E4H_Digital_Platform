import os
from typing import Dict, Any, List

import requests
from requests.exceptions import HTTPError, ConnectionError, Timeout, RequestException

from app.schemas.request_info import RequestInfo
from app.core.logging import AppLogger

from dotenv import load_dotenv
load_dotenv()
time_out = int(os.getenv("TIME_OUT", "60"))

logger = AppLogger().get_logger()


class LocalizationServiceClient:
    def __init__(self, base_url: str):
        self.base_url = (base_url or "").rstrip("/")

    def upsert_messages(
        self,
        request_info: RequestInfo,
        tenant_id: str,
        messages: List[Dict[str, Any]],
    ) -> Dict[str, Any]:
        """
        Upsert localization messages.
        messages: list of {"code": str, "message": str, "module": str, "locale": str}
        """
        if not self.base_url:
            logger.warning("LOCALIZATION_SERVICE_URL not set; skipping localization upsert")
            return {}
        if not messages:
            return {}

        url = f"{self.base_url}/localization/messages/v1/_upsert"
        payload = {
            "RequestInfo": request_info.model_dump(by_alias=True, exclude_none=True),
            "tenantId": tenant_id,
            "messages": messages,
        }
        try:
            response = requests.post(url, json=payload, timeout=time_out)
            response.raise_for_status()
            return response.json() if response.content else {}
        except HTTPError as e:
            logger.error(f"HTTP error during localization upsert: {e}")
            raise
        except ConnectionError as e:
            logger.error(f"Connection error during localization upsert: {e}")
            raise
        except Timeout as e:
            logger.error(f"Timeout error during localization upsert: {e}")
            raise
        except RequestException as e:
            logger.error(f"Unexpected request error during localization upsert: {e}")
            raise
