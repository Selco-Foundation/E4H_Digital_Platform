"""
Validate and convert an ICC (Installation Completion Certificate) report Excel file into a
flat JSON keyed by the {{placeholder}} field names used in the pdf-service BOM format-config
templates bundled under app/config/icc_templates/.

This mirrors the standalone icc_report_to_json.py script (built and verified against real
sample files for the "dc", "ac_off", and "hybrid" system types), reshaped into importable
functions for the /icc-reports endpoint: structural validation happens before conversion, and
conversion never guesses a System Type the caller didn't ask for - see validate_icc_report().

Ground truth for "Field Label -> JSON key" comes from the templates directly (they place
{{field_name}} placeholders right next to the same item labels used in the Excel form), not
from fuzzy-matching against an MDMS schema. See detect_system_type()'s docstring for how the
3 supported formats are told apart.
"""
import json
import os
import re
from collections import OrderedDict

from openpyxl import load_workbook
from openpyxl.utils.exceptions import InvalidFileException

_BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TEMPLATE_DIR = os.path.abspath(os.path.join(_BASE_DIR, "..", "config", "icc_templates"))

DATA_SHEET = "ICC Report"
MAP_SHEET = "Data_Ingestion_Map"
REQUIRED_SHEETS = (DATA_SHEET, MAP_SHEET)
REQUIRED_MAP_COLUMNS = ("Section", "Field Label", "Cell Address", "Row No.", "Column No.", "Expected Input Type")

# Public systemType values the /icc-reports endpoint accepts -> internal format key used by
# TEMPLATE_BY_TYPE/SFP_FIELD_MAPS/detect_system_type(). AC_ON_GRID has no approved template
# bundled yet (none of the pdf-service format-config templates cover it), so it intentionally
# maps to None - the endpoint rejects it with a clear "not yet supported" error rather than
# silently mis-converting it against the wrong template.
SYSTEM_TYPE_TO_INTERNAL = {
    "DC_OFF_GRID": "dc",
    "AC_OFF_GRID": "ac_off",
    "AC_HYBRID": "hybrid",
    "AC_ON_GRID": None,
}

TEMPLATE_BY_TYPE = {
    "dc": "bom_dc_system.json",
    "hybrid": "bom_hybrid_three.json",  # identical to bom_hybrid_single.json
    "ac_off": "bom_ac_off_three.json",  # cosmetically identical to bom_ac_off_single.json
}

SECTION_TEMPLATE_HEADERS = {
    "Bill Of Material (For Solar System)": "Bill Of Material(For Solar System)",
    "Bill of material (For Luminaries & Fans)": "Bill of material (For Luminaries & Fans)",
    "Bill of materials (For RMS)": "Bill of material (For RMS)",
    "Bill Of Material (For Load Wiring)": "Bill of material (For Load Wiring)",
}

SERIAL_RE = re.compile(r"^\d+[a-z]?$")
PLACEHOLDER_RE = re.compile(r"\{\{\s*([a-zA-Z0-9_]+)\s*\}\}")

LABEL_ROLE_SUFFIX_RE = re.compile(
    r"\s*-\s*(make|capacity|quantity|qty\.?|remarks|description(?:\s*&\s*make)?)\s*$",
    re.IGNORECASE,
)

NO_TEMPLATE_COVERAGE_PREFIXES = ("header", "image", "annexure")


class ICCValidationError(Exception):
    """Raised for any Validation 1 / Validation 2 failure - message is user-facing."""


def normalize_label(label):
    return re.sub(r"\s+", " ", str(label)).strip().lower().rstrip(":.")


TEMPLATE_LABEL_MERGE = {
    normalize_label("Battery rack with the following: 1. Acid absorbent mat"): "battery rack (merged)",
    normalize_label("Battery rack with the following: 2. Electrical Insulation mat (Minimum (0.4 kV)"): "battery rack (merged)",
}

