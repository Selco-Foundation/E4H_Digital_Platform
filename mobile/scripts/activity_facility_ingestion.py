#!/usr/bin/env python3
"""Ingest activity-facility asset submission data from the Excel template.

The script mirrors the mobile background submission flow:
1. validate local installation image counts from MDMS,
2. upload local files from the folder convention,
3. submit one merged BOM JSON per activity facility,
4. generate the BOM PDF and attach it as INSTALLATION_REPORT_BOM,
5. create or update assets,
6. finalize the activity workflow with the default SUBMIT_REPORT_B action.
"""

from __future__ import annotations

import argparse
import json
import mimetypes
import os
import time
import uuid
import zipfile
from dataclasses import dataclass
from pathlib import Path
from typing import Any
from urllib import error, parse, request
from xml.etree import ElementTree as ET


DEFAULT_WORKBOOK = "activity_facility_ingestion_template.xlsx"
DEFAULT_ENV_FILE = "scripts/.env"
DEFAULT_FILES_ROOT = "scripts/ActivityFacilityIngestion"
DEFAULT_AUTH_TOKEN = "0bfd30fc-3d3e-4ecb-9676-6bc0191047c3"
DEFAULT_MODULE = "Incident"
DEFAULT_WORKFLOW_ACTION = "SUBMIT_REPORT_B"

NS = {
    "main": "http://schemas.openxmlformats.org/spreadsheetml/2006/main",
    "rel": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
    "pkg": "http://schemas.openxmlformats.org/package/2006/relationships",
}


class IngestionError(RuntimeError):
    pass


class ApiRequestError(IngestionError):
    def __init__(
        self,
        *,
        url: str,
        status_code: int,
        detail: str,
        parsed_body: Any,
    ) -> None:
        super().__init__(f"{url} failed {status_code}: {detail}")
        self.url = url
        self.status_code = status_code
        self.detail = detail
        self.parsed_body = parsed_body


@dataclass(frozen=True)
class ActivityFacilityRow:
    facility_id: str
    latitude: str
    longitude: str


@dataclass(frozen=True)
class ActivityFacility:
    activity_facility_id: str
    facility_id: str
    assign_user_uuid: str
    latitude: str
    longitude: str
    system: str
    solution_design_code: str


@dataclass(frozen=True)
class LoginConfig:
    username: str
    password: str
    user_type: str
    basic_auth: str


@dataclass(frozen=True)
class LoginResult:
    assign_user_uuid: str
    user_info: dict[str, Any]


@dataclass(frozen=True)
class AssetTypeMdms:
    rows: list[dict[str, Any]]
    dry_run: bool


@dataclass(frozen=True)
class InstallationImageMdms:
    requirements: dict[str, "InstallationImageRequirement"]
    dry_run: bool


@dataclass(frozen=True)
class InstallationImageRequirement:
    code: str
    description: str
    required_count: int


INSTALLATION_IMAGE_REQUIREMENTS: tuple[InstallationImageRequirement, ...] = (
    InstallationImageRequirement(
        "1",
        "Clear image of solar panels with Module mounting structure from a range "
        "in which gives better visibility (Please capture image with standard marking)",
        2,
    ),
    InstallationImageRequirement(
        "2",
        "Clear image of batteries from a range in which gives better visibility "
        "including the water level (Please capture image with standard marking)",
        1,
    ),
    InstallationImageRequirement(
        "3",
        "Clear image of inverter from a range in which gives better visibility "
        "(Front and back) (Please capture image with standard marking)",
        2,
    ),
    InstallationImageRequirement(
        "4",
        "Clear image of the inverter isolator switches & Load MCB",
        1,
    ),
    InstallationImageRequirement(
        "5",
        "Clear image of cable routing for the entire system "
        "(Please capture image with standard marking)",
        3,
    ),
    InstallationImageRequirement("6", "Clear image of AJB", 1),
    InstallationImageRequirement("7", "Clear image of GIPB", 1),
    InstallationImageRequirement("8", "Clear image of Lightning Arrestor", 1),
    InstallationImageRequirement("9", "Clear image of Earthing pits", 1),
    InstallationImageRequirement("10", "Clear image of Changeover Switch", 1),
    InstallationImageRequirement("11", "Clear image of DO's and Don'ts Poster", 1),
    InstallationImageRequirement(
        "12",
        "Clear image of Foam Plaques (SLD, High Voltage, PASS, No Fire, Danger, "
        "Risk of Electric Shock)",
        2,
    ),
    InstallationImageRequirement("13", "Clear image of Metal Plaque", 1),
    InstallationImageRequirement("14", "Clear image of Outdoor Light", 1),
    InstallationImageRequirement("15", "RMS Site Photo with Geotagging", 1),
    InstallationImageRequirement("16", "Clear image of the Health Centre (Long Shot)", 1),
    InstallationImageRequirement("17", "Clear image of Health staff with Solar system", 1),
)


def installation_image_label(code: str, description: str) -> str:
    return f"{code} - {description}"


def static_installation_image_requirements() -> dict[str, InstallationImageRequirement]:
    return {requirement.code: requirement for requirement in INSTALLATION_IMAGE_REQUIREMENTS}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Submit activity-facility ingestion workbook data.",
    )
    parser.add_argument("--workbook", default=DEFAULT_WORKBOOK)
    parser.add_argument("--files-root", default=DEFAULT_FILES_ROOT)
    parser.add_argument("--env-file", default=DEFAULT_ENV_FILE)
    parser.add_argument("--base-url", default=None)
    parser.add_argument("--tenant-id", default=None)
    parser.add_argument("--auth-token", default=DEFAULT_AUTH_TOKEN)
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--timeout", type=int, default=120)
    return parser.parse_args()


def parse_env(path: str) -> dict[str, str]:
    env_path = Path(path)
    if not env_path.exists():
        return {}

    values: dict[str, str] = {}
    for raw in env_path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        value = value.strip().strip('"').strip("'")
        values[key.strip()] = value
    return values


