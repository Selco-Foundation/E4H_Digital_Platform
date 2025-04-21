import requests
import json
import logging
from app.core.logging import AppLogger

logger = AppLogger().get_logger()

def call_api(method, url, headers=None, data=None, params=None, timeout=10):
    default_headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'User-Agent': 'Vendor Service/1.0'
    }
    if headers:
        headers = {**default_headers, **headers}
    else:
        headers = default_headers
    if isinstance(data, dict):
        data = json.dumps(data)
    max_retries = 3
    retry_delay = 1
    for attempt in range(max_retries):
        try:
            response = requests.request(
                method=method,
                url=url,
                headers=headers,
                data=data,
                params=params,
                timeout=timeout
            )

            logging.info(f"Request to {url} (Attempt {attempt + 1}) - Status Code: {response.status_code}")
            if 200 <= response.status_code < 300:
                try:
                    response_json = response.json()
                    return True, response_json, response.status_code
                except json.JSONDecodeError:
                    return True, response.text, response.status_code
            else:
                if response.status_code in [429, 503, 504]:
                    logging.warning(f"Received {response.status_code} from {url}. Retrying in {retry_delay} seconds...")
                    import time
                    time.sleep(retry_delay)
                    retry_delay *= 2
                else:
                    logging.error(f"Error calling {url}. Status code: {response.status_code}, Response: {response.text}")
                    return False, response.text, response.status_code

        except requests.exceptions.RequestException as e:
            logging.error(f"Exception calling {url} (Attempt {attempt + 1}): {e}")
            if attempt < max_retries - 1:
                logging.warning(f"Retrying in {retry_delay} seconds...")
                import time
                time.sleep(retry_delay)
                retry_delay *= 2
            else:
                return False, None, None
    return False, None, None