SFP_FIELD_MAP_HYBRID = {
    (141, 3): "array_size_kwp",
    (141, 8): "array_no_modules",
    (141, 13): "array_no_strings",
    (141, 18): "array_no_modules_per_string",
    (141, 23): "array_each_module_wp",
    (148, 12): "ajb_off_voc_string_1",
    (149, 12): "ajb_off_voc_string_2",
    (150, 12): "ajb_off_voc_string_3",
    (151, 12): "ajb_off_voc_string_4",
    (152, 12): "ajb_off_voc_string_5",
    (153, 12): "ajb_off_voc_mv_anchor",
    (154, 12): "ajb_on_vmp_string_1", (154, 21): "ajb_on_imp_string_1",
    (155, 12): "ajb_on_vmp_string_2", (155, 21): "ajb_on_imp_string_2",
    (156, 12): "ajb_on_vmp_string_3", (156, 21): "ajb_on_imp_string_3",
    (157, 12): "ajb_on_vmp_string_4", (157, 21): "ajb_on_imp_string_4",
    (158, 12): "ajb_on_vmp_string_5", (158, 21): "ajb_on_imp_string_5",
    (159, 12): "ajb_on_vmp_mv_anchor", (159, 21): "ajb_on_imp_mv_anchor",
    (160, 12): "ajb_on_pmp_string_1",
    (161, 12): "ajb_on_pmp_string_2",
    (162, 12): "ajb_on_pmp_string_3",
    (163, 12): "ajb_on_pmp_string_4",
    (164, 12): "ajb_on_pmp_string_5",
    (165, 12): "ajb_on_pmp_array",
    (167, 4): "bbank_size_kwh",
    (167, 10): "bbank_capacity_ah",
    (167, 16): "bbank_no_batteries",
    (167, 22): "bbank_no_strings",
    (168, 4): "bbank_batteries_per_string",
    (168, 10): "bbank_each_capacity_ah",
    (168, 16): "bbank_each_voltage_v",
    (168, 22): "bbank_each_c_rating",
    (171, 17): "bb_voltage_mv",
    (172, 17): "bb_current_mv",
    (174, 7): "pcu_inverter_count",
    (174, 20): "charge_controller_count",
    (176, 20): "charge_controller_ratings",
    (181, 9): "ph1_load_voltage_mv", (181, 20): "ph1_grid_input_voltage_mv",
    (182, 9): "ph1_output_freq_mv", (182, 20): "ph1_grid_frequency_mv",
    (183, 9): "ph1_output_current_full_load_mv", (183, 20): "ph1_grid_current_mv",
    (187, 12): "ph3_off_load_inverter_voltage_rn_mv", (187, 20): "ph3_on_grid_voltage_rn_mv",
    (188, 12): "ph3_off_inverter_output_frequency_rn_mv", (188, 20): "ph3_on_grid_frequency_rn_mv",
    (189, 12): "ph3_off_inverter_full_load_current_rn_mv", (189, 20): "ph3_on_grid_current_rn_mv",
    (190, 12): "ph3_off_load_inverter_voltage_yn_mv", (190, 20): "ph3_on_grid_voltage_yn_mv",
    (191, 12): "ph3_off_inverter_output_frequency_yn_mv", (191, 20): "ph3_on_grid_frequency_yn_mv",
    (192, 12): "ph3_off_inverter_full_load_current_yn_mv", (192, 20): "ph3_on_grid_current_yn_mv",
    (193, 12): "ph3_off_load_inverter_voltage_bn_mv", (193, 20): "ph3_on_grid_voltage_bn_mv",
    (194, 12): "ph3_off_inverter_output_frequency_bn_mv", (194, 20): "ph3_on_grid_frequency_bn_mv",
    (195, 12): "ph3_off_inverter_full_load_current_bn_mv", (195, 20): "ph3_on_grid_current_bn_mv",
    (196, 12): "ph3_off_inverter_output_voltage_ry_mv", (196, 20): "ph3_on_grid_voltage_ry_mv",
    (197, 12): "ph3_off_inverter_output_frequency_ry_mv", (197, 20): "ph3_on_grid_frequency_ry_mv",
    (198, 12): "ph3_off_inverter_output_voltage_rb_mv", (198, 20): "ph3_on_grid_voltage_rb_mv",
    (199, 12): "ph3_off_inverter_output_frequency_rb_mv", (199, 20): "ph3_on_grid_frequency_rb_mv",
    (200, 12): "ph3_off_inverter_output_voltage_yb_mv", (200, 20): "ph3_on_grid_voltage_yb_mv",
    (201, 12): "ph3_off_inverter_output_frequency_yb_mv", (201, 20): "ph3_on_grid_frequency_yb_mv",
    (213, 13): "array_pcu_isolator_on_v", (213, 22): "array_pcu_isolator_off_v",
    (214, 13): "battery_pcu_isolator_on_v", (214, 22): "battery_pcu_isolator_off_v",
    (215, 13): "grid_pcu_isolator_on_v", (215, 22): "grid_pcu_isolator_off_v",
    (216, 13): "pcu_cos1_isolator_on_v", (216, 22): "pcu_cos1_isolator_off_v",
    (217, 13): "rccb_load_acdb_r_on_v", (217, 22): "rccb_load_acdb_r_off_v",
    (218, 13): "rccb_load_acdb_y_on_v", (218, 22): "rccb_load_acdb_y_off_v",
    (219, 13): "rccb_load_acdb_b_on_v", (219, 22): "rccb_load_acdb_b_off_v",
    (220, 13): "isolator_load_acdb_r_on_v", (220, 22): "isolator_load_acdb_r_off_v",
    (223, 13): "main_mcb_load_acdb_on_v", (223, 22): "main_mcb_load_acdb_off_v",
    (253, 18): "sock_v_pn_mv",
    (254, 18): "sock_v_pe_mv",
    (255, 18): "sock_v_en_mv",
    (259, 1): "ept_dc_earthing_mv",
    (259, 6): "ept_ac_earthing_mv",
    (259, 13): "ept_lightning_arrester1_mv",
    (259, 18): "ept_lightning_arrester2_mv",
    (262, 19): "dc_panel_panel_res",
    (263, 19): "dc_panel_mms_res",
    (264, 19): "dc_mms_ajb_res",
    (265, 19): "dc_ajb_busbar_res",
    (266, 19): "dc_battery_rack_busbar_res",
    (267, 19): "dc_isolator_box_busbar_res",
    (268, 19): "dc_busbar_earth_pit_res",
    (269, 19): "ac_pcu_busbar_res",
    (270, 19): "ac_changeover1_busbar_res",
    (271, 19): "ac_changeover2_busbar_res",
    (272, 19): "ac_gipb_busbar_res",
    (273, 19): "ac_load_acdb_busbar_res",
    (274, 19): "ac_busbar_earth_pit_res",
    (275, 19): "la1_la_aluminium_cable_res",
    (276, 19): "la1_aluminium_gi_strip_res",
    (277, 19): "la1_gi_strip_earth_pit_res",
    (278, 19): "la2_la_aluminium_cable_res",
    (279, 19): "la2_aluminium_gi_strip_res",
    (280, 19): "la2_gi_strip_earth_pit_res",
    (282, 19): "la1_la2_res",
    (285, 15): "orientation_operational_instructions_status",
    (286, 15): "orientation_routine_maintenance_status",
    (287, 15): "stickers_pasted_status",
    (288, 15): "posters_pasted_status",
    (289, 15): "orientation_warranty_solar_panel_status",
    (290, 15): "orientation_warranty_batteries_status",
    (291, 15): "orientation_warranty_pcu_status",
    (292, 15): "orientation_warranty_bidirectional_meter_status",
    (293, 15): "orientation_warranty_check_meter_status",
}

