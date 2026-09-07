import os
from typing import Optional, Tuple

import requests
from requests.exceptions import HTTPError, ConnectionError, Timeout, RequestException

from app.core.logging import AppLogger

from dotenv import load_dotenv
load_dotenv()
time_out = int(os.getenv("TIME_OUT", "60"))

logger = AppLogger().get_logger()


class FileStoreServiceClient:
    def __init__(self, filestore_service_url: str):
        self.filestore_service_url = filestore_service_url

    def download_file(self, tenant_id: str, file_store_id: str) -> Tuple[bytes, Optional[str]]:
        """
        Download the raw bytes of a file from the filestore service.
        Returns (file_bytes, content_type).
        """
        url = f"{self.filestore_service_url}/filestore/v1/files/id"
        params = {"tenantId": tenant_id, "fileStoreId": file_store_id}
        logger.trace(f"Downloading file from filestore: fileStoreId={file_store_id}, tenantId={tenant_id}")
        try:
            response = requests.get(url, params=params, timeout=time_out)
            response.raise_for_status()
            logger.debug(f"Downloaded file fileStoreId={file_store_id}, size={len(response.content)} bytes")
            return response.content, response.headers.get("Content-Type")
        except HTTPError as e:
            logger.error(f"HTTP error downloading file fileStoreId={file_store_id}: {e}", exc_info=True)
            raise
        except ConnectionError as e:
            logger.error(f"Connection error downloading file fileStoreId={file_store_id}: {e}", exc_info=True)
            raise
        except Timeout as e:
            logger.error(f"Timeout downloading file fileStoreId={file_store_id}: {e}", exc_info=True)
            raise
        except RequestException as e:
            logger.error(f"Request error downloading file fileStoreId={file_store_id}: {e}", exc_info=True)
            raise

    def upload_file(self, file_bytes: bytes, file_name: str, tenant_id: str, module: str,
                     content_type: str = "application/pdf", tag: Optional[str] = None) -> str:
        """
        Upload a file to the filestore service and return the generated fileStoreId.
        """
        url = f"{self.filestore_service_url}/filestore/v1/files"
        data = {"tenantId": tenant_id, "module": module}
        if tag:
            data["tag"] = tag
        files = {"file": (file_name, file_bytes, content_type)}
        logger.trace(f"Uploading file to filestore: fileName={file_name}, tenantId={tenant_id}, module={module}")
        try:
            response = requests.post(url, data=data, files=files, timeout=time_out)
            response.raise_for_status()
            response_body = response.json()
            uploaded_files = response_body.get("files") or []
            if not uploaded_files:
                raise ValueError(f"Filestore upload returned no files: {response_body}")
            file_store_id = uploaded_files[0].get("fileStoreId")
            logger.info(f"Uploaded file to filestore successfully: fileStoreId={file_store_id}")
            return file_store_id
        except HTTPError as e:
            logger.error(f"HTTP error uploading file to filestore: {e}", exc_info=True)
            raise
        except ConnectionError as e:
            logger.error(f"Connection error uploading file to filestore: {e}", exc_info=True)
            raise
        except Timeout as e:
            logger.error(f"Timeout uploading file to filestore: {e}", exc_info=True)
            raise
        except RequestException as e:
            logger.error(f"Request error uploading file to filestore: {e}", exc_info=True)
            raise
