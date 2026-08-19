import re
from typing import Any, Callable, Dict, List, MutableMapping, Optional, Set

import pandas as pd
from fastapi import HTTPException

# Same wording everywhere: MDMS pre-validation, API import, and Excel client hints.
ERR_HFR_OR_NIN_REQUIRED_WHEN_HEALTH = (
    "When Facility Category is HEALTH, at least one of HFR ID or NIN ID is required."
)
ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI = (
    "PoC Username is required when Facility Category is ANGANWADI."
)
ERR_POC_USERNAME_COLUMN_MISSING_FOR_ANGANWADI = (
    "PoC Username column is missing from the file; it is required for ANGANWADI facilities."
)


def normalize_facility_category_value(row: pd.Series) -> str:
    """
    Reads facility category from common template column names.
    Returns upper-cased value or '' when unset (IDs stay optional).
    """
    candidates = (
        "Category of Facility (Mandatory)",
        "Facility Category (Mandatory)",
    )
    for key in candidates:
        if key not in row.index:
            continue
        val = row.get(key, "")
        if pd.isna(val):
            continue
        s = str(val).strip()
        if s:
            return s.upper()
    return ""


def is_health_facility_category(category: str) -> bool:
    return category == "HEALTH"


def is_anganwadi_facility_category(category: str) -> bool:
    return category == "ANGANWADI"


def resolve_spreadsheet_header_for_schema_code(
    df: pd.DataFrame,
    schema_column_list: List[Dict[str, Any]],
    code: str,
) -> Optional[str]:
    """Map MDMS column `code` to the actual header string present in the dataframe."""
    cols = df.columns
    for col in schema_column_list:
        if col.get("code") != code:
            continue
        hn = format_col_name(col)
        if hn in cols:
            return hn
        base = (col.get("name") or "").strip()
        if base in cols:
            return base
    return None


def _is_legacy_mdms_hfr_nin_at_least_one_constraint(rc: Any) -> bool:
    """
    MDMS may define atLeastOneRequired on HFR ID + NIN ID for all rows.
    We enforce the same rule only when category is HEALTH in validate_hfr_nin_for_row,
    so skip this row constraint here to avoid duplicate errors.
    """
    ctype = getattr(rc, "type", None)
    if ctype != "atLeastOneRequired":
        return False
    fields = getattr(rc, "fields", None) or []
    return set(fields) == {"HFR ID", "NIN ID"}


def project_facility_validation(
    df, mdms_client, request_info, facility_client, boundary_data, schemaName
):
    """Main function that orchestrates all facility file validations."""

    # Ensure boundary_data is provided
    if boundary_data is None or boundary_data.empty:
        raise HTTPException(status_code=400, detail="Boundary data is missing or empty")

    if "BoundaryCode" not in boundary_data.columns:
        raise HTTPException(status_code=400, detail="Boundary data missing 'BoundaryCode' column")

    allowed_boundary_codes = set(
        str(x).strip() for x in boundary_data["BoundaryCode"] if pd.notna(x)
    )

    # Reset index so we always work with 0-based positional indices
    df = df.reset_index(drop=True)

    errors = [[] for _ in range(len(df))]
    add_err = lambda i, msg: errors[i].append(msg)

    # Only validate rows where Facility ID is empty
    new_rows = df[df["Facility Id"].isna() | (df["Facility Id"].astype(str).str.strip() == "")]
    if new_rows.empty:
        return errors  # No new rows to validate

    # Reset index on new_rows to get 0-based row positions
    new_rows = new_rows.reset_index()

    schema = mdms_client.get_column_definitions_and_row_constraints_with_metadata(
        request_info, schemaName
    )

    # Use positional index mapping to reference errors in original df
    validate_columns(new_rows, schema, lambda i, m: add_err(new_rows.loc[i, "index"], m))
    validate_boundary_codes(new_rows, allowed_boundary_codes, lambda i, m: add_err(new_rows.loc[i, "index"], m))
    validate_unique_ids(df, schema, add_err)
    validate_row_constraints(new_rows, schema, lambda i, m: add_err(new_rows.loc[i, "index"], m))
    validate_anganwadi_poc_username(new_rows, schema, lambda i, m: add_err(new_rows.loc[i, "index"], m))
    validate_hfr_nin(new_rows, lambda i, m: add_err(new_rows.loc[i, "index"], m), facility_client)

    return errors