SFP_FIELD_MAP_AC_OFF = {
    (138, 1): "sfp_date",
    (139, 6): "sfp_weather",
    (141, 4): "array_size_kwp",
    (141, 10): "array_no_modules",
    (141, 14): "array_no_strings",
    (141, 19): "array_no_modules_per_string",
    (141, 23): "array_each_module_wp",
    (144, 8): "ajb_off_voc_string_1",
    (145, 8): "ajb_off_voc_string_2",
    (146, 8): "ajb_off_voc_string_3",
    (147, 8): "ajb_off_voc_string_4",
    (148, 8): "ajb_off_voc_string_5",
    (149, 8): "ajb_off_voc_mv_anchor",
    (150, 8): "ajb_on_vmp_string_1", (150, 18): "ajb_on_imp_string_1",
    (151, 8): "ajb_on_vmp_string_2", (151, 18): "ajb_on_imp_string_2",
    (152, 8): "ajb_on_vmp_string_3", (152, 18): "ajb_on_imp_string_3",
    (153, 8): "ajb_on_vmp_string_4", (153, 18): "ajb_on_imp_string_4",
    (154, 8): "ajb_on_vmp_string_5", (154, 18): "ajb_on_imp_string_5",
    (155, 8): "ajb_on_vmp_mv_anchor", (155, 18): "ajb_on_imp_mv_anchor",
    (156, 8): "ajb_on_pmp_string_1",
    (157, 8): "ajb_on_pmp_string_2",
    (158, 8): "ajb_on_pmp_string_3",
    (159, 8): "ajb_on_pmp_string_4",
    (160, 8): "ajb_on_pmp_string_5",
    (161, 8): "ajb_on_pmp_array",
    (164, 15): "bb_voltage_mv",
    (165, 15): "bb_current_mv",
    (169, 9): "ph1_load_voltage_mv", (169, 20): "ph1_grid_input_voltage_mv",
    (170, 9): "ph1_output_freq_mv", (170, 20): "ph1_inverter_output_frequency_mv",
    (171, 9): "ph1_output_current_full_load_mv", (171, 20): "ph1_grid_input_current_mv",
    (175, 12): "ph3_rpn_voltage_mv_off", (175, 20): "ph3_rpn_voltage_mv_on",
    (176, 12): "ph3_rpn_freq_mv_off", (176, 20): "ph3_rpn_freq_mv_on",
    (177, 12): "inv_full_i_rn", (177, 20): "ph3_ypn_voltage_mv_on",
    (178, 12): "ph3_ypn_voltage_mv_off", (178, 20): "ph3_ypn_freq_mv_on",
    (179, 12): "ph3_ypn_freq_mv_off", (179, 20): "ph3_bpn_voltage_mv_on",
    (180, 12): "inv_full_i_yn", (180, 20): "ph3_bpn_freq_mv_on",
    (181, 12): "ph3_bpn_voltage_mv_off", (181, 20): "grid_on_v_ry",
    (182, 12): "ph3_bpn_freq_mv_off", (182, 20): "grid_on_f_ry",
    (183, 12): "inv_full_i_bn", (183, 20): "grid_on_v_rb",
    (184, 12): "ph3_r_y_voltage_mv", (184, 20): "grid_on_f_rb",
    (185, 12): "ph3_r_b_voltage_mv", (185, 20): "grid_on_v_by",
    (186, 12): "ph3_y_b_voltage_mv", (186, 20): "grid_on_f_by",
    (189, 6): "disp_inverter_priority",
    (190, 6): "disp_grid_export",
    (191, 6): "disp_battery_settings_ah",
    (192, 6): "disp_load_running_on",
    (194, 12): "chg_switch_orientation",
    (195, 12): "chg_switch_functional",
    (198, 17): "sock_v_pn_mv",
    (199, 17): "sock_v_pe_mv",
    (200, 17): "sock_v_en_mv",
    (204, 4): "ept_dc_earthing_mv",
    (204, 9): "ept_ac_earthing_mv",
    (204, 14): "ept_lightning_arrester1_mv",
    (204, 19): "ept_lightning_arrester2_mv",
    (207, 13): "orientation_operational_instructions_status",
    (208, 13): "orientation_routine_maintenance_status",
    (209, 13): "stickers_pasted_status",
    (210, 13): "posters_pasted_status",
}

