"""
Cross-field validation for Health Facility Type + System Type + Solution Design Type + Capacity.

Rules are loaded from MDMS master ``facility.FacilitySolarConfigurationRule``.
Custom Solution Design and Custom Capacity are always permitted when a combo is covered.
Uncovered facility types or facility+system combos allow only Custom + Custom.
"""

from __future__ import annotations

import math
import re
from typing import Any, Callable, Dict, List, Optional, Set, Tuple

import pandas as pd

from app.schemas.request_info import RequestInfo
from app.utils.facility_validator import resolve_spreadsheet_header_for_schema_code

FACILITY_SOLAR_CONFIGURATION_RULE_SCHEMA = "facility.FacilitySolarConfigurationRule"

CUSTOM_SOLUTION_DESIGN_CODE = "CUSTOM"
CUSTOM_CAPACITY_CODE = "CUSTOM"

COLUMN_CODE_FACILITY_TYPE = "facility_type"
COLUMN_CODE_SYSTEM_TYPE = "system_type"
COLUMN_CODE_SOLUTION_DESIGN = "facility_details.solar_solution_design_type"
COLUMN_CODE_TOTAL_CAPACITY = "total_system_capacity"
COLUMN_CODE_CUSTOM_SOLUTION_DESIGN = "custom_solar_solution_design"
COLUMN_CODE_CUSTOM_TOTAL_CAPACITY = "custom_solar_system_capacity"

CAPACITY_TOLERANCE = 0.001

ERR_SOLAR_COLUMNS_MISSING = (
    "Solar configuration columns are missing from the file; "
    "Health Facility Type, System Type, Solution Design Type and Total System Capacity are required."
)
ERR_SOLAR_CUSTOM_ONLY_UNCOVERED = (
    "For Health Facility Type '{facility_type}' and System Type '{system_type}', "
    "only Custom Solution Design and Custom Capacity are allowed."
)
ERR_SOLAR_PAIR_NOT_ALLOWED = (
    "For Health Facility Type '{facility_type}' and System Type '{system_type}', "
    "Solution Design Type '{solution}' with Total System Capacity '{capacity}' is not allowed. "
    "Allowed predefined pairs: {allowed}."
)
ERR_SOLAR_CUSTOM_ONLY_FACILITY_TYPE = (
    "For Health Facility Type '{facility_type}', only Custom Solution Design and Custom Capacity are allowed."
)
ERR_CUSTOM_SOLUTION_DESIGN_REQUIRED = (
    "Custom Solution Design Type is mandatory when Solution Design Type is '{solution}'."
)
ERR_CUSTOM_CAPACITY_REQUIRED = (
    "Custom Total System Capacity is mandatory when Total System Capacity is '{capacity}'."
)
ERR_CUSTOM_CAPACITY_NOT_NUMERIC = (
    "Custom Total System Capacity '{value}' must be numeric."
)