def field_plan_facility_validation(
    df, mdms_client, request_info, facility_client, boundary_data, schemaName
):
    """
    Field plan facility Excel validation: standard project checks for new rows,
    plus solar configuration rules for rows marked Included in Field Plan = Yes.
    """
    from app.utils.facility_solar_config_validator import validate_facility_solar_configuration

    errors = project_facility_validation(
        df, mdms_client, request_info, facility_client, boundary_data, schemaName
    )

    df = df.reset_index(drop=True)
    schema = mdms_client.get_column_definitions_and_row_constraints_with_metadata(
        request_info, schemaName
    )

    def add_err(i: int, msg: str) -> None:
        errors[i].append(msg)

    validate_facility_solar_configuration(
        df,
        schema,
        mdms_client,
        request_info,
        add_err,
        only_included_in_field_plan=True,
    )

    return errors


def facility_validation(
    df, mdms_client, request_info, facility_client, boundary_data, schemaName
):
    """Facility ingestion validation for new rows (boundary resolved upstream)."""
    df = df.reset_index(drop=True)

    errors = [[] for _ in range(len(df))]
    add_err = lambda i, msg: errors[i].append(msg)

    new_rows = df[df["Facility Id"].isna() | (df["Facility Id"].astype(str).str.strip() == "")]
    if new_rows.empty:
        return errors

    new_rows = new_rows.reset_index()

    schema = mdms_client.get_column_definitions_and_row_constraints_with_metadata(
        request_info, schemaName
    )

    validate_columns(new_rows, schema, lambda i, m: add_err(new_rows.loc[i, "index"], m))
    validate_unique_ids(df, schema, add_err)
    validate_row_constraints(new_rows, schema, lambda i, m: add_err(new_rows.loc[i, "index"], m))
    validate_anganwadi_poc_username(new_rows, schema, lambda i, m: add_err(new_rows.loc[i, "index"], m))
    validate_hfr_nin(new_rows, lambda i, m: add_err(new_rows.loc[i, "index"], m), facility_client)

    return errors


def assessment_plan_include_validation(
    df,
    mdms_client,
    request_info,
    include_col: str,
    facility_id_col: str,
    linked_ids: Set[str],
    availability_exclusions: Optional[Dict[str, str]] = None,
):
    """
    Validate assessment plan include Excel rows marked Include in Assessment Plan = Yes.
    Availability exclusions (ongoing assessment, etc.) are reported as EXCLUDED, not FAILED.
    Facility snapshot columns (PoC name, address, etc.) are not re-validated — include only
    selects existing project facilities; apply does not create facilities.
    """
    df = df.reset_index(drop=True)
    errors: List[List[str]] = [[] for _ in range(len(df))]
    exclusions: List[List[str]] = [[] for _ in range(len(df))]
    availability_exclusions = availability_exclusions or {}
    yes_row_count = 0

    for pos in range(len(df)):
        row = df.iloc[pos]
        include_val = str(row.get(include_col, "")).strip().lower()
        if include_val != "yes":
            continue

        yes_row_count += 1

        facility_id = _normalize_assessment_facility_id(row.get(facility_id_col))
        if not facility_id:
            errors[pos].append("Facility Id is required for Yes rows")
        elif facility_id not in linked_ids:
            errors[pos].append("Facility is not linked to this project")
        elif facility_id in availability_exclusions:
            exclusions[pos].append(availability_exclusions[facility_id])

        include_val_normalized = str(row.get(include_col, "")).strip().lower()
        if include_val_normalized and include_val_normalized not in {"yes", "no"}:
            errors[pos].append(f"{include_col} must be Yes or No")

    if yes_row_count == 0:
        raise HTTPException(
            status_code=400,
            detail="At least one facility must be marked Include in Assessment Plan = Yes",
        )

    return errors, exclusions


def _normalize_assessment_facility_id(val) -> str:
    if pd.isna(val) or val is None:
        return ""
    if isinstance(val, bool):
        return str(val).strip()
    if isinstance(val, int):
        return str(val)
    if isinstance(val, float):
        if val.is_integer():
            return str(int(val))
        return str(val).strip()
    text = str(val).strip()
    if not text or text.lower() == "nan":
        return ""
    if text.endswith(".0") and text[:-2].replace("-", "", 1).isdigit():
        return text[:-2]
    return text

def validate_boundary_codes(df, allowed_boundary_codes, add_err):
    """
    Validates that 'Boundary Code' column in df only contains values
    from allowed_boundary_codes set. Only validates rows in df passed here.
    """

    for i, val in enumerate(df["Boundary Code (Mandatory)"]):
        if pd.isna(val) or str(val).strip() == "":
            add_err(i, "Boundary Code is mandatory")
            continue

        str_val = str(val).strip()
        if str_val not in allowed_boundary_codes:
            add_err(i, f"Boundary Code '{str_val}' is invalid (not in boundary data)")