SFP_FIELD_MAP_DC = {
    (76, 1): "dc_date_of_recording",
    (76, 13): "dc_time_of_recording",
    (77, 6): "dc_weather_condition",
    (79, 4): "dc_array_size_kwp",
    (79, 10): "dc_array_no_modules",
    (79, 14): "dc_array_no_strings",
    (79, 19): "dc_array_modules_per_string",
    (79, 23): "dc_array_each_module_capacity_wp",
    (82, 8): "dc_on_vmp_rn_string1_mv",
    (83, 8): "dc_on_vmp_rn_string2_mv",
    (85, 8): "ajb_on_vmp_string_1", (85, 18): "dc_on_imp_rn_string1_mv",
    (86, 8): "ajb_on_vmp_string_2", (86, 18): "dc_on_imp_rn_string2_mv",
    (87, 18): "dc_on_imp_rn_array_mv",
    (93, 15): "bb_voltage_mv",
    (94, 15): "bb_current_mv",
    (97, 15): "dc_ccu_output_voltage_mv",
    (98, 15): "dc_ccu_output_current_full_load_mv",
    (101, 9): "dc_ccu_ldv_set_value",
    (101, 20): "dc_ccu_lrv_set_value",
    (102, 6): "dc_battery_type",
    (105, 13): "orientation_operational_instructions_status",
    (106, 13): "orientation_routine_maintenance_status",
    (107, 13): "stickers_pasted_status",
    (108, 13): "posters_pasted_status",
}

