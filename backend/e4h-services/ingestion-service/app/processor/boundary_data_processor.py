import math
import os
import string
from typing import List, Dict, Set, Tuple, Optional
import re
import pandas as pd

from app.core.logging import AppLogger
from app.ingest.boundary_excel_data_loader import BoundaryExcelDataLoader
from app.ingest.service.data_loader import DataLoader
from app.ingest.service.data_writer import DataWriter
from app.ingest.service.validator import Validator
from app.schemas.request_info import RequestInfo
from app.utils.boundary_code_utils import boundary_code_segment
from app.utils.boundary_service_client import BoundaryServiceClient
from app.utils.convertor import build_localization_reverse_map
from app.utils.localization_service_client import LocalizationServiceClient

from dotenv import load_dotenv
load_dotenv()
boundary_service_url = os.getenv("BOUNDARY_SERVICE_URL")
localization_service_url = os.getenv("LOCALIZATION_SERVICE_URL")

logger = AppLogger().get_logger()

# LGD identity columns - defined, and required, by data-ingestion.BoundaryIngestionSchema,
# whose validators reject a row without them before it gets here. The codes are stored on
# each boundary this upload creates, so a later LGD extract can be matched on code rather
# than name; a level that already exists keeps the codes it has. Where rows disagree, the
# first row for a level wins. A rename then updates the localization label while the boundary
# code - which facility codes are built from - stays put.
LGD_STATE_CODE_COLUMN = "LGD State Code"
LGD_DISTRICT_CODE_COLUMN = "LGD District Code"
LGD_BLOCK_CODE_COLUMN = "LGD Block Code"
# Which LGD tier the block code came from. In LGD a Development Block and a
# Sub-district both hang directly off District, and our Block slot holds a mix of
# the two, so the code alone would be ambiguous to reconcile against.
LGD_BLOCK_TIER_COLUMN = "LGD Block Tier"


def _read_identity_cell(row, column):
    """Read an optional code cell as a string.

    Excel hands these back as floats (29.0) once pandas infers the column as numeric,
    so integral values are rendered without the trailing '.0'.
    """
    value = row.get(column)
    if value is None:
        return ""
    if isinstance(value, float):
        if math.isnan(value):
            return ""
        if value.is_integer():
            return str(int(value))
    text = str(value).strip()
    return "" if text.lower() in ("nan", "none") else text


def _lgd_details(**codes):
    """Drop empty codes so a partially filled sheet doesn't store blank identity fields."""
    return {key: value for key, value in codes.items() if value}