# Fallback when MDMS rules are not yet loaded (codes must match facility.* masters).
_DEFAULT_RULES: List[Dict[str, Any]] = [
    {"facilityType": "SC", "systemType": "DC", "solutionDesignType": "OFF_GRID_DC_SYSTEM", "capacityKwp": 0.25},
    {"facilityType": "SC", "systemType": "DC", "solutionDesignType": "OFF_GRID_DC_SYSTEM", "capacityKwp": 0.5},
    {"facilityType": "SC", "systemType": "AC_OFF_GRID", "solutionDesignType": "NO_DELIVERY_NO_COLDCHAIN", "capacityKwp": 1.0},
    {"facilityType": "SC", "systemType": "AC_OFF_GRID", "solutionDesignType": "DELIVERY_ROOM_ONLY", "capacityKwp": 2.0},
    {"facilityType": "SC", "systemType": "AC_OFF_GRID", "solutionDesignType": "DELIVERY_ROOM_COLDCHAIN", "capacityKwp": 3.0},
    {"facilityType": "SC", "systemType": "AC_OFF_GRID", "solutionDesignType": "COLDCHAIN_ONLY", "capacityKwp": 3.0},
    {"facilityType": "PHC", "systemType": "AC_OFF_GRID", "solutionDesignType": "PHC_NO_DELIVERY_NO_COLDCHAIN_LAB", "capacityKwp": 5.0},
    {"facilityType": "PHC", "systemType": "AC_OFF_GRID", "solutionDesignType": "PHC_DELIVERY_ROOM", "capacityKwp": 5.0},
    {"facilityType": "PHC", "systemType": "AC_OFF_GRID", "solutionDesignType": "PHC_COLDCHAIN_LAB", "capacityKwp": 5.0},
    {"facilityType": "PHC", "systemType": "AC_OFF_GRID", "solutionDesignType": "PHC_DELIVERY_COLDCHAIN_LAB", "capacityKwp": 6.0},
    {"facilityType": "PHC", "systemType": "AC_OFF_GRID", "solutionDesignType": "PHC_DELIVERY_COLDCHAIN_LAB_DENTAL_ADMIN", "capacityKwp": 8.0},
    {"facilityType": "PHC", "systemType": "HYBRID", "solutionDesignType": "AC_HYBRID_SYSTEM", "capacityKwp": 7.0},
    {"facilityType": "PHC", "systemType": "HYBRID", "solutionDesignType": "AC_HYBRID_SYSTEM", "capacityKwp": 10.0},
    {"facilityType": "ANGANWADI", "systemType": "AC_OFF_GRID", "solutionDesignType": "LIGHT_FAN_EQUIPMENT", "capacityKwp": 0.63},
    {"facilityType": "ANGANWADI", "systemType": "AC_OFF_GRID", "solutionDesignType": "LIGHT_FAN_EQUIPMENT", "capacityKwp": 1.0},
    {"facilityType": "ANGANWADI", "systemType": "DC_OFF_GRID", "solutionDesignType": "ONLY_LIGHT_FAN", "capacityKwp": 0.25},
]

SolarRulesIndex = Dict[Tuple[str, str], Set[Tuple[str, float]]]

# facility.SystemType now has separate codes per phase (e.g. AC_ON_GRID_SINGLE_PHASE /
# AC_ON_GRID_THREE_PHASE, HYBRID_SINGLE_PHASE / HYBRID_THREE_PHASE, AC_OFF_GRID_THREE_PHASE),
# but facility.FacilitySolarConfigurationRule still keys its combos on the base system type
# (DC, AC_OFF_GRID, HYBRID, AC_ON_GRID) - phase doesn't change which combos are allowed. Strip
# the phase suffix before looking a system type code up against the rules index.
SYSTEM_TYPE_PHASE_SUFFIX_RE = re.compile(r"_(?:SINGLE|THREE)_PHASE$", re.IGNORECASE)


def normalize_system_type_code(system_code: str) -> str:
    return SYSTEM_TYPE_PHASE_SUFFIX_RE.sub("", system_code or "")


def _cell_str(val: Any) -> str:
    if pd.isna(val):
        return ""
    return str(val).strip()


def _mdms_name_to_code_map(column_list: List[Dict[str, Any]], code: str) -> Dict[str, str]:
    for col in column_list:
        if col.get("code") != code:
            continue
        mapping: Dict[str, str] = {}
        for item in col.get("mdms_values") or []:
            name = item.get("name")
            item_code = item.get("code")
            if name and item_code:
                mapping[str(name).strip()] = str(item_code).strip()
        return mapping
    return {}


def _capacity_name_to_kwp_map(column_list: List[Dict[str, Any]]) -> Dict[str, float]:
    for col in column_list:
        if col.get("code") != COLUMN_CODE_TOTAL_CAPACITY:
            continue
        mapping: Dict[str, float] = {}
        for item in col.get("mdms_values") or []:
            name = item.get("name")
            if not name:
                continue
            kwp = item.get("capacityKwp")
            if kwp is None:
                kwp = _parse_capacity_kwp_from_display(str(name))
            if kwp is not None:
                mapping[str(name).strip()] = float(kwp)
        return mapping
    return {}