SFP_FIELD_MAPS = {
    "hybrid": SFP_FIELD_MAP_HYBRID,
    "ac_off": SFP_FIELD_MAP_AC_OFF,
    "dc": SFP_FIELD_MAP_DC,
}


def slugify(label):
    label = re.sub(r"\(([^)]*)\)", r" \1 ", label)
    label = label.replace("'", "").replace("’", "")
    label = label.lower()
    label = re.sub(r"[^a-z0-9]+", "_", label)
    label = re.sub(r"(?<=[a-z])(?=[0-9])", "_", label)
    label = re.sub(r"(?<=[0-9])(?=[a-z])", "_", label)
    return re.sub(r"_+", "_", label).strip("_")


def make_fallback_key(section, label, idx, cell_count):
    key = f"{slugify(section)}_{slugify(label)}"
    return key if cell_count == 1 else f"{key}_{idx + 1}"


def strip_label_role_suffix(label):
    return LABEL_ROLE_SUFFIX_RE.sub("", label).strip()


def cell_text(cell):
    if isinstance(cell, dict):
        return cell.get("text", "") or ""
    if isinstance(cell, list):
        return " ".join(cell_text(c) for c in cell)
    return "" if cell is None else str(cell)


def validate_icc_report(wb, requested_system_type):
    """Validation 1 (ICC Format Verification) + Validation 2 (System Type Matching).

    Raises ICCValidationError with a caller-facing message on any failure; returns the
    detected internal system type ("dc" / "ac_off" / "hybrid") on success.
    """
    internal_type = SYSTEM_TYPE_TO_INTERNAL.get(requested_system_type)
    if requested_system_type not in SYSTEM_TYPE_TO_INTERNAL:
        raise ICCValidationError(
            f"Unknown System Type '{requested_system_type}'. "
            f"Expected one of {list(SYSTEM_TYPE_TO_INTERNAL)}."
        )
    if internal_type is None:
        raise ICCValidationError(
            f"System Type '{requested_system_type}' is not yet supported - no approved ICC "
            f"template is available for it."
        )

    missing_sheets = [s for s in REQUIRED_SHEETS if s not in wb.sheetnames]
    if missing_sheets:
        raise ICCValidationError(
            f"Uploaded file structure does not match the expected ICC format: "
            f"missing sheet(s) {missing_sheets}."
        )

    ws_map = wb[MAP_SHEET]
    header_row = [str(c.value).strip() if c.value is not None else "" for c in next(ws_map.iter_rows(min_row=1, max_row=1))]
    missing_columns = [c for c in REQUIRED_MAP_COLUMNS if c not in header_row]
    if missing_columns:
        raise ICCValidationError(
            f"Uploaded file structure does not match the expected ICC format: "
            f"'{MAP_SHEET}' is missing required column(s) {missing_columns}."
        )

    instances = load_data_ingestion_map(wb)
    if not instances:
        raise ICCValidationError(
            f"Uploaded file structure does not match the expected ICC format: "
            f"'{MAP_SHEET}' has no data rows."
        )

    detected_type = detect_system_type(instances)
    if detected_type != internal_type:
        raise ICCValidationError(
            f"Uploaded template does not correspond to the selected System Type. "
            f"Selected: {requested_system_type}; uploaded template matches: "
            f"{_INTERNAL_TO_SYSTEM_TYPE.get(detected_type, detected_type)}."
        )

    return detected_type


