from dataclasses import dataclass
from typing import List, Dict, Any

from app.schemas.vendor_ingestion_shema_response import MDMSColumn


@dataclass
class FacilityIngestionSchemaResponse:
    columns: List[MDMSColumn]

    @classmethod
    def from_mdms_response(cls, mdms_response: Dict[str, Any]) -> "FacilityIngestionSchemaResponse":
        """Create an instance from the MDMS service response"""
        master_data = mdms_response.get("MdmsRes", {}).get("data-ingestion", {}).get("FacilityIngestionSchema", [])

        column_schemas = []
        if master_data and len(master_data) > 0:
            schema_columns = master_data[0].get("columns", [])
            for column in schema_columns:
                if isinstance(column, dict):
                    column_schemas.append(MDMSColumn(
                        name=column.get("name", ""),
                        description=column.get("description"),
                        required=column.get("required", False),
                        dataType=column.get("dataType", "string")
                    ))
                elif isinstance(column, str):
                    column_schemas.append(MDMSColumn(name=column))

        return cls(columns=column_schemas)

    def get_column_names(self) -> List[str]:
        """Get list of column names for Excel sheet"""
        return [col.name for col in self.columns]

    def get_required_columns(self) -> List[str]:
        """Get list of required column names"""
        return [col.name for col in self.columns if col.required]