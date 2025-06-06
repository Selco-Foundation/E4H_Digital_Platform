from typing import Dict, List, Union

import pandas as pd
from openpyxl import load_workbook
from openpyxl.styles import Protection
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.datavalidation import DataValidation

def add_dropdowns_to_excel(
        file_path: str,
        sheet_name: str,
        dropdowns: Dict[str, List[str]],
        allow_blank: bool = True
):
    """
    Adds dropdowns to multiple columns in an Excel sheet.

    Args:
        file_path: Path to the Excel file.
        sheet_name: Name of the sheet to modify.
        dropdowns: Dictionary where keys are column headers and values are lists of dropdown options.
        allow_blank: Whether to allow blank values in the dropdown.
    """
    wb = load_workbook(file_path)
    ws = wb[sheet_name]

    for column_header, options in dropdowns.items():
        if not options:
            continue
        options_str = ",".join(options)
        dv = DataValidation(type="list", formula1=f'"{options_str}"', allow_blank=allow_blank)
        dv.error = 'Please select from the list'
        dv.errorTitle = 'Invalid Entry'
        for cell in ws[1]:
            if cell.value == column_header:
                col_letter = cell.column_letter
                dv.ranges.add(f"{col_letter}2:{col_letter}1048576")
                break
        ws.add_data_validation(dv)

    wb.save(file_path)


def lock_excel_columns(
        file_path: str,
        sheet_name: str,
        column_headers_to_unlock: List[Union[str, int]]
) -> None:
    """
    Locks the entire Excel sheet except the specified columns by header name or index.

    Args:
        file_path: Path to the Excel file.
        sheet_name: Name of the sheet to modify.
        column_headers_to_unlock: List of column headers (e.g., 'Selection?') or 1-based column indices to keep editable.
    """
    wb = load_workbook(file_path)
    ws = wb[sheet_name]

    # Find column indices to unlock based on header names or 1-based indices
    column_indices_to_unlock = set()
    for identifier in column_headers_to_unlock:
        if isinstance(identifier, str):
            for col_index, cell in enumerate(ws[1], 1):  # Header is assumed to be in the first row
                if cell.value and str(cell.value).strip() == identifier.strip():
                    column_indices_to_unlock.add(col_index)
                    break
        elif isinstance(identifier, int):
            column_indices_to_unlock.add(identifier)

    max_rows = ws.max_row

    # Lock all cells by default
    for row in range(1, max_rows + 1):
        for col in range(1, ws.max_column + 1):
            ws.cell(row=row, column=col).protection = Protection(locked=True)

    # Unlock only the specified columns
    for col_index in column_indices_to_unlock:
        for row in range(1, max_rows + 1):
            ws.cell(row=row, column=col_index).protection = Protection(locked=False)

    # Enable worksheet protection and allow selection of unlocked cells
    ws.protection.sheet = True

    wb.save(file_path)