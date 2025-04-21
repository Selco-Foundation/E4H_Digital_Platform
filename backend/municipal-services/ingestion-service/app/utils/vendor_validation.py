import pandas as pd


def validate_mandatory_fields(row):
    """
    Check if all mandatory fields are filled

    Args:
        row: DataFrame row containing vendor data

    Returns:
        List of validation errors for this row
    """
    errors = []
    mandatory_fields = [
        'Country Boundary Code', 'Vendor Name (Mandatory)', 'Vendor Code (Mandatory)', 'Vendor Type (Mandatory)',
        'Identifier Type (Mandatory)', 'Identifier Value (Mandatory)', 'HQ Address (Mandatory)', 'Pincode (Mandatory)',
        'PoC Phone (Mandatory)', 'PoC Name (Mandatory)'
    ]

    for field in mandatory_fields:
        if pd.isna(row[field]) or str(row[field]).strip() == '':
            errors.append(f"Missing mandatory field: {field}")
    return errors


def validate_boundary_code(self, boundary_code):
    """Validate that the boundary code exists in the boundary code sheet"""
    if boundary_code not in self.valid_boundary_codes:
        return f"Invalid boundary code: {boundary_code}"
    return None