def required_env(values: dict[str, str], key: str, env_file: str) -> str:
    value = values.get(key, "").strip()
    if not value:
        raise IngestionError(f"{key} missing. Add it to {env_file}.")
    return value


def login_config_from_env(values: dict[str, str], env_file: str) -> LoginConfig:
    return LoginConfig(
        username=required_env(values, "LOGIN_USERNAME", env_file),
        password=required_env(values, "LOGIN_PASSWORD", env_file),
        user_type=required_env(values, "LOGIN_USER_TYPE", env_file),
        basic_auth=required_env(values, "LOGIN_BASIC_AUTH", env_file),
    )


def dry_run_user_info(*, username: str, user_type: str, tenant_id: str) -> dict[str, Any]:
    return {
        "id": 0,
        "uuid": f"DRY_USER_UUID::{username}",
        "userName": username,
        "name": "Dry Run User",
        "mobileNumber": username,
        "emailId": "",
        "locale": "en_IN",
        "type": user_type,
        "roles": [],
        "active": True,
        "tenantId": tenant_id,
        "permanentCity": "All",
    }


def parse_login_response(response: Any) -> tuple[str, LoginResult]:
    if not isinstance(response, dict):
        raise IngestionError(f"Login response is not an object: {response}")

    access_token = str(response.get("access_token") or "").strip()
    if not access_token:
        raise IngestionError(f"Login response missing access_token: {response}")

    user_info = response.get("UserRequest")
    if not isinstance(user_info, dict):
        raise IngestionError(f"Login response missing UserRequest object: {response}")

    user_uuid = str(user_info.get("uuid") or "").strip()
    if not user_uuid:
        raise IngestionError(f"Login response missing UserRequest.uuid: {response}")

    return access_token, LoginResult(assign_user_uuid=user_uuid, user_info=user_info)


def load_xlsx(path: str) -> dict[str, list[dict[str, str]]]:
    workbook_path = Path(path)
    if not workbook_path.exists():
        raise IngestionError(f"Workbook not found: {workbook_path}")

    with zipfile.ZipFile(workbook_path) as archive:
        shared_strings = read_shared_strings(archive)
        workbook = ET.fromstring(archive.read("xl/workbook.xml"))
        rels = ET.fromstring(archive.read("xl/_rels/workbook.xml.rels"))
        rel_map = {rel.attrib["Id"]: rel.attrib["Target"] for rel in rels}

        sheets: dict[str, list[dict[str, str]]] = {}
        for sheet in workbook.find("main:sheets", NS) or []:
            name = sheet.attrib["name"]
            if name.startswith("_"):
                continue
            rel_id = sheet.attrib[f"{{{NS['rel']}}}id"]
            target = rel_map[rel_id]
            xml_path = "xl/" + target if not target.startswith("/") else target[1:]
            rows = read_sheet_rows(archive.read(xml_path), shared_strings)
            sheets[name] = rows_to_dicts(rows)
        return sheets


def read_shared_strings(archive: zipfile.ZipFile) -> list[str]:
    if "xl/sharedStrings.xml" not in archive.namelist():
        return []
    root = ET.fromstring(archive.read("xl/sharedStrings.xml"))
    strings: list[str] = []
    for si in root.findall("main:si", NS):
        text = "".join(t.text or "" for t in si.iter(f"{{{NS['main']}}}t"))
        strings.append(text)
    return strings


def read_sheet_rows(xml: bytes, shared_strings: list[str]) -> list[list[str]]:
    root = ET.fromstring(xml)
    rows: list[list[str]] = []
    for row in root.findall("main:sheetData/main:row", NS):
        current: list[str] = []
        for cell in row.findall("main:c", NS):
            ref = cell.attrib.get("r", "")
            col_index = column_index("".join(ch for ch in ref if ch.isalpha()))
            while len(current) < col_index:
                current.append("")
            current.append(cell_value(cell, shared_strings))
        rows.append(current)
    return rows


def column_index(column: str) -> int:
    total = 0
    for char in column:
        total = total * 26 + ord(char.upper()) - ord("A") + 1
    return max(total - 1, 0)


def cell_value(cell: ET.Element, shared_strings: list[str]) -> str:
    cell_type = cell.attrib.get("t")
    if cell_type == "s":
        value = cell.find("main:v", NS)
        if value is None or value.text is None:
            return ""
        return shared_strings[int(value.text)]
    if cell_type == "inlineStr":
        inline = cell.find("main:is", NS)
        if inline is None:
            return ""
        return "".join(t.text or "" for t in inline.iter(f"{{{NS['main']}}}t"))
    value = cell.find("main:v", NS)
    return "" if value is None or value.text is None else value.text


def rows_to_dicts(rows: list[list[str]]) -> list[dict[str, str]]:
    if not rows:
        return []
    headers = [normalize_header(v) for v in rows[0]]
    out: list[dict[str, str]] = []
    for row in rows[1:]:
        item = {
            headers[i]: row[i].strip() if i < len(row) else ""
            for i in range(len(headers))
            if headers[i]
        }
        if any(value for value in item.values()):
            out.append(item)
    return out


def normalize_header(value: str) -> str:
    return value.strip().lower()