class BoundaryDataProcessor:
    def __init__(self, data_loader, validators: List[Validator], data_writer,
                 request_info: RequestInfo = None):
        self.data_loader = data_loader
        self.validators = validators
        self.data_writer = data_writer
        self.validation_errors = []
        self.request_info = request_info
        self.boundary_service_client = BoundaryServiceClient(boundary_service_url)
        self.localization_client = LocalizationServiceClient(localization_service_url)

        # Hierarchical structure to store boundary data
        self.hierarchy_levels = ["Country", "State", "District", "Block"]
        self.boundary_data: Dict[str, Dict] = {
            "Country": {},
            "State": {},
            "District": {},
            "Block": {}
        }

        # Track all boundary full codes that need to be checked/created
        self.all_boundary_full_codes: Set[str] = set()

        # Track which boundaries already exist in the system
        self.existing_boundaries: Set[str] = set()
        # casefold(code) -> canonical code from boundary service
        self.existing_boundaries_casefold: Dict[str, str] = {}
        # code built from the sheet -> existing boundary's exact code, where they differ only
        # in letter case (see _adopt_existing_codes)
        self.code_renames: Dict[str, str] = {}

        # normalized localization message -> list of "Boundary_<code>" localization codes
        self.localization_reverse_map: Dict[str, List[str]] = {}
        # row index -> {level: full_code} resolved for that row
        self.row_level_codes: Dict[object, Dict[str, str]] = {}

        # Track failed operations
        self.failed_boundaries: Dict[str, str] = {}  # {full_code: error_message}
        self.failed_relationships: Dict[Tuple[str, str], str] = {}  # {(full_code, boundary_type): error_message}

    def process_data(self):
        """Process and validate boundary data"""

        logger.trace("Starting boundary data processing")
        if isinstance(self.data_loader, BoundaryExcelDataLoader):
            boundary_df = self.data_loader.get_boundary_data()
            logger.info(boundary_df.head(2))
        else:
            logger.warning("Data loader is not compatible")
            return pd.DataFrame()

        boundary_df["status"] = None
        boundary_df["error"] = ""

        # Run all validators
        has_error = False
        for validator in self.validators:
            boundary_df = validator.validate(boundary_df)
            if (boundary_df["status"] == "fail").any():
                has_error = True

        # Collect validation errors
        self._collect_validation_errors(boundary_df)

        # Process valid boundaries
        valid_boundaries_df = boundary_df[boundary_df["status"].isna()]
        self._organize_boundary_data(valid_boundaries_df)
        self._check_existing_boundaries()
        self._adopt_existing_codes()
        self._create_new_boundaries()
        self._create_boundary_relationships()
        self._upsert_localization_for_boundaries()

        # Update the DataFrame with boundary creation results
        boundary_df = self._update_dataframe_with_results(boundary_df)

        return boundary_df

    def _collect_validation_errors(self, boundary_df):
        """Collect validation errors from the DataFrame"""
        self.validation_errors = []
        for idx, row in boundary_df[boundary_df["status"] == "fail"].iterrows():
            self.validation_errors.append({
                'row': idx + 2,  # +2 for Excel row numbers (header + 1-based)
                'boundary_code': row.get('Country', 'Unknown'),
                'errors': [row.get('error', '')]
            })

    def _load_boundary_localizations(self):
        """Fetch boundary localizations once and build the message -> codes reverse map."""
        if not localization_service_url:
            logger.warning("LOCALIZATION_SERVICE_URL not set; boundary existence will be checked by code only")
            return

        try:
            loc_response = self.localization_client.search_messages(
                tenant_id="in",
                locale="en_IN",
                module="rainmaker-in",
            )
            messages = loc_response.get("messages", []) if loc_response else []
            self.localization_reverse_map = build_localization_reverse_map(messages)
            logger.info(f"Built boundary localization reverse map with {len(self.localization_reverse_map)} entries "
                        f"from {len(messages)} messages")
        except Exception as e:
            logger.error(f"Error fetching boundary localizations: {e}", exc_info=True)

    @staticmethod
    def _localization_lookup_key(label: str) -> str:
        return label.strip().lower().replace(" ", "") if label else ""

    def _resolve_code_by_localization(self, label: str, parent_full_code: Optional[str]) -> Optional[str]:
        """Resolve an already existing boundary code from its localized label.

        Mirrors the facility bulk ingestion resolver: the label is matched
        against the localization reverse map and narrowed to the codes that sit
        directly under `parent_full_code` (one extra segment, no deeper). Only a
        single unambiguous match counts as an existing boundary.
        """
        key = self._localization_lookup_key(label)
        if not key or key == "nan" or not self.localization_reverse_map:
            return None

        candidates = self.localization_reverse_map.get(key, [])
        if not candidates:
            return None

        prefix = f"Boundary_{parent_full_code}_" if parent_full_code else "Boundary_"
        matches = [c for c in candidates if c.startswith(prefix) and "_" not in c[len(prefix):]]
        if not matches:
            return None
        if len(matches) > 1:
            logger.warning(f"Ambiguous boundary localization for '{label}' under '{parent_full_code}': {matches}; "
                           f"falling back to the generated code")
            return None

        return matches[0].replace("Boundary_", "", 1)

    def _effective_full_code(self, generated_code: str, label: str,
                             parent_full_code: Optional[str]) -> str:
        """Full code to use for one hierarchy level of a row.

        When the localized label already maps to a boundary under the parent
        code resolved for this row, that existing code is reused so children are
        attached to the boundary that already exists instead of to a new one
        generated from the sheet's spelling.
        """
        existing_code = self._resolve_code_by_localization(label, parent_full_code)
        if existing_code:
            logger.debug(f"Resolved existing boundary '{label}' under '{parent_full_code}' to '{existing_code}'")
            return existing_code
        return f"{parent_full_code}_{generated_code}" if parent_full_code else generated_code

    def _organize_boundary_data(self, boundary_df):
        """Organize boundary data into hierarchical structure with full codes.
        Keyed by full code, never by the bare leaf name: block and district names repeat
        across the country ("Sadar", "Bilaspur"), and keying by name would silently drop
        every repeat after the first - and attach the first one's LGD codes to it.
        """
        self._load_boundary_localizations()

        for index, row in boundary_df.iterrows():
            country = self.to_camel_case(str(row.get('Country', '')).strip())
            state = self.to_camel_case(str(row.get('State', '')).strip())
            district = self.to_camel_case(str(row.get('District', '')).strip())
            block = self.to_camel_case(str(row.get('Block', '')).strip())

            country_label = self.boundary_localization_label(row.get("Country"))
            state_label = self.boundary_localization_label(row.get("State"))
            district_label = self.boundary_localization_label(row.get("District"))
            block_label = self.boundary_localization_label(row.get("Block"))

            level_codes: Dict[str, str] = {}
            country_code = state_code = district_code = None

            # LGD identity columns (required by the schema).
            lgd_state = _read_identity_cell(row, LGD_STATE_CODE_COLUMN)
            lgd_district = _read_identity_cell(row, LGD_DISTRICT_CODE_COLUMN)
            lgd_block = _read_identity_cell(row, LGD_BLOCK_CODE_COLUMN)
            lgd_tier = _read_identity_cell(row, LGD_BLOCK_TIER_COLUMN)

            # Country level
            if country:
                country_code = self._effective_full_code(country, country_label or country, None)
                level_codes["Country"] = country_code
                self.all_boundary_full_codes.add(country_code)
                if country_code not in self.boundary_data["Country"]:
                    self.boundary_data["Country"][country_code] = {
                        "name": country,
                        "localization_label": country_label or country,
                        "parent": None,
                        "full_code": country_code,
                        "lgd": {},
                    }

            # State level
            if state and country_code:
                state_code = self._effective_full_code(state, state_label or state, country_code)
                level_codes["State"] = state_code
                self.all_boundary_full_codes.add(state_code)
                if state_code not in self.boundary_data["State"]:
                    self.boundary_data["State"][state_code] = {
                        "name": state,
                        "localization_label": state_label or state,
                        "parent": country_code,
                        "full_code": state_code,
                        "lgd": _lgd_details(stateCode=lgd_state),
                    }

            # District level
            if district and state_code:
                district_code = self._effective_full_code(district, district_label or district, state_code)
                level_codes["District"] = district_code
                self.all_boundary_full_codes.add(district_code)
                if district_code not in self.boundary_data["District"]:
                    self.boundary_data["District"][district_code] = {
                        "name": district,
                        "localization_label": district_label or district,
                        "parent": state_code,
                        "full_code": district_code,
                        "lgd": _lgd_details(stateCode=lgd_state, districtCode=lgd_district),
                    }

            # Block level
            if block and district_code:
                block_code = self._effective_full_code(block, block_label or block, district_code)
                level_codes["Block"] = block_code
                self.all_boundary_full_codes.add(block_code)
                if block_code not in self.boundary_data["Block"]:
                    self.boundary_data["Block"][block_code] = {
                        "name": block,
                        "localization_label": block_label or block,
                        "parent": district_code,
                        "full_code": block_code,
                        "lgd": _lgd_details(stateCode=lgd_state,
                                                                    districtCode=lgd_district,
                                                                    blockCode=lgd_block,
                                                                    blockTier=lgd_tier),
                    }

            self.row_level_codes[index] = level_codes

        # Log summary
        for level in self.hierarchy_levels:
            logger.info(f"Found {len(self.boundary_data[level])} unique {level} boundaries")

    def _check_existing_boundaries(self):
        """Check which boundaries already exist in the system using their full codes"""
        if not self.all_boundary_full_codes:
            return

        # Split into chunks to avoid too long URLs
        chunk_size = 50
        codes_list = list(self.all_boundary_full_codes)

        for i in range(0, len(codes_list), chunk_size):
            chunk = codes_list[i:i + chunk_size]

            try:
                response_data = self.boundary_service_client.search_boundaries(
                    request_info=self.request_info,
                    tenant_id="in",
                    codes=chunk
                )

                if response_data and "Boundary" in response_data:
                    for boundary in response_data["Boundary"]:
                        code = boundary["code"]
                        self.existing_boundaries.add(code)
                        self.existing_boundaries_casefold[code.casefold()] = code
            except Exception as e:
                logger.error(f"Error checking existing boundaries: {e}")

        logger.info(f"Found {len(self.existing_boundaries)} existing boundaries in the system")

    def _adopt_existing_codes(self) -> None:
        """Use an existing boundary's exact code where this run's code matches it only in
        letter case. boundary-service treats such codes as duplicates on create, but links a
        relationship only to the exact code - so creating would be skipped and the link would
        then fail ('Boundary entity does not exist'), taking every child below with it. A
        renamed parent also renames the codes built under it."""
        renamed: Dict[str, str] = {}
        for level in self.hierarchy_levels:
            adopted: Dict[str, Dict] = {}
            for data in self.boundary_data[level].values():
                old_code, old_parent = data["full_code"], data["parent"]
                parent = renamed.get(old_parent, old_parent) if old_parent else old_parent
                code = old_code
                if parent != old_parent and old_code.startswith(old_parent + "_"):
                    code = parent + old_code[len(old_parent):]
                code = self.existing_boundaries_casefold.get(code.casefold(), code)
                if code != old_code:
                    renamed[old_code] = code
                data["full_code"], data["parent"] = code, parent
                adopted.setdefault(code, data)
            self.boundary_data[level] = adopted
        if renamed:
            self.code_renames.update(renamed)
            self.all_boundary_full_codes = {renamed.get(c, c) for c in self.all_boundary_full_codes}
            logger.info(f"Using existing codes for {len(renamed)} boundaries that differ only in "
                        f"letter case, e.g. {next(iter(renamed.items()))}")
        for level_codes in self.row_level_codes.values():
            for level, code in list(level_codes.items()):
                level_codes[level] = self.code_renames.get(code, code)

    def _boundary_exists(self, full_code: str) -> bool:
        return full_code.casefold() in self.existing_boundaries_casefold

    def _mark_boundary_failure(self, full_code: str, error_message: str) -> None:
        self.failed_boundaries[full_code] = error_message

    def _create_new_boundaries(self):
        """Create new boundaries that don't already exist"""
        boundaries_to_create = []

        # Prepare boundary creation data for all levels
        for level in self.hierarchy_levels:
            for code, data in self.boundary_data[level].items():
                full_code = data["full_code"]
                if full_code in self.failed_boundaries:
                    continue
                if self._boundary_exists(full_code):
                    continue
                boundary = {
                    "tenantId": "in",
                    "code": full_code,
                    "geometry": None
                }
                # LGD identity goes into the boundary's own lgd_* columns, denormalised
                # down the hierarchy: a State carries only its state code, a District its
                # state and district, a Block all three. These are what a later import
                # reconciles on, since a boundary code is derived from the name and so
                # changes when the name does.
                lgd = data.get("lgd") or {}
                if lgd.get("stateCode"):
                    boundary["lgdStateCode"] = lgd["stateCode"]
                if lgd.get("districtCode"):
                    boundary["lgdDistrictCode"] = lgd["districtCode"]
                if lgd.get("blockCode"):
                    boundary["lgdBlockCode"] = lgd["blockCode"]
                    boundary["lgdBlockTier"] = lgd.get("blockTier") or "Block"
                boundaries_to_create.append(boundary)

        if not boundaries_to_create:
            logger.info("No new boundaries to create")
            return

        # Create boundaries in chunks
        chunk_size = 50
        for i in range(0, len(boundaries_to_create), chunk_size):
            chunk = boundaries_to_create[i:i + chunk_size]

            try:
                response_data, error_message = self.boundary_service_client.create_boundaries(
                    request_info=self.request_info,
                    boundary_data=chunk
                )

                if error_message:
                    for boundary in chunk:
                        self._mark_boundary_failure(boundary["code"], error_message)
                    continue

                created_boundaries = (response_data or {}).get("Boundary") or []
                if not created_boundaries:
                    for boundary in chunk:
                        self._mark_boundary_failure(
                            boundary["code"],
                            "Failed to create boundary (empty response from boundary service)",
                        )
                    continue

                created_codes = {b["code"] for b in created_boundaries if b.get("code")}
                for boundary in chunk:
                    if boundary["code"] not in created_codes:
                        self._mark_boundary_failure(
                            boundary["code"],
                            "Failed to create boundary (not returned by boundary service)",
                        )
                logger.info(f"Successfully created {len(created_boundaries)} boundaries")
            except Exception as e:
                for boundary in chunk:
                    self._mark_boundary_failure(boundary["code"], str(e))

        logger.info(f"Attempted to create {len(boundaries_to_create)} boundaries. "
                    f"Failed: {len(self.failed_boundaries)}")

    def _deepest_full_code_for_row(self, index) -> Optional[str]:
        level_codes = self.row_level_codes.get(index) or {}
        for level in reversed(self.hierarchy_levels):
            if level_codes.get(level):
                return level_codes[level]
        return None

    def _create_boundary_relationships(self):
        """Create boundary relationships in hierarchical order"""
        relationship_created_count = 0

        # Process relationships for each level
        for level in self.hierarchy_levels:
            for code, data in self.boundary_data[level].items():
                full_code = data["full_code"]
                parent_full_code = data["parent"]

                # Skip if boundary creation failed
                if full_code in self.failed_boundaries:
                    continue

                # Skip for country level (no parent)
                if level == "Country":
                    continue

                # Skip if parent creation failed
                if parent_full_code and parent_full_code in self.failed_boundaries:
                    self.failed_relationships[(full_code, level)] = f"Parent {parent_full_code} creation failed"
                    continue

                success, error = self._create_single_relationship(full_code, level, parent_full_code)
                if success:
                    relationship_created_count += 1
                elif error:
                    self.failed_relationships[(full_code, level)] = error

        logger.info(f"Successfully created {relationship_created_count} relationships. "
                    f"Failed: {len(self.failed_relationships)}")

    def _create_single_relationship(self, full_code, boundary_type, parent_full_code):
        """Create a single boundary relationship"""
        try:
            response_data = self.boundary_service_client.create_boundary_relationship(
                request_info=self.request_info,
                tenant_id="in",
                code=full_code,
                hierarchy_type="SELCO",
                boundary_type=boundary_type,
                parent=parent_full_code
            )

            if "Errors" in response_data:
                if any(error.get("code") == "DUPLICATE_RECORD" for error in response_data["Errors"]):
                    return True, None  # Relationship already exists
                else:
                    error_msg = ", ".join(error.get("message") for error in response_data["Errors"])
                    return False, error_msg
            return True, None

        except Exception as e:
            return False, str(e)

    def _upsert_localization_for_boundaries(self):
        """Upsert localization messages for all boundaries"""
        if not self.request_info:
            logger.warning("No RequestInfo; skipping localization upsert for boundaries")
            return
        messages = []
        for level in self.hierarchy_levels:
            for code, data in self.boundary_data[level].items():
                full_code = data["full_code"]
                if full_code in self.failed_boundaries:
                    continue
                # Human-readable label for localization (spaces preserved; leading/trailing stripped)
                raw_display_name = data.get("localization_label") or data.get("name") or code
                display_name = re.sub(r"\s+", " ", raw_display_name).strip()
                messages.append({
                    "code": f"Boundary_{full_code}",
                    "message": display_name,
                    "module": "rainmaker-in",
                    "locale": "en_IN",
                })
        if not messages:
            return
        chunk_size = 50
        for i in range(0, len(messages), chunk_size):
            chunk = messages[i : i + chunk_size]
            try:
                self.localization_client.upsert_messages(
                    request_info=self.request_info,
                    tenant_id="in",
                    messages=chunk,
                )
                logger.info(f"Upserted localization for {len(chunk)} boundaries")
            except Exception as e:
                logger.error(f"Localization upsert failed for boundary batch: {e}", exc_info=True)

    def _update_dataframe_with_results(self, boundary_df):
        """Update the DataFrame with boundary creation results"""
        for index, row in boundary_df.iterrows():
            status = row["status"]
            if not pd.isna(status) and str(status).strip().lower() not in ("", "none"):
                continue  # Keep validation failures and skipped rows as-is

            row_failed = False
            row_errors = []

            level_codes = self.row_level_codes.get(index) or {}

            deepest_code = self._deepest_full_code_for_row(index)
            if deepest_code and self._boundary_exists(deepest_code) and deepest_code not in self.failed_boundaries:
                existing_code = self.existing_boundaries_casefold[deepest_code.casefold()]
                row_failed = True
                row_errors.append(f"Boundary already exists: {existing_code}")

            # Check each level that exists in this row
            for level in self.hierarchy_levels:
                full_code = level_codes.get(level)
                if not full_code:
                    continue

                name = self.boundary_localization_label(row.get(level)) or full_code
                if full_code in self.failed_boundaries:
                    row_failed = True
                    row_errors.append(
                        f"Failed to create {level} '{name}': {self.failed_boundaries[full_code]}")
                elif (full_code, level) in self.failed_relationships:
                    row_failed = True
                    row_errors.append(
                        f"Failed relationship for {level} '{name}': {self.failed_relationships[(full_code, level)]}")

            if row_failed:
                boundary_df.loc[index, "status"] = "fail"
                boundary_df.loc[index, "error"] = ", ".join(row_errors)
            else:
                boundary_df.loc[index, "status"] = "success"
                boundary_df.loc[index, "error"] = ""

        return boundary_df

    def to_camel_case(self, text: str) -> str:
        if not text or not text.strip():
            return ""

        return boundary_code_segment(text)

    @staticmethod
    def boundary_localization_label(cell) -> str:
        """The name as typed, with ends trimmed and internal whitespace collapsed. Not
        re-cased: title-casing turned 'Chanditala-II' into 'Chanditala-ii' and 'G.Madugula'
        into 'G.madugula'."""
        if cell is None or (isinstance(cell, float) and pd.isna(cell)):
            return ""
        raw = str(cell).strip()
        if not raw:
            return ""
        return re.sub(r"\s+", " ", raw)