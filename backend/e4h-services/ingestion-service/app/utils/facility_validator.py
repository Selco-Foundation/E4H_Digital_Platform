import re

import pandas as pd
from fastapi import HTTPException


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
    validate_hfr_nin(new_rows, lambda i, m: add_err(new_rows.loc[i, "index"], m), facility_client)

    return errors


def facility_validation(
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
    validate_hfr_nin(new_rows, lambda i, m: add_err(new_rows.loc[i, "index"], m), facility_client)

    return errors


# ----------------- Helper Functions ----------------- #

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
    for col in schema["column_list"]:
        col_name = format_col_name(col)

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
                allowed_values = [v.get("name") for v in mdms_values if v.get("name")]
                if str_val not in allowed_values:
                    add_err(i, f"Invalid value in column '{col_name}'")

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
            fields = [col_map.get(f, f) for f in rc.fields]
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
    checked_in_db = {}

    for idx, row in df.iterrows():
        hfr = row.get("HFR ID", "")
        nin = row.get("NIN ID", "")

        hfr = str(hfr).strip() if pd.notna(hfr) else ""
        nin = str(nin).strip() if pd.notna(nin) else ""

        # Skip if both are empty/NaN
        if not hfr and not nin:
            continue

        # Pass only available IDs to DB check
        check_db_duplicates(
            cache=checked_in_db,
            facility_client=facility_client,
            add_err=add_err,
            df=df,
            row_idx=idx,
            hfr=hfr if hfr else None,
            nin=nin if nin else None,
        )


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
                    # ✅ Instead of letting row pass, flag it
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