def validate_columns(df, schema, add_err):
    skip_columns = {"Boundary Code"}
    for col in schema["column_list"]:
        col_name = format_col_name(col)

        if col.get("name", "").strip() in skip_columns:
            continue

        # Check if column exists
        if col_name not in df.columns:
            if col.get("required"):
                raise HTTPException(status_code=400, detail=f"Missing mandatory column: {col_name}")
            continue

        for i, val in enumerate(df[col_name]):
            # Treat NaN or None as empty string
            if pd.isna(val):
                str_val = ""
            else:
                str_val = str(val).strip()

            # Check mandatory
            if col.get("required") and not str_val:
                add_err(i, f"{col_name} is mandatory")
                continue

            # Skip pattern/type checks if empty
            if not str_val:
                continue

            # Pattern validation
            if col.get("pattern"):
                pattern_val = str_val.split(".")[0] if str_val.endswith(".0") else str_val
                if not re.fullmatch(col["pattern"], pattern_val):  # fullmatch is safer than match
                    add_err(i, f"{col_name} does not match pattern {col['pattern']}")

            # Enum validation (case-insensitive)
            if col.get("type") == "enum-yes-no" and str_val.lower() not in {"yes", "no"}:
                add_err(i, f"{col_name} must be Yes or No")

            # --- Dropdown check (MDMS values) ---
            mdms_values = col.get("mdms_values")
            if mdms_values:
                effective_mdms = mdms_values
                facility_cat_for_type = ""
                if col.get("code") == "facility_type":
                    facility_cat_for_type = normalize_facility_category_value(df.iloc[i])
                    if facility_cat_for_type in ("HEALTH", "ANGANWADI"):
                        effective_mdms = [
                            v
                            for v in mdms_values
                            if str(v.get("facilityCategory") or "").strip().upper()
                            == facility_cat_for_type
                        ]
                allowed_values = [v.get("name") for v in effective_mdms if v.get("name")]
                if str_val not in allowed_values:
                    if col.get("code") == "facility_type" and facility_cat_for_type in (
                        "HEALTH",
                        "ANGANWADI",
                    ):
                        add_err(
                            i,
                            f"{col_name} must be a facility type for Facility Category "
                            f"'{facility_cat_for_type}' (MDMS facilityCategory); "
                            f"'{str_val}' is not valid for this category.",
                        )
                    else:
                        add_err(i, f"Invalid value in column '{col_name}'")


def validate_anganwadi_poc_username(df, schema, add_err):
    """When facility category is ANGANWADI, PoC Username (MDMS code facility_poc_username) is mandatory."""
    column_list = schema.get("column_list") or []
    for idx, row in df.iterrows():
        if not is_anganwadi_facility_category(normalize_facility_category_value(row)):
            continue
        header = resolve_spreadsheet_header_for_schema_code(df, column_list, "facility_poc_username")
        if not header:
            add_err(idx, ERR_POC_USERNAME_COLUMN_MISSING_FOR_ANGANWADI)
            continue
        val = row.get(header, "")
        if pd.isna(val) or str(val).strip() == "":
            add_err(idx, ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI)


def validate_unique_ids(df, schema, add_err):
    unique_columns = [c for c in schema["column_list"] if c["type"] == "Unique_Id"]

    for col in unique_columns:
        seen = {}
        col_name = format_col_name(col)

        for i, val in enumerate(df.get(col_name, [])):
            if pd.isna(val):
                continue

            key = str(val).strip()
            if not key:
                continue

            if key in seen:
                add_err(i, f"Duplicate value in {col_name}")
            else:
                seen[key] = i



def validate_row_constraints(df, schema, add_err):
    """
    Validates row-level constraints defined in schema against DataFrame rows.
    Handles cases where df column names may have '(Mandatory)' suffix,
    while row_constraints fields are without the suffix.
    """
    row_constraints = schema.get("row_constraints", [])

    # Build mapping: base column name -> formatted column name in df
    col_map = {}
    for col in schema.get("column_list", []):
        base_name = col.get("name", "").strip()
        formatted_name = format_col_name(col)
        if formatted_name in df.columns:
            col_map[base_name] = formatted_name
        elif base_name in df.columns:
            col_map[base_name] = base_name

    for idx, row in df.iterrows():
        for rc in row_constraints:
            if _is_legacy_mdms_hfr_nin_at_least_one_constraint(rc):
                continue
            raw_fields = getattr(rc, "fields", None) or []
            if not raw_fields:
                continue
            fields = [col_map.get(f, f) for f in raw_fields]
            values = []
            for f in fields:
                val = row.get(f, "")
                if pd.isna(val):
                    val = ""
                else:
                    val = str(val).strip()
                values.append(val)

            if rc.type == "atLeastOneRequired" and not any(values):
                add_err(idx, rc.message)

            elif rc.type == "allOrNoneRequired":
                filled_count = sum(bool(v) for v in values)
                if 0 < filled_count < len(values):
                    add_err(idx, rc.message)