_INTERNAL_TO_SYSTEM_TYPE = {v: k for k, v in SYSTEM_TYPE_TO_INTERNAL.items() if v}


def load_data_ingestion_map(wb):
    ws = wb[MAP_SHEET]
    rows = list(ws.iter_rows(min_row=2, values_only=True))
    instances = []
    for section, label, _addr, row_no, col_no, _etype in rows:
        if not section or row_no is None or col_no is None:
            continue
        base_label = strip_label_role_suffix(label)
        if (
            instances
            and instances[-1]["section"] == section
            and instances[-1]["label"] == base_label
            and instances[-1]["cells"][-1][0] == row_no
        ):
            instances[-1]["cells"].append((row_no, col_no))
        else:
            instances.append({"section": section, "label": base_label, "cells": [(row_no, col_no)]})
    return instances


def detect_system_type(instances):
    """See icc_report_to_json.py's detect_system_type() for the full rationale: phase
    (single/three) is not detected because it never changes which Excel cell maps to which
    key for any of the 3 supported formats."""
    labels_lower = [inst["label"].lower() for inst in instances]

    has_rms = any(inst["section"] == "Bill of materials (For RMS)" for inst in instances)
    if not has_rms:
        return "dc"

    is_hybrid = any("solar hybrid pcu" in l or l.strip() == "net meter" for l in labels_lower)
    return "hybrid" if is_hybrid else "ac_off"


def extract_section_rows(template, header_text):
    content = template["config"]["content"]
    rows = []
    collecting = False
    for node in content:
        if isinstance(node, dict) and node.get("style") == "header" and "text" in node:
            collecting = node["text"].strip() == header_text
        elif collecting and isinstance(node, dict) and "table" in node:
            rows.extend(node["table"].get("body", []))
    return rows


def extract_bom_entries(rows):
    entries = []
    for row in rows:
        texts = [cell_text(c).strip() for c in row]
        if len(texts) < 3 or not SERIAL_RE.match(texts[0]):
            continue
        label = texts[1]
        value_keys = [PLACEHOLDER_RE.findall(t) for t in texts[2:]]
        entries.append((label, value_keys))
    return entries


def run_length_group(items, key_fn):
    runs = []
    for item in items:
        k = key_fn(item)
        if runs and runs[-1][0] == k:
            runs[-1][1].append(item)
        else:
            runs.append((k, [item]))
    return runs


