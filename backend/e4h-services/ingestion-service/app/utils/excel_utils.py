from typing import Dict, List, Union

import pandas as pd
from openpyxl import load_workbook
from openpyxl.comments import Comment
from openpyxl.styles import Protection
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.datavalidation import DataValidation

def add_dropdowns_to_excel(
        file_path: str,
        sheet_name: str,
        dropdowns: Dict[str, List[str]],
        allow_blank: bool = True,
        max_extra_rows: int = 1000
):
    wb = load_workbook(file_path)
    ws = wb[sheet_name]
    header_row = 1
    max_row = ws.max_row + max_extra_rows  # extend range

    for column_header, options in dropdowns.items():
        if not options:
            continue
        options_str = ",".join(options)
        dv = DataValidation(type="list", formula1=f'"{options_str}"', allow_blank=allow_blank)
        dv.error = 'Please select from the list'
        dv.errorTitle = 'Invalid Entry'
        for cell in ws[header_row]:
            if cell.value == column_header:
                col_letter = cell.column_letter
                dv.add(f"{col_letter}2:{col_letter}{max_row}")
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


def lock_prefilled_rows_in_excel(
    file_path: str,
    sheet_name: str,
    editable_columns: list,
    total_rows: int,
    total_columns: int,
    extra_append_rows: int = 1000
):
    wb = load_workbook(file_path)
    ws = wb[sheet_name]

    # Unlock all cells first
    for row in ws.iter_rows():
        for cell in row:
            cell.protection = Protection(locked=False)

    # Find editable column indices
    header_row = [cell.value for cell in ws[1]]
    editable_indices = [
        i + 1 for i, col in enumerate(header_row)
        if col and any(c in col for c in editable_columns)
    ]

    # Lock prefilled rows completely
    for row_idx in range(2, total_rows + 2):
        for col_idx in range(1, total_columns + 1):
            ws.cell(row=row_idx, column=col_idx).protection = Protection(locked=True)

    # Unlock editable cols in prefilled rows
    for row_idx in range(2, total_rows + 2):
        for col_idx in editable_indices:
            ws.cell(row=row_idx, column=col_idx).protection = Protection(locked=False)

    # Leave appendable rows fully unlocked
    for row_idx in range(total_rows + 2, total_rows + extra_append_rows + 2):
        for col_idx in range(1, total_columns + 1):
            ws.cell(row=row_idx, column=col_idx).protection = Protection(locked=False)

    # Enable protection
    ws.protection.sheet = True
    ws.protection.select_unlocked_cells = True
    wb.save(file_path)


def add_validations_to_excel(file_path: str, sheet_name: str, validations: Dict[str, Dict[str, str]]):
    """
    Adds Excel data validation (pattern + uniqueness) to specified columns.
    :param file_path: Excel file path
    :param sheet_name: Sheet where validation needs to be added
    :param validations: Dict[column_name] = {"type": "regex"/"unique", "pattern"/"message": str}
    """
    wb = load_workbook(file_path)
    ws = wb[sheet_name]

    header_row = 1
    max_row = ws.max_row + 1000  # allow future rows for data entry
    header_cells = {cell.value.strip(): cell for cell in ws[header_row] if cell.value}

    for col_name, config in validations.items():
        header_cell = header_cells.get(col_name)
        if not header_cell:
            continue

        col_idx = header_cell.column
        col_letter = get_column_letter(col_idx)
        data_range = f"{col_letter}2:{col_letter}{max_row}"

        if config["type"] == "regex":
            pattern = config["pattern"]
            # Handle known simple patterns
            if pattern == "^\\d{10}$":  # 10-digit number validation
                formula = f'AND(ISNUMBER({col_letter}2),LEN({col_letter}2)=10)'
                dv = DataValidation(type="custom", formula1=formula,
                                    showErrorMessage=True, error=config.get("message", "Invalid value"))
                ws.add_data_validation(dv)
                dv.add(data_range)
            else:
                # For unknown/complex regex, just add a comment instead of hard validation
                for cell in ws[data_range]:
                    for c in cell:
                        c.comment = Comment(f"Validation: {config['message']}", "System")

        elif config["type"] == "unique":
            # Enforce uniqueness using COUNTIF formula
            formula = f'COUNTIF(${col_letter}:${col_letter},{col_letter}2)=1'
            dv_unique = DataValidation(type="custom", formula1=formula,
                                       showErrorMessage=True, error=config.get("message", "Must be unique"))
            ws.add_data_validation(dv_unique)
            dv_unique.add(data_range)

    wb.save(file_path)