def _parse_capacity_kwp_from_display(value: str) -> Optional[float]:
    if not value:
        return None
    normalized = value.strip().lower().replace("kwp", "").strip()
    try:
        return float(normalized)
    except ValueError:
        match = re.search(r"(\d+(?:\.\d+)?)", normalized)
        if match:
            try:
                return float(match.group(1))
            except ValueError:
                return None
    return None


def _capacities_equal(a: float, b: float) -> bool:
    return math.isclose(a, b, rel_tol=0, abs_tol=CAPACITY_TOLERANCE)


def build_solar_rules_index(rule_records: List[Dict[str, Any]]) -> SolarRulesIndex:
    index: SolarRulesIndex = {}
    for record in rule_records:
        if record.get("active") is False:
            continue
        facility_type = str(record.get("facilityType") or "").strip()
        system_type = str(record.get("systemType") or "").strip()
        solution_type = str(record.get("solutionDesignType") or "").strip()
        capacity = record.get("capacityKwp")
        if not facility_type or not system_type or not solution_type or capacity is None:
            continue
        key = (facility_type, system_type)
        index.setdefault(key, set()).add((solution_type, float(capacity)))
    return index


def covered_facility_types(rules_index: SolarRulesIndex) -> Set[str]:
    return {facility_type for facility_type, _ in rules_index.keys()}


def load_solar_configuration_rules(mdms_client: Any, request_info: RequestInfo) -> SolarRulesIndex:
    records = mdms_client.fetch_mdms_records(request_info, FACILITY_SOLAR_CONFIGURATION_RULE_SCHEMA)
    if not records:
        records = _DEFAULT_RULES
    return build_solar_rules_index(records)


def resolve_display_to_code(display_value: str, name_to_code: Dict[str, str]) -> str:
    if not display_value:
        return ""
    if display_value in name_to_code:
        return name_to_code[display_value]
    upper_map = {k.upper(): v for k, v in name_to_code.items()}
    return upper_map.get(display_value.upper(), display_value)


def resolve_capacity_kwp(
    display_value: str,
    name_to_kwp: Dict[str, float],
    capacity_name_to_code: Dict[str, str],
) -> Optional[float]:
    if not display_value:
        return None
    if resolve_display_to_code(display_value, capacity_name_to_code) == CUSTOM_CAPACITY_CODE:
        return None
    if display_value in name_to_kwp:
        return name_to_kwp[display_value]
    return _parse_capacity_kwp_from_display(display_value)


def _format_allowed_pairs(
    pairs: Set[Tuple[str, float]],
    solution_name_to_code: Dict[str, str],
) -> str:
    code_to_name = {code: name for name, code in solution_name_to_code.items()}
    parts = []
    for solution_code, capacity in sorted(pairs, key=lambda x: (x[0], x[1])):
        label = code_to_name.get(solution_code, solution_code)
        cap_display = f"{capacity:g} kWp".replace(".0 kWp", " kWp")
        parts.append(f"{label} / {cap_display}")
    return "; ".join(parts)


def _is_included_in_field_plan_row(row: pd.Series, df: pd.DataFrame, column_list: List[Dict[str, Any]]) -> bool:
    header = resolve_spreadsheet_header_for_schema_code(df, column_list, "include_in_fieldplan")
    if not header:
        for col_name in df.columns:
            if "included in field plan" in str(col_name).lower():
                header = col_name
                break
    if not header:
        return True
    return _cell_str(row.get(header, "")).lower() == "yes"