def zip_section(template_entries, excel_instances, section, unmatched):
    output = {}
    template_key_fn = lambda e: TEMPLATE_LABEL_MERGE.get(normalize_label(e[0]), normalize_label(e[0]))
    template_runs = run_length_group(template_entries, key_fn=template_key_fn)
    excel_runs = run_length_group(excel_instances, key_fn=lambda e: normalize_label(e["label"]))

    for (_t_label, t_items), (_e_label, e_items) in zip(template_runs, excel_runs):
        for i, (_, value_keys) in enumerate(t_items):
            excel_inst = e_items[i] if i < len(e_items) else e_items[-1]
            for col_idx, keys in enumerate(value_keys):
                if col_idx >= len(excel_inst["cells"]) or not keys:
                    continue
                row_no, col_no = excel_inst["cells"][col_idx]
                for key in keys:
                    output.setdefault(key, []).append((row_no, col_no))
        for extra in e_items[len(t_items):]:
            unmatched.append((section, extra["label"]))
    return output


def convert_icc_report(wb, detected_type):
    """Convert an already-validated workbook to the flat {{field_name}}: value JSON.

    Returns (data, fallback_fields, unmatched_fields) - fallback_fields/unmatched_fields are
    diagnostics (not errors): they list which cells used a slugified key instead of a real
    template field name, and which Excel fields had no corresponding template row.
    """
    ws = wb[DATA_SHEET]
    instances = load_data_ingestion_map(wb)

    template_path = os.path.join(TEMPLATE_DIR, TEMPLATE_BY_TYPE[detected_type])
    with open(template_path) as f:
        template = json.load(f)

    sfp_map = SFP_FIELD_MAPS.get(detected_type, {})

    by_section = OrderedDict()
    for inst in instances:
        by_section.setdefault(inst["section"], []).append(inst)

    output = {}
    unmatched_excel_fields = []
    fallback_fields = []

    for section, section_instances in by_section.items():
        header_text = SECTION_TEMPLATE_HEADERS.get(section)

        if not header_text and section.strip().lower().startswith(NO_TEMPLATE_COVERAGE_PREFIXES):
            for inst in section_instances:
                for idx, (row_no, col_no) in enumerate(inst["cells"]):
                    key = make_fallback_key(section, inst["label"], idx, len(inst["cells"]))
                    output[key] = ws.cell(row=row_no, column=col_no).value
                    fallback_fields.append((section, inst["label"], key))
            continue

        if not header_text:
            for inst in section_instances:
                for idx, (row_no, col_no) in enumerate(inst["cells"]):
                    key = sfp_map.get((row_no, col_no))
                    if key is None:
                        key = make_fallback_key(section, inst["label"], idx, len(inst["cells"]))
                        fallback_fields.append((section, inst["label"], key))
                    output[key] = ws.cell(row=row_no, column=col_no).value
            continue

        rows = extract_section_rows(template, header_text)
        template_entries = extract_bom_entries(rows)
        key_cells = zip_section(template_entries, section_instances, section, unmatched_excel_fields)
        for key, cell_list in key_cells.items():
            row_no, col_no = cell_list[-1]
            output[key] = ws.cell(row=row_no, column=col_no).value

    return output, fallback_fields, unmatched_excel_fields


def validate_and_convert(xlsx_path, requested_system_type):
    """Single entry point for the /icc-reports endpoint: raises ICCValidationError on any
    Validation 1/2 failure (nothing is saved in that case); otherwise returns
    (detected_type, data, fallback_fields, unmatched_fields)."""
    try:
        wb = load_workbook(xlsx_path, data_only=True)
    except InvalidFileException as exc:
        raise ICCValidationError(f"Uploaded file is not a valid Excel (.xlsx) file: {exc}") from exc

    detected_type = validate_icc_report(wb, requested_system_type)
    data, fallback_fields, unmatched_fields = convert_icc_report(wb, detected_type)
    return detected_type, data, fallback_fields, unmatched_fields