class ApiClient:
    def __init__(
        self,
        *,
        base_url: str,
        tenant_id: str,
        auth_token: str,
        login_config: LoginConfig,
        dry_run: bool,
        timeout: int,
    ) -> None:
        self.base_url = base_url.rstrip("/") + "/"
        self.tenant_id = tenant_id
        self.auth_token = auth_token
        self.login_config = login_config
        self.user_info: dict[str, Any] | None = None
        self.dry_run = dry_run
        self.timeout = timeout

    def post_json(self, path: str, body: dict[str, Any]) -> Any:
        payload = dict(body)
        payload["RequestInfo"] = self.request_info(path)
        url = parse.urljoin(self.base_url, path.lstrip("/"))
        if self.dry_run:
            print_json("POST " + url, payload)
            return {}

        data = json.dumps(payload).encode("utf-8")
        req = request.Request(
            url,
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        return self._send(req)

    def login(self) -> LoginResult:
        form = {
            "grant_type": "password",
            "username": self.login_config.username,
            "password": self.login_config.password,
            "userType": self.login_config.user_type,
            "tenantId": self.tenant_id,
        }
        url = parse.urljoin(self.base_url, "user/oauth/token")
        if self.dry_run:
            print("\nPOST " + url)
            print_json("FORM", form)
            user_info = dry_run_user_info(
                username=self.login_config.username,
                user_type=self.login_config.user_type,
                tenant_id=self.tenant_id,
            )
            self.user_info = user_info
            return LoginResult(assign_user_uuid=str(user_info["uuid"]), user_info=user_info)

        data = parse.urlencode(form).encode("utf-8")
        req = request.Request(
            url,
            data=data,
            headers={
                "Authorization": self.login_config.basic_auth,
                "Content-Type": "application/x-www-form-urlencoded",
            },
            method="POST",
        )
        response = self._send(req)
        access_token, login_result = parse_login_response(response)

        self.auth_token = access_token
        self.user_info = login_result.user_info
        return login_result

    def upload_file(self, file_path: str) -> str:
        file = Path(file_path).expanduser()
        if self.dry_run:
            print(f"UPLOAD {file} -> DRY_FILESTORE::{file.name}")
            return f"DRY_FILESTORE::{file.name}"
        if not file.exists():
            raise IngestionError(f"File not found for upload: {file}")

        boundary = "----codex-ingestion-" + uuid.uuid4().hex
        content_type = mimetypes.guess_type(file.name)[0] or "application/octet-stream"
        body = build_multipart(
            boundary,
            fields={"tenantId": self.tenant_id, "module": DEFAULT_MODULE},
            files={"file": (file.name, content_type, file.read_bytes())},
        )
        url = parse.urljoin(self.base_url, "filestore/v1/files")
        req = request.Request(
            url,
            data=body,
            headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
            method="POST",
        )
        response = self._send(req)
        files = response.get("files") if isinstance(response, dict) else None
        if not files:
            raise IngestionError(f"Filestore response missing files array: {response}")
        file_store_id = files[0].get("fileStoreId")
        if not file_store_id:
            raise IngestionError(f"Filestore response missing fileStoreId: {response}")
        return str(file_store_id)

    def request_info(self, path: str) -> dict[str, Any]:
        now = int(time.time() * 1000)
        request_info = {
            "apiId": "project-api",
            "ver": ".01",
            "ts": now,
            "action": path.rstrip("/").split("/")[-1],
            "did": "1",
            "key": "1",
            "msgId": f"{now}|en_IN",
            "authToken": self.auth_token,
            "plainAccessRequest": {},
        }
        if self.user_info is not None:
            request_info["userInfo"] = self.user_info
        return request_info

    def _send(self, req: request.Request) -> Any:
        try:
            with request.urlopen(req, timeout=self.timeout) as resp:
                raw = resp.read().decode("utf-8")
                if not raw:
                    return {}
                try:
                    return json.loads(raw)
                except json.JSONDecodeError:
                    return raw
        except error.HTTPError as exc:
            detail = exc.read().decode("utf-8", errors="replace")
            try:
                parsed_body: Any = json.loads(detail)
            except json.JSONDecodeError:
                parsed_body = None
            raise ApiRequestError(
                url=req.full_url,
                status_code=exc.code,
                detail=detail,
                parsed_body=parsed_body,
            ) from exc
        except error.URLError as exc:
            raise IngestionError(f"{req.full_url} failed: {exc}") from exc


def build_multipart(
    boundary: str,
    *,
    fields: dict[str, str],
    files: dict[str, tuple[str, str, bytes]],
) -> bytes:
    chunks: list[bytes] = []
    marker = f"--{boundary}\r\n".encode("utf-8")
    for name, value in fields.items():
        chunks.extend(
            [
                marker,
                f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode("utf-8"),
                value.encode("utf-8"),
                b"\r\n",
            ]
        )
    for name, (filename, content_type, data) in files.items():
        chunks.extend(
            [
                marker,
                (
                    f'Content-Disposition: form-data; name="{name}"; '
                    f'filename="{filename}"\r\n'
                ).encode("utf-8"),
                f"Content-Type: {content_type}\r\n\r\n".encode("utf-8"),
                data,
                b"\r\n",
            ]
        )
    chunks.append(f"--{boundary}--\r\n".encode("utf-8"))
    return b"".join(chunks)


def print_json(title: str, payload: dict[str, Any]) -> None:
    print("\n" + title)
    print(json.dumps(payload, indent=2, sort_keys=True))


def required(row: dict[str, str], key: str, context: str) -> str:
    value = row.get(key, "").strip()
    if not value:
        raise IngestionError(f"Missing required {key} in {context}")
    return value


def maybe_number(value: str) -> int | float | None:
    value = value.strip()
    if not value:
        return None
    try:
        number = float(value)
    except ValueError:
        return None
    return int(number) if number.is_integer() else number


def group_by(rows: list[dict[str, str]], key: str) -> dict[str, list[dict[str, str]]]:
    grouped: dict[str, list[dict[str, str]]] = {}
    for row in rows:
        grouped.setdefault(row.get(key, "").strip(), []).append(row)
    return grouped


def parse_activity_facilities(rows: list[dict[str, str]]) -> dict[str, ActivityFacilityRow]:
    activities: dict[str, ActivityFacilityRow] = {}
    for row in rows:
        facility_id = required(row, "facility_id", "ActivityFacilities")
        if facility_id in activities:
            raise IngestionError(f"Duplicate facility_id in ActivityFacilities: {facility_id}")
        activities[facility_id] = ActivityFacilityRow(
            facility_id=facility_id,
            latitude=row.get("latitude", "").strip(),
            longitude=row.get("longitude", "").strip(),
        )
    return activities


def validate_facility_ids(sheet_name: str, rows: list[dict[str, str]]) -> None:
    for index, row in enumerate(rows, start=2):
        if not row.get("facility_id", "").strip():
            raise IngestionError(f"Missing required facility_id in {sheet_name} row {index}")


def resolve_activity_facility(
    client: ApiClient,
    activity_row: ActivityFacilityRow,
    assign_user_uuid: str,
) -> ActivityFacility:
    facility_id = activity_row.facility_id
    if client.dry_run:
        activity_payload = {
            "ActivityFacility": {
                "facilityIds": [facility_id],
                "tenantId": client.tenant_id,
            }
        }
        client.post_json(
            f"activity/v1/activities/_search?tenantId={parse.quote(client.tenant_id)}&offset=0&limit=10",
            activity_payload,
        )
        mdms_payload = {
            "MdmsCriteria": {
                "tenantId": client.tenant_id,
                "schemaCode": "facility.SolarSolutionDesignType",
                "moduleDetails": [],
            }
        }
        client.post_json("egov-mdms-service/v2/_search", mdms_payload)
        solution_design_code = "UNKNOWN"
        return ActivityFacility(
            activity_facility_id=f"DRY_ACTIVITY_FACILITY::{facility_id}",
            facility_id=facility_id,
            assign_user_uuid=assign_user_uuid,
            latitude=activity_row.latitude,
            longitude=activity_row.longitude,
            system=f"DRY_SYSTEM::{solution_design_code}",
            solution_design_code=solution_design_code,
        )

    activity_response = client.post_json(
        f"activity/v1/activities/_search?tenantId={parse.quote(client.tenant_id)}&offset=0&limit=10",
        {
            "ActivityFacility": {
                "facilityIds": [facility_id],
                "tenantId": client.tenant_id,
            }
        },
    )
    activity_id, solution_design_code = parse_activity_search_response(
        activity_response,
        facility_id,
    )
    system = resolve_system_from_solution_design(client, solution_design_code)
    return ActivityFacility(
        activity_facility_id=activity_id,
        facility_id=facility_id,
        assign_user_uuid=assign_user_uuid,
        latitude=activity_row.latitude,
        longitude=activity_row.longitude,
        system=system,
        solution_design_code=solution_design_code,
    )


def parse_activity_search_response(response: Any, facility_id: str) -> tuple[str, str]:
    if not isinstance(response, dict):
        raise IngestionError(f"Activity search response is not an object: {response}")
    facilities = response.get("facility")
    if not isinstance(facilities, list):
        raise IngestionError(f"Activity search response missing facility array: {response}")

    matches: list[dict[str, Any]] = []
    for item in facilities:
        if not isinstance(item, dict):
            continue
        activity_facility = item.get("activityFacility")
        if not isinstance(activity_facility, dict):
            continue
        response_facility_id = (
            str(activity_facility.get("facilityId") or "")
            or str(nested_get(activity_facility, "facility", "facility_id") or "")
        )
        if response_facility_id == facility_id:
            matches.append(activity_facility)

    if not matches:
        raise IngestionError(f"No activity facility found for facility_id {facility_id}")
    if len(matches) > 1:
        raise IngestionError(f"Multiple activity facilities found for facility_id {facility_id}")

    activity_facility = matches[0]
    activity_id = str(activity_facility.get("id") or "").strip()
    if not activity_id:
        raise IngestionError(f"Activity facility id missing for facility_id {facility_id}")

    solution_design_code = str(
        nested_get(
            activity_facility,
            "facility",
            "facility_details",
            "solar_solution_design_type",
        )
        or nested_get(
            activity_facility,
            "facility",
            "facilityDetails",
            "solar_solution_design_type",
        )
        or ""
    ).strip()
    if not solution_design_code:
        raise IngestionError(
            f"solar_solution_design_type missing for facility_id {facility_id}"
        )
    return activity_id, solution_design_code


def resolve_system_from_solution_design(
    client: ApiClient,
    solution_design_code: str,
) -> str:
    response = client.post_json(
        "egov-mdms-service/v2/_search",
        {
            "MdmsCriteria": {
                "tenantId": client.tenant_id,
                "schemaCode": "facility.SolarSolutionDesignType",
                "moduleDetails": [],
            }
        },
    )
    system = parse_solution_design_system(response, solution_design_code)
    if not system:
        raise IngestionError(
            f"No system_code found in MDMS for solution design {solution_design_code}"
        )
    return system


def parse_solution_design_system(response: Any, solution_design_code: str) -> str:
    if not isinstance(response, dict):
        raise IngestionError(f"MDMS response is not an object: {response}")
    mdms_rows = response.get("mdms")
    if not isinstance(mdms_rows, list):
        raise IngestionError(f"MDMS response missing mdms array: {response}")

    for item in mdms_rows:
        if not isinstance(item, dict):
            continue
        data = item.get("data")
        if not isinstance(data, dict):
            continue
        code = str(data.get("code") or "").strip()
        if code != solution_design_code:
            continue
        return str(data.get("system_code") or data.get("systemCode") or "").strip()
    return ""


def fetch_asset_type_mdms(client: ApiClient) -> AssetTypeMdms:
    payload = {
        "MdmsCriteria": {
            "tenantId": client.tenant_id,
            "schemaCode": "asset-registry.AssetTypeSchema",
            "moduleDetails": [],
        }
    }
    response = client.post_json("egov-mdms-service/v2/_search", payload)
    if client.dry_run:
        return AssetTypeMdms(rows=[], dry_run=True)
    if not isinstance(response, dict):
        raise IngestionError(f"AssetTypeSchema response is not an object: {response}")
    mdms_rows = response.get("mdms")
    if not isinstance(mdms_rows, list):
        raise IngestionError(f"AssetTypeSchema response missing mdms array: {response}")
    return AssetTypeMdms(rows=mdms_rows, dry_run=False)


def fetch_installation_image_mdms(client: ApiClient) -> InstallationImageMdms:
    payload = {
        "MdmsCriteria": {
            "tenantId": client.tenant_id,
            "schemaCode": "common-masters.InstallationImages",
            "moduleDetails": [],
        }
    }
    response = client.post_json("egov-mdms-service/v2/_search", payload)
    if client.dry_run:
        return InstallationImageMdms(
            requirements=static_installation_image_requirements(),
            dry_run=True,
        )
    if not isinstance(response, dict):
        raise IngestionError(f"InstallationImages response is not an object: {response}")
    mdms_rows = response.get("mdms")
    if not isinstance(mdms_rows, list):
        raise IngestionError(f"InstallationImages response missing mdms array: {response}")

    requirements: dict[str, InstallationImageRequirement] = {}
    for row in mdms_rows:
        if not isinstance(row, dict):
            continue
        data = row.get("data")
        if not isinstance(data, dict):
            continue
        images = data.get("InstallationImage")
        if not isinstance(images, list):
            continue
        for image in images:
            if not isinstance(image, dict) or image.get("active") is False:
                continue
            code = str(image.get("code") or "").strip()
            if code:
                description = str(image.get("description") or "").strip()
                required_count = int(maybe_number(str(image.get("required_count") or "")) or 0)
                requirements[code] = InstallationImageRequirement(
                    code=code,
                    description=description,
                    required_count=required_count,
                )

    if not requirements:
        raise IngestionError("InstallationImages MDMS response has no active codes")
    return InstallationImageMdms(requirements=requirements, dry_run=False)


def derive_asset_details_from_mdms(
    mdms: AssetTypeMdms,
    *,
    asset_type: str,
    system: str,
) -> dict[str, Any]:
    if mdms.dry_run:
        details: dict[str, Any] = {
            "totalCapacity": 0,
            "totalCapacityUnit": f"DRY_TOTAL_CAPACITY_UOM::{asset_type}::{system}",
            "totalCapacityUOM": f"DRY_TOTAL_CAPACITY_UOM::{asset_type}::{system}",
        }
        if asset_type in {"battery", "panel"}:
            details["capacityUnit"] = f"DRY_CAPACITY_UOM::{asset_type}::{system}"
        if asset_type in {"battery", "inverter"}:
            details["voltageUnit"] = f"DRY_VOLTAGE_UOM::{asset_type}::{system}"
        if asset_type == "inverter":
            details["invertorCapacityUnit"] = f"DRY_CAPACITY_UOM::{asset_type}::{system}"
        return details

    asset_type_data = find_asset_type_data(mdms.rows, asset_type)
    fields = asset_type_data.get("form_fields")
    if not isinstance(fields, list):
        raise IngestionError(f"Asset type {asset_type} missing form_fields in MDMS")

    total_capacity = required_mdms_option(
        fields,
        key="total_capacity",
        system=system,
        asset_type=asset_type,
    )
    total_capacity_uom = required_mdms_option(
        fields,
        key="total_capacity_uom",
        system=system,
        asset_type=asset_type,
    )

    details: dict[str, Any] = {
        "totalCapacity": maybe_number(total_capacity),
        "totalCapacityUnit": total_capacity_uom,
        "totalCapacityUOM": total_capacity_uom,
    }
    if details["totalCapacity"] is None:
        raise IngestionError(
            f"MDMS total_capacity for {asset_type}/{system} is not numeric: {total_capacity}"
        )

    if asset_type in {"battery", "panel", "inverter"}:
        capacity_uom = required_mdms_option(
            fields,
            key="capacity_uom",
            system=system,
            asset_type=asset_type,
        )
        if asset_type in {"battery", "panel"}:
            details["capacityUnit"] = capacity_uom
        if asset_type == "inverter":
            details["invertorCapacityUnit"] = capacity_uom

    if asset_type in {"battery", "inverter"}:
        details["voltageUnit"] = required_mdms_option(
            fields,
            key="voltage_uom",
            system=system,
            asset_type=asset_type,
        )

    return details


def find_asset_type_data(mdms_rows: list[dict[str, Any]], asset_type: str) -> dict[str, Any]:
    for row in mdms_rows:
        if not isinstance(row, dict):
            continue
        data = row.get("data")
        if not isinstance(data, dict):
            continue
        asset_types = data.get("AssetType")
        if not isinstance(asset_types, list):
            continue
        for item in asset_types:
            if not isinstance(item, dict):
                continue
            code = str(item.get("code") or "").strip().lower()
            if code == asset_type:
                return item
    raise IngestionError(f"Asset type {asset_type} not found in AssetTypeSchema MDMS")


def required_mdms_option(
    fields: list[Any],
    *,
    key: str,
    system: str,
    asset_type: str,
) -> str:
    for field in fields:
        if not isinstance(field, dict):
            continue
        if str(field.get("key") or "").strip() != key:
            continue
        if str(field.get("system") or "").strip() != system:
            continue
        options = field.get("options")
        if isinstance(options, list) and options:
            value = str(options[0]).strip()
            if value:
                return value
    raise IngestionError(f"MDMS option {key} missing for {asset_type}/{system}")


def resolve_installation_image_code(
    value: str,
    mdms: InstallationImageMdms,
    *,
    context: str,
) -> str:
    selected = value.strip()
    if not selected:
        raise IngestionError(f"{context} missing installation_image")

    code = selected.split(" - ", 1)[0].strip()
    if not code:
        raise IngestionError(f"{context} has invalid installation image value: {value}")
    if code not in mdms.requirements:
        raise IngestionError(
            f"{context} installation image code {code} is not active in MDMS"
        )
    return code


def nested_get(data: dict[str, Any], *keys: str) -> Any:
    current: Any = data
    for key in keys:
        if not isinstance(current, dict):
            return None
        current = current.get(key)
    return current


def facility_folder(files_root: Path, facility_id: str) -> Path:
    return files_root / facility_id.replace("/", "_")


def sorted_files(paths: list[Path]) -> list[Path]:
    return sorted(
        [path for path in paths if path.is_file()],
        key=lambda path: str(path).lower(),
    )


def direct_child_files(directory: Path) -> list[Path]:
    if not directory.exists():
        return []
    return sorted_files([path for path in directory.iterdir()])


def direct_child_dirs(directory: Path) -> list[Path]:
    if not directory.exists():
        return []
    return sorted(
        [path for path in directory.iterdir() if path.is_dir()],
        key=lambda path: str(path).lower(),
    )


def discover_asset_files(files_root: Path, activity: ActivityFacility, row: dict[str, str]) -> list[Path]:
    asset_type = required(row, "asset_type", "Assets").lower()
    serial_number = required(row, "serial_number", "Assets")
    base = facility_folder(files_root, activity.facility_id) / "assets" / asset_type
    paths = direct_child_files(base / serial_number)
    paths.extend(sorted_files(list(base.glob(f"{serial_number}.*"))))
    return sorted_files(paths)


def discover_installation_image_files(
    files_root: Path,
    activity: ActivityFacility,
    code: str,
) -> list[Path]:
    base = facility_folder(files_root, activity.facility_id) / "installation_images"
    paths = direct_child_files(base / code)
    paths.extend(sorted_files(list(base.glob(f"{code}.*"))))
    return sorted_files(paths)


def validate_installation_image_counts(
    files_root: Path,
    facility_id: str,
    mdms: InstallationImageMdms,
) -> None:
    base = facility_folder(files_root, facility_id) / "installation_images"
    active_codes = set(mdms.requirements)
    invalid = [
        path.name
        for path in direct_child_dirs(base)
        if path.name not in active_codes
    ]
    if invalid:
        raise IngestionError(
            f"Invalid installation image code folders for {facility_id}: "
            + ", ".join(invalid)
        )

    problems: list[str] = []
    for code, requirement in sorted(
        mdms.requirements.items(),
        key=lambda item: int(item[0]) if item[0].isdigit() else item[0],
    ):
        if requirement.required_count <= 0:
            continue
        actual = len(direct_child_files(base / code))
        if actual != requirement.required_count:
            problems.append(f"{code}: expected {requirement.required_count}, found {actual}")

    if problems:
        raise IngestionError(
            f"Installation image count mismatch for {facility_id}: "
            + "; ".join(problems)
        )

    print(f"Installation image counts validated for {facility_id}")


def upload_filestores(
    client: ApiClient,
    *,
    filestore_id: str,
    file_paths: list[Path],
    context: str,
) -> list[str]:
    if filestore_id.strip():
        return [filestore_id.strip()]
    if not file_paths:
        raise IngestionError(f"No local files found for {context}")
    return [client.upload_file(str(path)) for path in file_paths]


def error_code_from_response(response: Any) -> str:
    try:
        if isinstance(response, ApiRequestError):
            response = response.parsed_body
        if isinstance(response, dict):
            errors = response.get("Errors")
            if isinstance(errors, list) and errors:
                first = errors[0]
                if isinstance(first, dict):
                    return str(first.get("code") or "").strip()
        if isinstance(response, list) and response:
            first = response[0]
            if isinstance(first, dict):
                return str(first.get("code") or "").strip()
    except Exception:
        return ""
    return ""


def parse_asset_search_response(response: Any, serial_number: str) -> dict[str, Any] | None:
    if isinstance(response, list):
        rows = response
    elif isinstance(response, dict):
        rows = (
            response.get("assets")
            or response.get("Assets")
            or response.get("asset")
            or response.get("Asset")
            or []
        )
        if isinstance(rows, dict):
            rows = [rows]
    else:
        return None

    if not isinstance(rows, list):
        return None

    for item in rows:
        if not isinstance(item, dict):
            continue
        if str(item.get("serialNumber") or "").strip() == serial_number:
            return item
    return None


def fetch_asset_by_serial(
    client: ApiClient,
    *,
    activity_facility_id: str,
    serial_number: str,
) -> dict[str, Any] | None:
    serial = serial_number.strip()
    if not serial:
        return None

    response = client.post_json(
        f"asset-registry/v1/asset/_search?tenantId={parse.quote(client.tenant_id)}",
        {
            "criteria": {
                "tenantId": client.tenant_id,
                "activityFacilityID": activity_facility_id,
                "serialNumber": [serial],
            }
        },
    )
    return parse_asset_search_response(response, serial)


def submit_bom(
    client: ApiClient,
    activity: ActivityFacility,
    bom_row: dict[str, str] | None,
) -> tuple[dict[str, Any] | None, str]:
    if not bom_row:
        return None, ""

    raw_json = required(bom_row, "bom_json", f"BOMValues:{activity.facility_id}")
    try:
        bom_data = json.loads(raw_json)
    except json.JSONDecodeError as exc:
        raise IngestionError(
            f"Invalid bom_json for {activity.activity_facility_id}: {exc}"
        ) from exc
    if not isinstance(bom_data, dict):
        raise IngestionError(f"bom_json must be a JSON object for {activity.activity_facility_id}")

    bom_id = bom_row.get("bom_id", "").strip()
    is_update = bool(bom_id)
    bom_payload = {
        "bom": [
            {
                **({"id": bom_id} if is_update else {}),
                "tenantId": client.tenant_id,
                "name": "BOM.SolarSystem",
                "facilityId": activity.facility_id,
                "activityFacilityId": activity.activity_facility_id,
                "assignUser": activity.assign_user_uuid,
                "data": bom_data,
                "isActive": True,
            }
        ],
        "isCascadingProjectDateUpdate": False,
        "apiOperation": "UPDATE" if is_update else "CREATE",
    }
    client.post_json(
        "activity/v1/bom/_update" if is_update else "activity/v1/bom/_create",
        bom_payload,
    )

    pdf_payload = {"system": activity.system, "bom": bom_data}
    response = client.post_json(
        f"activity/v1/bom/_save_pdf?tenantId={parse.quote(client.tenant_id)}",
        pdf_payload,
    )
    filestore_id = ""
    if isinstance(response, dict):
        filestore_id = str(response.get("filestoreId") or "")
    if client.dry_run and not filestore_id:
        filestore_id = f"DRY_BOM_PDF::{activity.activity_facility_id}"
    if not filestore_id:
        raise IngestionError(f"BOM PDF response missing filestoreId: {response}")
    return bom_data, filestore_id


def submit_asset(
    client: ApiClient,
    activity: ActivityFacility,
    row: dict[str, str],
    asset_type_mdms: AssetTypeMdms,
    files_root: Path,
) -> str:
    asset_type = required(row, "asset_type", "Assets").lower()
    asset_id = ""
    serial_number = required(row, "serial_number", "Assets")
    document_filestores = upload_filestores(
        client,
        filestore_id="",
        file_paths=discover_asset_files(files_root, activity, row),
        context=f"asset {activity.facility_id}/{asset_type}/{serial_number}",
    )

    documents = []
    for index, document_filestore in enumerate(document_filestores, start=1):
        documents.append(
            {
                "id": None,
                "documentType": "ASSET",
                "fileStore": document_filestore,
                "documentUid": f"DOC-ASSET-{serial_number}-{index}",
                "additionalDetails": None,
                "geoLocation": {
                    "latitude": activity.latitude,
                    "longitude": activity.longitude,
                    "additionalDetails": None,
                },
            }
        )

    details = derive_asset_details_from_mdms(
        asset_type_mdms,
        asset_type=asset_type,
        system=activity.system,
    )
    if asset_type == "inverter":
        details.update(
            {
                "currentUnit": "1",
                "inverterCapacity": maybe_number(row.get("inverter_capacity", "")),
            }
        )
    elif asset_type == "battery":
        details.update(
            {
                "batteryCapacity": maybe_number(row.get("battery_capacity", "")),
                "batteryVoltage": maybe_number(row.get("battery_voltage", "")),
                "batteryType": row.get("battery_type", "") or None,
            }
        )
    elif asset_type == "panel":
        details.update(
            {
                "panelCapacity": maybe_number(row.get("panel_capacity", "")),
            }
        )
    details = {key: value for key, value in details.items() if value is not None}

    years = int(maybe_number(row.get("warranty_years", "")) or 0)
    now = int(time.time())
    warranty_start = iso_utc(now) if years > 0 else ""
    warranty_end = iso_utc(now + years * 365 * 24 * 60 * 60) if years > 0 else ""

    asset = {
        "assetId": asset_id or None,
        "tenantId": client.tenant_id,
        "activityFacilityID": activity.activity_facility_id,
        "facilityID": activity.facility_id,
        "system": activity.system,
        "serialNumber": serial_number,
        "assetTypeID": asset_type.upper(),
        "assetDetails": details,
        "brandID": row.get("brand_id", "") or None,
        "warrantyStartDate": warranty_start,
        "warrantyDuration": years,
        "warrantyEndDate": warranty_end,
        "wfStatus": "CREATED",
        "isActive": True,
        "documents": documents,
    }
    asset = {key: value for key, value in asset.items() if value is not None}
    endpoint = (
        f"asset-registry/v1/asset/_update?assetID={parse.quote(asset_id)}"
        if asset_id
        else "asset-registry/v1/asset/_create"
    )
    try:
        response = client.post_json(endpoint, {"assetDetail": {"Asset": asset}})
    except ApiRequestError as exc:
        if asset_id or error_code_from_response(exc) != "ERR_ASSET_DUPLICATE_VALIDATION":
            raise

        remote = fetch_asset_by_serial(
            client,
            activity_facility_id=activity.activity_facility_id,
            serial_number=serial_number,
        )
        remote_asset_id = ""
        if isinstance(remote, dict):
            remote_asset_id = str(remote.get("assetId") or remote.get("assetID") or "").strip()
        if not remote_asset_id:
            raise IngestionError(
                "Duplicate asset create failed, and existing asset could not be "
                f"found for activityFacilityID={activity.activity_facility_id}, "
                f"serialNumber={serial_number}"
            ) from exc

        print(f"Duplicate asset found for {serial_number}; retrying update {remote_asset_id}")
        asset["assetId"] = remote_asset_id
        response = client.post_json(
            f"asset-registry/v1/asset/_update?assetID={parse.quote(remote_asset_id)}",
            {"assetDetail": {"Asset": asset}},
        )
        asset_id = remote_asset_id

    if isinstance(response, dict):
        returned = response.get("asset") or response.get("Asset") or {}
        if isinstance(returned, dict) and returned.get("assetId"):
            return str(returned["assetId"])
    return asset_id


def iso_utc(epoch_seconds: int) -> str:
    return time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime(epoch_seconds))