def validate_facility_solar_configuration_row(
    row: pd.Series,
    column_list: List[Dict[str, Any]],
    rules_index: SolarRulesIndex,
    facility_type_header: Optional[str],
    system_type_header: Optional[str],
    solution_header: Optional[str],
    capacity_header: Optional[str],
    name_to_code_by_column: Dict[str, Dict[str, str]],
    capacity_name_to_kwp: Dict[str, float],
    custom_solution_header: Optional[str] = None,
    custom_capacity_header: Optional[str] = None,
) -> List[str]:
    if not all([facility_type_header, system_type_header, solution_header, capacity_header]):
        return [ERR_SOLAR_COLUMNS_MISSING]

    facility_display = _cell_str(row.get(facility_type_header, ""))
    system_display = _cell_str(row.get(system_type_header, ""))
    solution_display = _cell_str(row.get(solution_header, ""))
    capacity_display = _cell_str(row.get(capacity_header, ""))

    if not facility_display or not system_display or not solution_display or not capacity_display:
        return []

    facility_code = resolve_display_to_code(
        facility_display, name_to_code_by_column.get(COLUMN_CODE_FACILITY_TYPE, {})
    )
    system_code = normalize_system_type_code(resolve_display_to_code(
        system_display, name_to_code_by_column.get(COLUMN_CODE_SYSTEM_TYPE, {})
    ))
    solution_code = resolve_display_to_code(
        solution_display, name_to_code_by_column.get(COLUMN_CODE_SOLUTION_DESIGN, {})
    )
    capacity_code = resolve_display_to_code(
        capacity_display, name_to_code_by_column.get(COLUMN_CODE_TOTAL_CAPACITY, {})
    )

    is_custom_solution = solution_code == CUSTOM_SOLUTION_DESIGN_CODE
    is_custom_capacity = capacity_code == CUSTOM_CAPACITY_CODE

    # These apply regardless of which branch below the row falls into, so they're collected
    # into `errors` (not returned early) and carried through every return path.
    errors: List[str] = []
    if is_custom_solution and custom_solution_header and not _cell_str(row.get(custom_solution_header, "")):
        errors.append(ERR_CUSTOM_SOLUTION_DESIGN_REQUIRED.format(solution=solution_display))
    if is_custom_capacity and custom_capacity_header and not _cell_str(row.get(custom_capacity_header, "")):
        errors.append(ERR_CUSTOM_CAPACITY_REQUIRED.format(capacity=capacity_display))

    if custom_capacity_header:
        custom_capacity_value = _cell_str(row.get(custom_capacity_header, ""))
        if custom_capacity_value:
            try:
                if not math.isfinite(float(custom_capacity_value)):
                    raise ValueError(custom_capacity_value)
            except ValueError:
                errors.append(ERR_CUSTOM_CAPACITY_NOT_NUMERIC.format(value=custom_capacity_value))

    covered_types = covered_facility_types(rules_index)
    if covered_types and facility_code not in covered_types:
        if not (is_custom_solution and is_custom_capacity):
            errors.append(ERR_SOLAR_CUSTOM_ONLY_FACILITY_TYPE.format(facility_type=facility_display))
        return errors

    combo_key = (facility_code, system_code)
    allowed_pairs = rules_index.get(combo_key)

    if allowed_pairs is None:
        if not (is_custom_solution and is_custom_capacity):
            errors.append(
                ERR_SOLAR_CUSTOM_ONLY_UNCOVERED.format(
                    facility_type=facility_display,
                    system_type=system_display,
                )
            )
        return errors

    if is_custom_solution or is_custom_capacity:
        return errors

    capacity_kwp = resolve_capacity_kwp(
        capacity_display,
        capacity_name_to_kwp,
        name_to_code_by_column.get(COLUMN_CODE_TOTAL_CAPACITY, {}),
    )
    if capacity_kwp is None:
        return errors

    for allowed_solution, allowed_capacity in allowed_pairs:
        if solution_code == allowed_solution and _capacities_equal(capacity_kwp, allowed_capacity):
            return errors

    errors.append(
        ERR_SOLAR_PAIR_NOT_ALLOWED.format(
            facility_type=facility_display,
            system_type=system_display,
            solution=solution_display,
            capacity=capacity_display,
            allowed=_format_allowed_pairs(
                allowed_pairs,
                name_to_code_by_column.get(COLUMN_CODE_SOLUTION_DESIGN, {}),
            ),
        )
    )
    return errors


