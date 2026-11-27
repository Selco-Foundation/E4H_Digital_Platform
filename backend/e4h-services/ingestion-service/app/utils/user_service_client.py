import logging
from typing import Any, Dict, Optional

import requests

logger = logging.getLogger(__name__)


class UserServiceClient:
    def __init__(self, user_service_url: str):
        self.user_service_url = user_service_url.rstrip("/")

    def get_user_by_token(self, access_token: str) -> Optional[Dict[str, Any]]:
        """The user an access token belongs to, as egov-user reports it (uuid, type, roles, ...).

        Returns None when the token is not valid, so the caller can tell "this is nobody" from
        "the user service is down", which raises.
        """
        url = f"{self.user_service_url}/user/_details"
        try:
            response = requests.post(url, params={"access_token": access_token}, timeout=30)
        except requests.exceptions.RequestException as req_err:
            logger.error("User service token lookup failed: %s", req_err)
            raise
        if response.status_code in (400, 401, 403):
            return None
        response.raise_for_status()
        return response.json()
