from typing import Dict, Any

from app.schemas.column_schema import ColumnSchema
from app.schemas.mdms_data_response import MDMSDataResponse


def transform_mdms_data(mdms_data: MDMSDataResponse, column: ColumnSchema) -> Dict[str, Any]:
    # Implementation depends on the actual structure of MDMS data
    # This is a placeholder function
    values = []
    if column.mdmsSource and column.mdmsSource.module in mdms_data.MdmsRes:
        module_data = mdms_data.MdmsRes[column.mdmsSource.module]
        if column.mdmsSource.master in module_data:
            master_data = module_data[column.mdmsSource.master]
            if column.mdmsSource.valueAttribute:
                values = [item.get(column.mdmsSource.valueAttribute) for item in master_data]
            else:
                values = master_data
    return {"values": values}