def collect_workflow_documents(
    client: ApiClient,
    activity: ActivityFacility,
    bom_pdf_filestore_id: str,
    installation_image_mdms: InstallationImageMdms,
    files_root: Path,
) -> list[dict[str, Any]]:
    documents: list[dict[str, Any]] = []
    workflow_base = facility_folder(files_root, activity.facility_id) / "workflow_documents"

    asset_media_base = workflow_base / "asset_media"
    for asset_type_dir in direct_child_dirs(asset_media_base):
        asset_type = asset_type_dir.name
        for item_type_dir in direct_child_dirs(asset_type_dir):
            item_type = item_type_dir.name
            document_type = f"{asset_type}-{item_type}"
            for index, file_path in enumerate(direct_child_files(item_type_dir), start=1):
                filestore_id = client.upload_file(str(file_path))
                documents.append(
                    workflow_document(
                        document_type=document_type,
                        filestore_id=filestore_id,
                        uid=f"DOC-{asset_type}-{item_type}-{index}-{int(time.time() * 1000)}",
                        latitude=activity.latitude,
                        longitude=activity.longitude,
                    )
                )

    for index, file_path in enumerate(
        direct_child_files(workflow_base / "installation_report"),
        start=1,
    ):
        filestore_id = client.upload_file(str(file_path))
        documents.append(
            workflow_document(
                document_type="INSTALLATION_REPORT",
                filestore_id=filestore_id,
                uid=f"INSTALLATION-REPORT-file-{index}-{int(time.time() * 1000)}",
                latitude=activity.latitude,
                longitude=activity.longitude,
            )
        )

    for code in sorted(
        installation_image_mdms.requirements,
        key=lambda value: int(value) if value.isdigit() else value,
    ):
        for index, file_path in enumerate(
            discover_installation_image_files(files_root, activity, code),
            start=1,
        ):
            filestore_id = client.upload_file(str(file_path))
            documents.append(
                workflow_document(
                    document_type=f"INSTALLATION_IMAGE-{code}",
                    filestore_id=filestore_id,
                    uid=f"INSTALLATION-IMAGE-{code}-{index}-{int(time.time() * 1000)}",
                    latitude=activity.latitude,
                    longitude=activity.longitude,
                )
            )

    if bom_pdf_filestore_id:
        documents.append(
            workflow_document(
                document_type="INSTALLATION_REPORT_BOM",
                filestore_id=bom_pdf_filestore_id,
                uid=f"BOM-{activity.activity_facility_id}-{int(time.time() * 1000)}",
                latitude=activity.latitude,
                longitude=activity.longitude,
            )
        )

    return dedupe_documents(documents)


