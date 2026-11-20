"""
Builds ticket_migration_mapping.json from Ticket Migration.xlsx.

The workbook maps old issue type / sub type labels to new ones, one sheet per reporter type:
  - sheet "CRM": columns A-D = Old Issue Type, Old Issue Sub Type, New Issue Type, New Sub Type
  - sheet "HCR": columns G-J = the same four columns (A-C hold the facility user list, ignored here)

An issue type is written only on the first row of its group (merged cells), so an empty type cell
takes the value of the row above. A "Remove" target means the tickets are left as they are.

Usage:
    python3 build_mapping.py "/path/to/Ticket Migration.xlsx" [output.json]
"""
import json
import os
import re
import sys

import openpyxl

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_OUTPUT = os.path.join(HERE, "ticket_migration_mapping.json")

# reporter type -> (sheet name, index of the "Old Issue Type" column; the next three follow it)
SHEETS = {"CRM": ("CRM", 0), "HCR": ("HCR", 6)}


def clean(value):
    if value is None:
        return ""
    return re.sub(r"\s+", " ", str(value)).strip()


def read_sheet(workbook, reporter_type, sheet_name, first_col):
    rows = []
    sheet = workbook[sheet_name]
    old_type = new_type = ""
    for row in sheet.iter_rows(min_row=2, values_only=True):
        cells = [clean(c) for c in row[first_col:first_col + 4]]
        cells += [""] * (4 - len(cells))
        if not any(cells):
            continue
        old_type = cells[0] or old_type
        new_type = cells[2] or new_type
        old_sub, new_sub = cells[1], cells[3]
        if not old_sub:
            continue
        entry = {
            "reporterType": reporter_type,
            "oldType": old_type,
            "oldSubType": old_sub,
            "newType": new_type,
            "newSubType": new_sub,
        }
        if new_type.lower() == "remove" or new_sub.lower() == "remove":
            entry["action"] = "skip"
        rows.append(entry)
    return rows


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    workbook = openpyxl.load_workbook(sys.argv[1], data_only=True)
    output = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_OUTPUT
    mapping = []
    for reporter_type, (sheet_name, first_col) in SHEETS.items():
        mapping += read_sheet(workbook, reporter_type, sheet_name, first_col)
    with open(output, "w") as f:
        json.dump(mapping, f, indent=2, ensure_ascii=False)
        f.write("\n")
    for reporter_type in SHEETS:
        rows = [m for m in mapping if m["reporterType"] == reporter_type]
        skipped = sum(1 for m in rows if m.get("action") == "skip")
        print(f"{reporter_type}: {len(rows)} rows ({skipped} Remove)")
    print(f"Written {output}")


if __name__ == "__main__":
    main()
