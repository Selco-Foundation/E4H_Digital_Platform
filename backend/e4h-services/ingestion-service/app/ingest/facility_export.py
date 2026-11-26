"""View Facilities' Download: every facility the caller can see that matches their search and
filters, as an Excel workbook.

The search runs as the caller, with their own RequestInfo and `x-acting-role`, so facility-service
scopes it to their jurisdiction exactly as it scopes the list on screen. It must never be sent as
this service: a service identity skips that scoping and would export every facility in the country.
"""
from typing import Any, Callable, Dict, Iterable, List, Optional

from openpyxl import Workbook
from openpyxl.cell import WriteOnlyCell
from openpyxl.styles import Font, PatternFill

SHEET_NAME = "Facilities"

# (header, facility -> value). Boundary and MDMS values are codes here; `export_rows` names them.
EXPORT_COLUMNS = [
    ("Facility ID", "facility_id"),
    ("Facility Name", "facility_name"),
    ("State", "state"),
    ("District", "district"),
    ("Block", "block"),
    ("Facility Category", "facility_category"),
    ("Facility Type", "facility_type"),
    ("HFR ID", "hfr_id"),
    ("NIN ID", "nin_id"),
    ("PoC Username", "facility_poc_username"),
]
COLUMN_WIDTHS = [18, 36, 18, 22, 22, 20, 30, 18, 18, 24]

_CRITERIA_LISTS = ("state", "district", "block", "boundaryCodes")


def _text(value: Any) -> str:
    return "" if value is None else str(value).strip()


def build_export_criteria(tenant_id: str, filters: Dict[str, Any]) -> Dict[str, Any]:
    """`/_bulk-search` criteria for the page's filters and search text, every row at once.

    `filters` is what View Facilities sends: `searchText`, and the `state`, `district`, `block`
    and `boundaryCodes` code lists. Anything else is ignored, so the export can't ask for more
    than the list on screen does.
    """
    criteria: Dict[str, Any] = {"tenantIds": [tenant_id], "sendNonPaginatedResponse": True}
    for key in _CRITERIA_LISTS:
        values = [_text(value) for value in (filters.get(key) or []) if _text(value)]
        if values:
            criteria[key] = values
    search_text = _text(filters.get("searchText"))
    if search_text:
        criteria["searchText"] = search_text
    return criteria


def boundary_codes_of(facilities: Iterable[Dict[str, Any]]) -> List[str]:
    """Every state, district and block code on the facilities, for one localization lookup."""
    codes = set()
    for facility in facilities:
        boundary = facility.get("boundary") or {}
        for level in ("state", "district", "block"):
            code = _text(boundary.get(level))
            if code:
                codes.add(code)
    return sorted(codes)


def export_rows(facilities: Iterable[Dict[str, Any]],
                boundary_name: Callable[[str], str],
                category_names: Dict[str, str],
                type_names: Dict[str, str]) -> List[List[str]]:
    """One row of text per facility, in `EXPORT_COLUMNS` order, with names in place of codes."""
    rows = []
    for facility in facilities:
        boundary = facility.get("boundary") or {}
        row = []
        for _, key in EXPORT_COLUMNS:
            if key in ("state", "district", "block"):
                code = _text(boundary.get(key))
                row.append(boundary_name(code) if code else "")
            elif key == "facility_category":
                code = _text(facility.get(key))
                row.append(category_names.get(code, code))
            elif key == "facility_type":
                code = _text(facility.get(key))
                row.append(type_names.get(code, code))
            else:
                row.append(_text(facility.get(key)))
        rows.append(row)
    return rows


def write_export_workbook(path: str, rows: List[List[str]]) -> None:
    """Writes the rows under a bold header, filterable and with the header frozen.

    Write-only mode, so a country-wide export doesn't hold every cell object in memory. Every
    cell is stored as text: an HFR ID keeps its leading zeros and isn't turned into a number, and
    a value starting with "=" stays text instead of becoming a formula.
    """
    workbook = Workbook(write_only=True)
    sheet = workbook.create_sheet(SHEET_NAME)
    for index, width in enumerate(COLUMN_WIDTHS):
        sheet.column_dimensions[chr(ord("A") + index)].width = width
    sheet.freeze_panes = "A2"
    last_column = chr(ord("A") + len(EXPORT_COLUMNS) - 1)
    sheet.auto_filter.ref = f"A1:{last_column}{max(len(rows), 0) + 1}"

    header_font = Font(bold=True)
    header_fill = PatternFill("solid", fgColor="EEEEEE")
    header = []
    for title, _ in EXPORT_COLUMNS:
        cell = WriteOnlyCell(sheet, value=title)
        cell.font = header_font
        cell.fill = header_fill
        header.append(cell)
    sheet.append(header)

    for row in rows:
        cells = []
        for value in row:
            cell = WriteOnlyCell(sheet, value=value)
            cell.data_type = "s"
            cells.append(cell)
        sheet.append(cells)
    workbook.save(path)


def names_by_code(records: Optional[List[Dict[str, Any]]]) -> Dict[str, str]:
    """`{code: name}` for an MDMS master's rows."""
    return {
        _text(record.get("code")): _text(record.get("name"))
        for record in (records or [])
        if _text(record.get("code"))
    }
