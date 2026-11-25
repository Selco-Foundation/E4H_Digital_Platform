"""The vendor column of the facility Bulk Add template.

The column shows vendor organisation *names* in a dropdown. Names aren't unique, so a name shared
by two organisations carries its code: ``Name (CODE)``. On upload a cell resolves back to a code by
exact label, by a trailing ``(CODE)``, or by a bare code (older sheets), and anything else is a row
error rather than a guess.

The column is found by name whatever the MDMS master currently calls it: ``Vendor Name`` after the
master change, ``Vendor Code`` / ``Vendor Code (Mandatory)`` before it and in older sheets.
"""
import re
from collections import defaultdict
from typing import Any, Dict, Iterable, List, Optional, Tuple

import pandas as pd

VENDOR_COLUMN_BASE_NAMES = ("vendor name", "vendor code")
_TRAILING_CODE = re.compile(r"^(?P<name>.*?)\s*\((?P<code>[^()]+)\)\s*$")


def header_base_name(header: Any) -> str:
    """The header without its " (Mandatory)" suffix, lower-cased."""
    if header is None:
        return ""
    return str(header).replace("(Mandatory)", "").strip().lower()


def find_vendor_column(columns: Iterable[Any]) -> Optional[str]:
    """The vendor column's header as it appears in the sheet, or None when there is none."""
    for column in columns:
        if header_base_name(column) in VENDOR_COLUMN_BASE_NAMES:
            return column
    return None


def cell_text(value: Any) -> str:
    """A cell as trimmed text; blank for empty cells, and ``123.0`` read back as ``123``."""
    if value is None or (not isinstance(value, str) and pd.isna(value)):
        return ""
    text = str(value).strip()
    if len(text) > 2 and text.endswith(".0") and text[:-2].isdigit():
        text = text[:-2]
    return text


def vendor_options(organisations: List[Dict[str, Any]]) -> List[Tuple[str, str, str]]:
    """``(label, code, name)`` per organisation, sorted by label. The label is the name, with the
    code appended only where another organisation has the same name (or there is no name)."""
    by_name = defaultdict(int)
    for organisation in organisations:
        by_name[cell_text(organisation.get("name")).casefold()] += 1
    options = []
    for organisation in organisations:
        code = cell_text(organisation.get("code"))
        name = cell_text(organisation.get("name"))
        if not code:
            continue
        label = name if name and by_name[name.casefold()] == 1 else f"{name or code} ({code})"
        options.append((label, code, name))
    options.sort(key=lambda option: option[0].casefold())
    return options


class VendorResolver:
    """Turns a vendor cell back into an organisation code."""

    def __init__(self, organisations: List[Dict[str, Any]]):
        self._codes = {}
        self._label_to_code = {}
        self._codes_by_name = defaultdict(list)
        self._organisation_by_code = {}
        for label, code, name in vendor_options(organisations):
            self._codes[code.casefold()] = code
            self._label_to_code[label.casefold()] = code
            if name:
                self._codes_by_name[name.casefold()].append(code)
        for organisation in organisations:
            code = cell_text(organisation.get("code"))
            if code:
                self._organisation_by_code[code] = organisation

    def organisation(self, code: str) -> Optional[Dict[str, Any]]:
        return self._organisation_by_code.get(code)

    def resolve(self, value: Any) -> Tuple[Optional[str], Optional[str]]:
        """``(code, None)``, ``(None, None)`` for a blank cell, or ``(None, error)``."""
        text = cell_text(value)
        if not text:
            return None, None
        key = text.casefold()
        if key in self._label_to_code:
            return self._label_to_code[key], None
        match = _TRAILING_CODE.match(text)
        if match and match.group("code").strip().casefold() in self._codes:
            return self._codes[match.group("code").strip().casefold()], None
        if key in self._codes:
            return self._codes[key], None
        if len(self._codes_by_name.get(key, [])) > 1:
            return None, (f"Vendor '{text}' matches more than one organisation; pick the one with "
                          "its code from the dropdown")
        return None, f"Vendor '{text}' is not an active vendor organisation"


class ComplaintResolverCheck:
    """Whether each vendor organisation has an active Complaint Resolver, asked once per
    organisation. facility-service refuses to map an organisation without one, so a row naming it
    is failed here, at validation, with the cause spelled out."""

    def __init__(self, org_client, hrms_client, request_info):
        self._org_client = org_client
        self._hrms_client = hrms_client
        self._request_info = request_info
        self._errors: Dict[str, Optional[str]] = {}

    def error_for(self, code: str, organisation: Dict[str, Any]) -> Optional[str]:
        """None when the organisation has a resolver, else the row error to show."""
        if code not in self._errors:
            self._errors[code] = self._check(code, organisation)
        return self._errors[code]

    def _check(self, code: str, organisation: Dict[str, Any]) -> Optional[str]:
        name = cell_text(organisation.get("name")) or code
        try:
            user_ids = self._org_client.fetch_org_user_ids(cell_text(organisation.get("id")), self._request_info)
            if self._hrms_client.find_active_complaint_resolvers(user_ids, self._request_info):
                return None
        except Exception as e:
            return f"Could not check vendor '{name}' for a Complaint Resolver ({e}); try again"
        return (f"Vendor '{name}' has no active Complaint Resolver; add one before ingesting "
                "its facilities")