def dedupe_documents(documents: list[dict[str, Any]]) -> list[dict[str, Any]]:
    seen: set[tuple[str, str]] = set()
    unique: list[dict[str, Any]] = []
    for document in documents:
        key = (
            str(document.get("documentType") or ""),
            str(document.get("fileStoreId") or document.get("fileStore") or ""),
        )
        if key in seen:
            continue
        seen.add(key)
        unique.append(document)
    return unique


def workflow_document(
    *,
    document_type: str,
    filestore_id: str,
    uid: str,
    latitude: str,
    longitude: str,
) -> dict[str, Any]:
    return {
        "id": None,
        "documentType": document_type,
        "fileStoreId": filestore_id,
        "documentUid": uid or f"DOC-{document_type}-{int(time.time() * 1000)}",
        "additionalDetails": None,
        "geoLocation": {
            "latitude": latitude,
            "longitude": longitude,
            "additionalDetails": None,
        },
    }


def finalize_workflow(
    client: ApiClient,
    activity: ActivityFacility,
    documents: list[dict[str, Any]],
) -> None:
    client.post_json(
        "activity/v1/activities/workflow/update",
        {
            "activityFacilityId": activity.activity_facility_id,
            "workflow": {
                "action": DEFAULT_WORKFLOW_ACTION,
                "documents": documents,
            },
        },
    )