def validate_hfr_nin(df, add_err, facility_client):
    checked_in_db: Dict[str, bool] = {}

    for idx, row in df.iterrows():
        validate_hfr_nin_for_row(
            row=row,
            row_idx=idx,
            df=df,
            add_err=add_err,
            facility_client=facility_client,
            checked_in_db=checked_in_db,
        )


def validate_hfr_nin_for_row(
    row: pd.Series,
    row_idx: Any,
    df: pd.DataFrame,
    add_err: Callable[[Any, str], None],
    facility_client: Any,
    checked_in_db: MutableMapping[str, bool],
) -> None:
    """Category-aware HFR/NIN checks + duplicate lookup (shared by bulk validate and import)."""
    hfr = row.get("HFR ID", "")
    nin = row.get("NIN ID", "")

    hfr = str(hfr).strip() if pd.notna(hfr) else ""
    nin = str(nin).strip() if pd.notna(nin) else ""

    category = normalize_facility_category_value(row)
    if is_health_facility_category(category) and not hfr and not nin:
        add_err(row_idx, ERR_HFR_OR_NIN_REQUIRED_WHEN_HEALTH)

    if not hfr and not nin:
        return

    check_db_duplicates(
        cache=checked_in_db,
        facility_client=facility_client,
        add_err=add_err,
        df=df,
        row_idx=row_idx,
        hfr=hfr if hfr else None,
        nin=nin if nin else None,
    )


def collect_hfr_nin_errors_for_row(
    row: pd.Series,
    row_idx: Any,
    df: pd.DataFrame,
    facility_client: Any,
    checked_in_db: MutableMapping[str, bool],
) -> List[str]:
    out: List[str] = []

    def add_err(_idx: Any, msg: str) -> None:
        out.append(msg)

    validate_hfr_nin_for_row(
        row=row,
        row_idx=row_idx,
        df=df,
        add_err=add_err,
        facility_client=facility_client,
        checked_in_db=checked_in_db,
    )
    return list(dict.fromkeys(out))


def collect_anganwadi_poc_username_errors_for_row(
    row: pd.Series,
    _row_idx: Any,
    df: pd.DataFrame,
    facility_schema: List[Dict[str, Any]],
) -> List[str]:
    if not is_anganwadi_facility_category(normalize_facility_category_value(row)):
        return []
    header = resolve_spreadsheet_header_for_schema_code(df, facility_schema, "facility_poc_username")
    if not header:
        return [ERR_POC_USERNAME_COLUMN_MISSING_FOR_ANGANWADI]
    val = row.get(header, "")
    if pd.isna(val) or str(val).strip() == "":
        return [ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI]
    return []


def check_db_duplicates(cache, facility_client, add_err, df, row_idx, hfr=None, nin=None):
    """
    Checks for duplicates in DB for HFR ID and NIN ID in the given row.
    tenant_id is fixed as 'in'. Only passes non-empty params to search API.
    If DB call fails, we log error for that row and skip further validation.
    """
    row = df.loc[row_idx]
    boundary_code = str(row.get("Boundary Code (Mandatory)", "")).strip()
    tenant_id = "in"

    try:
        for col_name, value, key in [
            ("HFR ID", hfr, "hfr_id"),
            ("NIN ID", nin, "nin_id"),
        ]:
            if not value:  # Skip if None or empty string
                continue

            cache_key = f"{boundary_code}|{key}|{value}"

            if cache_key not in cache:
                try:
                    result = facility_client.search_facility(
                        tenant_id=tenant_id,
                        boundary_code=boundary_code,
                        **({key: value} if value else {})  # only include if value is present
                    )
                    exists = result.get("totalCount", 0) > 0
                    cache[cache_key] = exists
                except Exception as e:
                    # Instead of letting row pass, flag it
                    add_err(row_idx, f"Could not validate {col_name}='{value}' in DB: {e}")
                    # Stop further checks for this row, to avoid partial validation
                    return

            if cache[cache_key]:
                add_err(row_idx, f"{col_name} '{value}' already exists in system")

    except Exception as e:
        # Catch unexpected errors and fail the row
        add_err(row_idx, f"Unexpected error during DB duplicate check: {e}")

def format_col_name(col: dict) -> str:
    """
    Formats column name with '(Mandatory)' if 'required' is True.
    Example:
        {"name": "Facility Name", "required": True}
        -> "Facility Name (Mandatory)"
    """
    name = col.get("name", "")
    required = col.get("required", False)
    return f"{name}{' (Mandatory)' if required else ''}"