def validate_facility_solar_configuration(
    df: pd.DataFrame,
    schema: Dict[str, Any],
    mdms_client: Any,
    request_info: RequestInfo,
    add_err: Callable[[Any, str], None],
    *,
    only_included_in_field_plan: bool = True,
) -> None:
    column_list = schema.get("column_list") or []
    rules_index = load_solar_configuration_rules(mdms_client, request_info)

    facility_type_header = resolve_spreadsheet_header_for_schema_code(
        df, column_list, COLUMN_CODE_FACILITY_TYPE
    )
    system_type_header = resolve_spreadsheet_header_for_schema_code(
        df, column_list, COLUMN_CODE_SYSTEM_TYPE
    )
    solution_header = resolve_spreadsheet_header_for_schema_code(
        df, column_list, COLUMN_CODE_SOLUTION_DESIGN
    )
    capacity_header = resolve_spreadsheet_header_for_schema_code(
        df, column_list, COLUMN_CODE_TOTAL_CAPACITY
    )
    custom_solution_header = resolve_spreadsheet_header_for_schema_code(
        df, column_list, COLUMN_CODE_CUSTOM_SOLUTION_DESIGN
    )
    custom_capacity_header = resolve_spreadsheet_header_for_schema_code(
        df, column_list, COLUMN_CODE_CUSTOM_TOTAL_CAPACITY
    )

    name_to_code_by_column = {
        COLUMN_CODE_FACILITY_TYPE: _mdms_name_to_code_map(column_list, COLUMN_CODE_FACILITY_TYPE),
        COLUMN_CODE_SYSTEM_TYPE: _mdms_name_to_code_map(column_list, COLUMN_CODE_SYSTEM_TYPE),
        COLUMN_CODE_SOLUTION_DESIGN: _mdms_name_to_code_map(column_list, COLUMN_CODE_SOLUTION_DESIGN),
        COLUMN_CODE_TOTAL_CAPACITY: _mdms_name_to_code_map(column_list, COLUMN_CODE_TOTAL_CAPACITY),
    }
    capacity_name_to_kwp = _capacity_name_to_kwp_map(column_list)

    for idx, row in df.iterrows():
        if only_included_in_field_plan and not _is_included_in_field_plan_row(row, df, column_list):
            continue
        for message in validate_facility_solar_configuration_row(
            row=row,
            column_list=column_list,
            rules_index=rules_index,
            facility_type_header=facility_type_header,
            system_type_header=system_type_header,
            solution_header=solution_header,
            capacity_header=capacity_header,
            name_to_code_by_column=name_to_code_by_column,
            capacity_name_to_kwp=capacity_name_to_kwp,
            custom_solution_header=custom_solution_header,
            custom_capacity_header=custom_capacity_header,
        ):
            add_err(idx, message)


def collect_facility_solar_configuration_errors_for_row(
    row: pd.Series,
    row_idx: Any,
    df: pd.DataFrame,
    schema: Dict[str, Any],
    mdms_client: Any,
    request_info: RequestInfo,
    *,
    only_included_in_field_plan: bool = True,
) -> List[str]:
    errors: List[str] = []

    def add_err(_idx: Any, msg: str) -> None:
        errors.append(msg)

    validate_facility_solar_configuration(
        df=df.iloc[[row_idx]].copy(),
        schema=schema,
        mdms_client=mdms_client,
        request_info=request_info,
        add_err=add_err,
        only_included_in_field_plan=only_included_in_field_plan,
    )
    return list(dict.fromkeys(errors))
