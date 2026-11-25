"""The facility Bulk Add error file.

Both stages (validate and create) hand back the uploaded workbook itself, edited in place with
openpyxl, so its dropdowns, hidden columns and other sheets survive. When rows fail, the workbook
keeps only those rows, each with its reason in the ``status`` / ``error`` columns, and can be
corrected and uploaded again as a new batch. Rows that went through are not in it, so they can't
be added twice; uploading the original file again fails them as duplicates in facility-service.
"""
from typing import Dict, List, Tuple

import pandas as pd
from openpyxl.styles import Font
from openpyxl.workbook import Workbook

STATUS_COLUMN = "status"
ERROR_COLUMN = "error"
FIRST_DATA_ROW = 2


def reset_status_columns(df: pd.DataFrame) -> pd.DataFrame:
    """Every upload is a new batch: a status or error carried over from an earlier pass (a
    re-uploaded error file) is ignored, so a corrected row is judged afresh."""
    df[STATUS_COLUMN] = ""
    df[ERROR_COLUMN] = ""
    df[STATUS_COLUMN] = df[STATUS_COLUMN].astype(object)
    df[ERROR_COLUMN] = df[ERROR_COLUMN].astype(object)
    return df


def _status_columns(ws) -> Tuple[int, int]:
    """Column numbers of status and error, adding the headers when the sheet has none."""
    headers = [cell.value for cell in ws[1]]
    positions = {}
    for name in (STATUS_COLUMN, ERROR_COLUMN):
        if name in headers:
            positions[name] = headers.index(name) + 1
        else:
            headers.append(name)
            positions[name] = len(headers)
            ws.cell(row=1, column=positions[name], value=name).font = Font(bold=True)
    return positions[STATUS_COLUMN], positions[ERROR_COLUMN]


def _runs(rows: List[int]) -> List[Tuple[int, int]]:
    """Consecutive row numbers grouped into (first row, count), last run first."""
    runs = []
    for row in sorted(rows):
        if runs and runs[-1][0] + runs[-1][1] == row:
            runs[-1] = (runs[-1][0], runs[-1][1] + 1)
        else:
            runs.append((row, 1))
    return list(reversed(runs))


def keep_failed_rows_only(wb: Workbook, sheet_name: str, data_row_count: int,
                          failures: Dict[int, str], failed_status: str) -> None:
    """Writes each failed row's status and reason, and deletes every other data row.

    `failures` maps the DataFrame index of each failed row (read with pandas from the same sheet,
    so row ``i`` is sheet row ``i + 2``) to its reason.
    """
    ws = wb[sheet_name]
    status_col, error_col = _status_columns(ws)
    last_row = max(ws.max_row, data_row_count + FIRST_DATA_ROW - 1)
    keep = set()
    for index, reason in failures.items():
        row = index + FIRST_DATA_ROW
        keep.add(row)
        ws.cell(row=row, column=status_col, value=failed_status)
        ws.cell(row=row, column=error_col, value=reason)
    # Bottom-up, in runs, so each deletion shifts as little as possible.
    for first, count in _runs([row for row in range(FIRST_DATA_ROW, last_row + 1) if row not in keep]):
        ws.delete_rows(first, count)


def clear_status_columns(wb: Workbook, sheet_name: str) -> None:
    """Blanks status and error in every row, for a workbook going on to the create stage clean."""
    ws = wb[sheet_name]
    headers = [cell.value for cell in ws[1]]
    for name in (STATUS_COLUMN, ERROR_COLUMN):
        if name in headers:
            column = headers.index(name) + 1
            for row in range(FIRST_DATA_ROW, ws.max_row + 1):
                # Assigned, not passed to ws.cell(): openpyxl ignores value=None there.
                ws.cell(row=row, column=column).value = None
