import json
import os
from datetime import datetime
from typing import Any, Dict, Optional

import requests
from dotenv import load_dotenv
from fastapi import APIRouter, BackgroundTasks, Form, Header, HTTPException
from fastapi.responses import FileResponse

from app.core.logging import AppLogger
from app.ingest.facility_export import (
    boundary_codes_of,
    build_export_criteria,
    export_rows,
    names_by_code,
    write_export_workbook,
)
from app.ingest.facility_template_service import LOCALIZATION_LOOKUP_BATCH
from app.utils.convertor import request_info_from_json
from app.utils.facility_service_client import FacilityServiceClient
from app.utils.file_utils import cleanup_temp_file, create_temp_file
from app.utils.localization_service_client import LocalizationServiceClient
from app.utils.mdms_client import MDMSClient

router = APIRouter()
logger = AppLogger().get_logger()

load_dotenv()
mdms_url = os.getenv("MDMS_URL")
facility_service_url = os.getenv("FACILITY_SERVICE_URL")
localization_service_url = os.getenv("LOCALIZATION_SERVICE_URL")

XLSX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"


def _parse_json_form(value: str, field: str) -> Any:
    try:
        return json.loads(value) if value and value.strip() else {}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=f"{field} is not valid JSON: {e}")


def _facility_service_error(response: requests.Response) -> HTTPException:
    """facility-service's own refusal, passed back with its status and messages. A 4xx (no role
    declared, or a role the caller doesn't hold) is the caller's; a 5xx is reported as a bad
    gateway, since it is facility-service that failed."""
    try:
        errors = (response.json() or {}).get("Errors") or []
        detail = " ".join(error.get("message") or error.get("code") or "" for error in errors).strip()
    except ValueError:
        detail = ""
    detail = detail or f"Facility search failed with HTTP {response.status_code}"
    status = response.status_code if 400 <= response.status_code < 500 else 502
    return HTTPException(status_code=status, detail=detail)


def _boundary_names(codes, tenant_id: str) -> Dict[str, str]:
    """`{Boundary_<code>: name}` from rainmaker-in, batched. A failed batch only leaves its own
    names as codes, as the Bulk Add template does."""
    names: Dict[str, str] = {}
    if not localization_service_url or not codes:
        return names
    client = LocalizationServiceClient(localization_service_url)
    keys = [f"Boundary_{code}" for code in codes]
    for start in range(0, len(keys), LOCALIZATION_LOOKUP_BATCH):
        try:
            response = client.search_messages(tenant_id=tenant_id, locale="en_IN", module="rainmaker-in",
                                              codes=keys[start:start + LOCALIZATION_LOOKUP_BATCH])
            for message in response.get("messages", []):
                if message.get("code") and message.get("message"):
                    names[message["code"]] = message["message"]
        except Exception as e:
            logger.error(f"Facility export: boundary localization lookup failed: {e}")
    return names


def _mdms_names(request_info, schema_code: str) -> Dict[str, str]:
    """Names for an MDMS master's codes; on failure the export keeps the codes. Through
    fetch_active_mdms_entries, which sends a limit: without one MDMS v2 returns only 10 records,
    and there are more facility types than that."""
    try:
        entries = MDMSClient(mdms_url).fetch_active_mdms_entries(request_info, schema_code)
        return names_by_code([entry.data.model_dump() for entry in entries])
    except Exception as e:
        logger.error(f"Facility export: could not load {schema_code}: {e}")
        return {}


@router.post('/facilities',
             summary="Download the facilities the caller can see, as Excel",
             response_description="An .xlsx of every facility matching the search and filters")
async def export_facilities(
        background_tasks: BackgroundTasks,
        request_info: str = Form(...),
        filters: str = Form(default=""),
        x_acting_role: Optional[str] = Header(default=None, alias="x-acting-role"),
):
    """View Facilities' Download.

    `filters` is a JSON object: `searchText`, and the `state`, `district`, `block` and
    `boundaryCodes` code lists the page is filtering by. The search runs as the caller with
    their `x-acting-role`, so the file holds exactly the facilities they can see.
    """
    request_info_payload = _parse_json_form(request_info, "request_info")
    if not isinstance(request_info_payload, dict) or not request_info_payload.get("authToken"):
        raise HTTPException(status_code=400, detail="request_info with an authToken is required")
    filter_values = _parse_json_form(filters, "filters")
    if not isinstance(filter_values, dict):
        raise HTTPException(status_code=400, detail="filters must be a JSON object")
    if not facility_service_url:
        raise HTTPException(status_code=500, detail="FACILITY_SERVICE_URL is not configured")

    request_info_model = request_info_from_json(request_info_payload)
    user_info = request_info_payload.get("userInfo") or {}
    tenant_id = user_info.get("tenantId") or "in"
    criteria = build_export_criteria(tenant_id, filter_values)

    try:
        response = FacilityServiceClient(facility_service_url).search_as_caller(
            request_info_payload, x_acting_role, criteria)
    except requests.exceptions.RequestException as e:
        logger.error(f"Facility export: facility search unreachable: {e}")
        raise HTTPException(status_code=502, detail=f"Facility search failed: {e}")
    if response.status_code != 200:
        logger.warning(f"Facility export: facility search refused with HTTP {response.status_code}")
        raise _facility_service_error(response)
    facilities = (response.json() or {}).get("facilities") or []

    boundary_names = _boundary_names(boundary_codes_of(facilities), tenant_id)
    rows = export_rows(
        facilities,
        lambda code: boundary_names.get(f"Boundary_{code}", code),
        _mdms_names(request_info_model, "facility.FacilityCategory"),
        _mdms_names(request_info_model, "facility.FacilityType"),
    )

    output_path = create_temp_file(suffix=".xlsx")
    try:
        write_export_workbook(output_path, rows)
    except Exception as e:
        cleanup_temp_file(output_path)
        logger.error(f"Facility export: writing the workbook failed: {e}")
        raise HTTPException(status_code=500, detail=f"Could not build the export: {e}")
    background_tasks.add_task(cleanup_temp_file, output_path)
    logger.info(f"Facility export: {len(rows)} facilities as {x_acting_role or 'no acting role'}")
    return FileResponse(
        path=output_path,
        filename=f"facilities_{datetime.now().strftime('%Y-%m-%d')}.xlsx",
        media_type=XLSX_MEDIA_TYPE,
        headers={"X-Total-Count": str(len(rows))},
    )
