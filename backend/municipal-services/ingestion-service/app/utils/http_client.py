import os
from abc import ABC, abstractmethod
from typing import Dict, Any
import httpx

from dotenv import load_dotenv
load_dotenv()
time_out = os.getenv("TIME_OUT")


class HttpClientInterface(ABC):
    @abstractmethod
    async def get(self, url: str) -> Dict[str, Any]:
        pass

    @abstractmethod
    async def post(self, url: str, json: Dict[str, Any]) -> Dict[str, Any]:
        pass


class AsyncHttpClient(HttpClientInterface):
    def __init__(self):
        self.timeout = time_out

    async def get(self, url: str) -> Dict[str, Any]:
        """Make an HTTP GET request and return the JSON response"""
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.get(url)
            response.raise_for_status()
            return response.json()

    async def post(self, url: str, json: Dict[str, Any]) -> Dict[str, Any]:
        """Make an HTTP POST request and return the JSON response"""
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.post(url, json=json)
            response.raise_for_status()
            return response.json()