def ingest(
    client: ApiClient,
    workbook: dict[str, list[dict[str, str]]],
    assign_user_uuid: str,
    asset_type_mdms: AssetTypeMdms,
    installation_image_mdms: InstallationImageMdms,
    files_root: Path,
) -> None:
    activities = parse_activity_facilities(workbook.get("ActivityFacilities", []))
    validate_facility_ids("Assets", workbook.get("Assets", []))
    validate_facility_ids("BOMValues", workbook.get("BOMValues", []))

    assets_by_facility = group_by(workbook.get("Assets", []), "facility_id")
    bom_by_activity = {
        row.get("facility_id", "").strip(): row
        for row in workbook.get("BOMValues", [])
        if row.get("facility_id", "").strip()
    }

    for facility_id, activity_row in activities.items():
        print(f"\n=== Ingesting {facility_id} ===")
        validate_installation_image_counts(files_root, facility_id, installation_image_mdms)
        activity = resolve_activity_facility(client, activity_row, assign_user_uuid)
        print(
            "Resolved "
            f"{facility_id} -> {activity.activity_facility_id} "
            f"({activity.solution_design_code} -> {activity.system})"
        )
        _, bom_pdf = submit_bom(client, activity, bom_by_activity.get(facility_id))
        for asset in assets_by_facility.get(facility_id, []):
            asset_id = submit_asset(client, activity, asset, asset_type_mdms, files_root)
            if asset_id:
                print(f"Asset submitted: {asset_id}")
        documents = collect_workflow_documents(
            client,
            activity,
            bom_pdf,
            installation_image_mdms,
            files_root,
        )
        finalize_workflow(client, activity, documents)


def main() -> None:
    args = parse_args()
    env = parse_env(args.env_file)
    base_url = args.base_url or env.get("BASE_URL")
    tenant_id = args.tenant_id or env.get("TENANT_ID")
    if not base_url:
        raise IngestionError("BASE_URL missing. Provide --base-url or .env BASE_URL.")
    if not tenant_id:
        raise IngestionError("TENANT_ID missing. Provide --tenant-id or .env TENANT_ID.")
    login_config = login_config_from_env(env, args.env_file)

    workbook = load_xlsx(args.workbook)
    files_root = Path(args.files_root).expanduser()
    client = ApiClient(
        base_url=base_url,
        tenant_id=tenant_id,
        auth_token=args.auth_token,
        login_config=login_config,
        dry_run=args.dry_run,
        timeout=args.timeout,
    )
    login_result = client.login()
    asset_type_mdms = fetch_asset_type_mdms(client)
    installation_image_mdms = fetch_installation_image_mdms(client)
    ingest(
        client,
        workbook,
        login_result.assign_user_uuid,
        asset_type_mdms,
        installation_image_mdms,
        files_root,
    )


if __name__ == "__main__":
    main()
