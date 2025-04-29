from typing import List

import pandas as pd

from app.core.logging import AppLogger
from app.ingest.boundary_excel_data_loader import BoundaryExcelDataLoader
from app.ingest.facility_template_service import boundary_service_url
from app.ingest.service.data_loader import DataLoader
from app.ingest.service.data_writer import DataWriter
from app.ingest.service.validator import Validator
from app.schemas.request_info import RequestInfo
from app.utils.boundary_service_client import BoundaryServiceClient

logger = AppLogger().get_logger()
class BoundaryDataProcessor:
    def __init__(self, data_loader: DataLoader, validators: List[Validator], data_writer: DataWriter,
                 request_info: RequestInfo = None):
        self.data_loader = data_loader
        self.validators = validators
        self.data_writer = data_writer
        self.validation_errors = []
        self.request_info = request_info
        self.boundary_service_client = BoundaryServiceClient(boundary_service_url)

        # Hierarchical structure to store boundary data
        self.hierarchy_levels = ["Country", "State", "District", "Block"]
        self.boundary_data = {
            "Country": {},  # {code: {name: name, children: {child_code: child_type}}}
            "State": {},
            "District": {},
            "Block": {}
        }

        # Sets to track unique boundary codes at each level
        self.boundary_codes = {
            "Country": set(),
            "State": set(),
            "District": set(),
            "Block": set()
        }

        # Track which boundaries already exist in the system
        self.existing_boundaries = set()

    def process_data(self):
        """Process and validate boundary data"""
        # Load data
        if not self.data_loader.load_data():
            return pd.DataFrame()

        if isinstance(self.data_loader, BoundaryExcelDataLoader):
            boundary_df = self.data_loader.get_boundary_data()
            logger.info(boundary_df.head(2))
        else:
            logger.warning("Data loader is not compatible")
            return pd.DataFrame()

        # Run all validators
        has_error = False
        for validator in self.validators:
            boundary_df = validator.validate(boundary_df)
            if (boundary_df["status"] == "fail").any():
                has_error = True

        # Collect validation errors
        self.validation_errors = []
        for idx, row in boundary_df[boundary_df["status"] == "fail"].iterrows():
            self.validation_errors.append({
                'row': idx + 2,  # +2 for Excel row numbers (header + 1-based)
                'boundary_code': row.get('Country', 'Unknown'),
                'errors': [row.get('error', '')]
            })

        # Write results back to Excel
        # self.data_writer.write_data(boundary_df)

        # Process valid boundaries
        valid_boundaries_df = boundary_df[boundary_df["status"] != "fail"]
        self._organize_boundary_data(valid_boundaries_df)
        self._check_existing_boundaries()
        self._create_new_boundaries()
        self._create_boundary_relationships()

        # Mark rows as failed based on failed boundaries and relationships
        for index, row in boundary_df.iterrows():
            if row["status"] != "fail":  # Only check rows that weren't failed in initial validation
                row_failed = False
                row_errors = []

                country = str(row.get('Country', '')).strip()
                state = str(row.get('State', '')).strip()
                district = str(row.get('District', '')).strip()
                block = str(row.get('Block', '')).strip()
                codes_in_row = [country, state, district, block]
                levels_in_row = self.hierarchy_levels

                for level, code in zip(levels_in_row, codes_in_row):
                    if code and code in self.failed_boundaries:
                        row_failed = True
                        row_errors.append(f"Failed to create {level} '{code}': {self.failed_boundaries[code]}")
                    if code and (code, level) in self.failed_relationships:
                        row_failed = True
                        row_errors.append(
                            f"Failed relationship for {level} '{code}': {self.failed_relationships[(code, level)]}")

                if row_failed:
                    boundary_df.loc[index, "status"] = "fail"
                    boundary_df.loc[index, "error"] = ", ".join(row_errors)
                else:
                    boundary_df.loc[index, "status"] = "success"
                    boundary_df.loc[index, "error"] = ""

        # self.data_writer.write_data(boundary_df)
        return boundary_df

    def _organize_boundary_data(self, boundary_df):
        """Organize boundary data into a hierarchical structure"""
        for _, row in boundary_df.iterrows():
            country = str(row.get('Country', '')).strip()
            state = str(row.get('State', '')).strip()
            district = str(row.get('District', '')).strip()
            block = str(row.get('Block', '')).strip()

            # Store each boundary code in the appropriate set
            if country:
                self.boundary_codes["Country"].add(country)
                if country not in self.boundary_data["Country"]:
                    self.boundary_data["Country"][country] = {"name": country, "parent": None}

            if state:
                self.boundary_codes["State"].add(state)
                if state not in self.boundary_data["State"]:
                    self.boundary_data["State"][state] = {"name": state, "parent": country}

            if district:
                self.boundary_codes["District"].add(district)
                if district not in self.boundary_data["District"]:
                    self.boundary_data["District"][district] = {"name": district, "parent": state}

            if block:
                self.boundary_codes["Block"].add(block)
                if block not in self.boundary_data["Block"]:
                    self.boundary_data["Block"][block] = {"name": block, "parent": district}

        # Print summary
        for level in self.hierarchy_levels:
            logger.info(f"Found {len(self.boundary_codes[level])} unique {level} boundaries")

    def _check_existing_boundaries(self):
        """Check which boundaries already exist in the system"""
        # Combine all boundary codes
        all_codes = []
        for level in self.hierarchy_levels:
            all_codes.extend(list(self.boundary_codes[level]))

        # Split into chunks of 50 to avoid too long URLs
        chunk_size = 50
        for i in range(0, len(all_codes), chunk_size):
            chunk = all_codes[i:i + chunk_size]

            try:
                response_data = self.boundary_service_client.search_boundaries(
                    request_info=self.request_info,
                    tenant_id="in",
                    codes=chunk
                )

                if response_data and "Boundary" in response_data:
                    for boundary in response_data["Boundary"]:
                        self.existing_boundaries.add(boundary["code"])
            except Exception as e:
                logger.error(f"Error checking existing boundaries: {e}")

        logger.info(f"Found {len(self.existing_boundaries)} existing boundaries in the system")

    def _create_new_boundaries(self):
        """Create new boundaries that don't already exist"""
        boundaries_to_create = []
        created_count = 0
        self.failed_boundaries = {}  # {code: error_message}

        for level in self.hierarchy_levels:
            for code in self.boundary_codes[level]:
                if code not in self.existing_boundaries and code not in self.failed_boundaries:
                    boundaries_to_create.append({
                        "tenantId": "in",
                        "code": code,
                        "geometry": None
                    })

        if not boundaries_to_create:
            logger.info("No new boundaries to create")
            return

        chunk_size = 50
        for i in range(0, len(boundaries_to_create), chunk_size):
            chunk = boundaries_to_create[i:i + chunk_size]

            try:
                response_data = self.boundary_service_client.create_boundaries(
                    request_info=self.request_info,
                    boundary_data=chunk
                )
                if response_data and "Boundary" in response_data:
                    created_count += len(chunk)
                else:
                    logger.warning("Failed to create boundaries")
            except Exception as e:
                for boundary in chunk:
                    if boundary["code"] not in self.failed_boundaries:
                        self.failed_boundaries[boundary["code"]] = str(e)

        logger.info(f"Attempted to create {len(boundaries_to_create)} new boundaries. "
                    f"Successfully created: {created_count}, Failed: {len(self.failed_boundaries)}")
        if self.failed_boundaries:
            logger.error(f"Failed boundary creations: {self.failed_boundaries}")

    def _create_boundary_relationships(self):
        """Create boundary relationships in hierarchical order"""
        relationship_created_count = 0
        self.failed_relationships = {}  # {(code, boundary_type): error_message}

        for level in self.hierarchy_levels:
            for code, data in self.boundary_data[level].items():
                parent_code = data.get("parent")
                success, error = self._create_single_relationship(code, level, parent_code)
                if success:
                    relationship_created_count += 1
                elif error:
                    self.failed_relationships[(code, level)] = error

        logger.info(
            f"Attempted to create {sum(len(self.boundary_data[level]) for level in self.hierarchy_levels)} relationships. "
            f"Successfully created: {relationship_created_count}, Failed: {len(self.failed_relationships)}")
        if self.failed_relationships:
            logger.error(f"Failed relationship creations: {self.failed_relationships}")

    def _create_single_relationship(self, code, boundary_type, parent_code):
        """Create a single boundary relationship"""
        try:
            response_data = self.boundary_service_client.create_boundary_relationship(
                request_info=self.request_info,
                tenant_id="in",
                code=code,
                hierarchy_type="SELCO",
                boundary_type=boundary_type,
                parent=parent_code
            )

            if "Errors" in response_data and any(
                    error.get("code") == "DUPLICATE_RECORD" for error in response_data["Errors"]
            ):
                logger.info(f"Relationship for {boundary_type} {code} already exists")
                return True, None  # Consider as success
            elif "Errors" in response_data:
                error_messages = [error.get("message") for error in response_data["Errors"]]
                logger.error(f"Error creating relationship for {boundary_type} {code}: {error_messages}")
                return False, ", ".join(error_messages)
            else:
                logger.info(f"Successfully created relationship for {boundary_type} {code}")
                return True, None

        except Exception as e:
            logger.error(f"Error creating relationship for {boundary_type} {code}: {e}")
            return False, str(e)
