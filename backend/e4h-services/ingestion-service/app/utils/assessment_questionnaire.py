"""On Site / On Remote questionnaire columns of the assessment plan include sheet.

facility.CategoryAssessmentForm lists, per facility category, the assessment forms it may use
(e.g. HEALTH -> HF_PHONE, HF_FIELD). A form whose code ends in _FIELD is an on-site
questionnaire, one ending in _PHONE a remote one. The sheet shows each form by the `label` of its
assessment.AssessmentMobileFormSchema record, and the upload sends back the form's
uniqueIdentifier.
"""
from typing import Any, Dict, Iterable, List, Optional

import pandas as pd

from app.core.logging import AppLogger

logger = AppLogger().get_logger()

CATEGORY_ASSESSMENT_FORM_SCHEMA = "facility.CategoryAssessmentForm"
ASSESSMENT_MOBILE_FORM_SCHEMA = "assessment.AssessmentMobileFormSchema"
FACILITY_CATEGORY_SCHEMA = "facility.FacilityCategory"

MODE_SITE = "site"
MODE_REMOTE = "remote"
_MODE_SUFFIXES = {MODE_SITE: "_FIELD", MODE_REMOTE: "_PHONE"}

ON_SITE_QUESTIONNAIRE_HEADER = "On Site Questionnaire (Mandatory)"
ON_REMOTE_QUESTIONNAIRE_HEADER = "On Remote Questionnaire (Mandatory)"
QUESTIONNAIRE_HEADERS = {MODE_SITE: ON_SITE_QUESTIONNAIRE_HEADER, MODE_REMOTE: ON_REMOTE_QUESTIONNAIRE_HEADER}
# Keys the chosen forms are sent under to field-planner, which keeps them in additionalDetails.
QUESTIONNAIRE_PAYLOAD_KEYS = {MODE_SITE: "onSiteQuestionnaire", MODE_REMOTE: "onRemoteQuestionnaire"}
_HEADER_FRAGMENTS = {MODE_SITE: "on site questionnaire", MODE_REMOTE: "on remote questionnaire"}


def questionnaire_mode(form_code: str) -> Optional[str]:
    code = (form_code or "").strip().upper()
    for mode, suffix in _MODE_SUFFIXES.items():
        if code.endswith(suffix):
            return mode
    return None


def _cell_text(value: Any) -> str:
    if value is None or (not isinstance(value, str) and pd.isna(value)):
        return ""
    return str(value).strip()


class QuestionnaireOptions:
    """Questionnaires each facility category may pick, as {category: {mode: {label: form code}}}."""

    def __init__(self, options: Dict[str, Dict[str, Dict[str, str]]], category_codes: Dict[str, str]):
        self.options = options
        # Category name or code, lower-cased -> category code, so a sheet showing either resolves.
        self._category_codes = category_codes

    def category_code(self, value: Any) -> str:
        text = _cell_text(value)
        return self._category_codes.get(text.lower(), text.upper())

    def labels(self, mode: str, categories: Iterable[str]) -> List[str]:
        """Dropdown values for a column: every label the given categories may pick, in MDMS order."""
        labels: List[str] = []
        for category in categories:
            for label in self.options.get(self.category_code(category), {}).get(mode, {}):
                if label not in labels:
                    labels.append(label)
        return labels

    def form_code(self, category: Any, mode: str, label: Any) -> Optional[str]:
        return self.options.get(self.category_code(category), {}).get(mode, {}).get(_cell_text(label))


def load_questionnaire_options(mdms_client, request_info) -> QuestionnaireOptions:
    category_forms: Dict[str, List[str]] = {}
    for record in mdms_client.fetch_mdms_records(request_info, CATEGORY_ASSESSMENT_FORM_SCHEMA):
        category = _cell_text(record.get("category")).upper()
        if category:
            category_forms[category] = [
                _cell_text(code) for code in (record.get("assessment_code_form") or []) if _cell_text(code)
            ]

    all_codes = sorted({code for codes in category_forms.values() for code in codes})
    form_labels: Dict[str, str] = {}
    if all_codes:
        for entry in mdms_client.fetch_active_mdms_entries(
                request_info, ASSESSMENT_MOBILE_FORM_SCHEMA, unique_identifiers=all_codes):
            label = _cell_text(entry.data.model_dump().get("label"))
            if not label:
                logger.warning(f"Assessment form {entry.uniqueIdentifier} has no label; showing its code instead")
                label = entry.uniqueIdentifier
            form_labels[entry.uniqueIdentifier] = label

    options: Dict[str, Dict[str, Dict[str, str]]] = {}
    for category, codes in category_forms.items():
        by_mode: Dict[str, Dict[str, str]] = {MODE_SITE: {}, MODE_REMOTE: {}}
        for code in codes:
            mode = questionnaire_mode(code)
            label = form_labels.get(code)
            if not mode or not label:
                logger.warning(f"Assessment form {code} of category {category} skipped: "
                               f"{'no active form schema' if mode else 'code does not end in _PHONE or _FIELD'}")
                continue
            if label in by_mode[mode]:
                logger.warning(f"Assessment forms {by_mode[mode][label]} and {code} share the label "
                               f"'{label}' for category {category}; keeping {by_mode[mode][label]}")
                continue
            by_mode[mode][label] = code
        options[category] = by_mode

    category_codes: Dict[str, str] = {}
    for record in mdms_client.fetch_mdms_records(request_info, FACILITY_CATEGORY_SCHEMA):
        code = _cell_text(record.get("code"))
        if not code:
            continue
        category_codes[code.lower()] = code.upper()
        name = _cell_text(record.get("name"))
        if name:
            category_codes[name.lower()] = code.upper()

    return QuestionnaireOptions(options, category_codes)


def find_questionnaire_columns(df: pd.DataFrame) -> Dict[str, Optional[str]]:
    columns: Dict[str, Optional[str]] = {MODE_SITE: None, MODE_REMOTE: None}
    for col in df.columns:
        col_lower = str(col).lower()
        for mode, fragment in _HEADER_FRAGMENTS.items():
            if columns[mode] is None and fragment in col_lower:
                columns[mode] = col
    return columns


def find_category_column(df: pd.DataFrame) -> Optional[str]:
    return next(
        (c for c in df.columns
         if "category of facility" in str(c).lower() or "facility category" in str(c).lower()),
        None,
    )


def resolve_row_questionnaires(row, options: QuestionnaireOptions, category_col: Optional[str],
                               questionnaire_cols: Dict[str, Optional[str]]):
    """The form codes a sheet row picked, and an error message for each column that is not valid.

    Returns ({payload key: form code}, [errors]).
    """
    category = _cell_text(row.get(category_col)) if category_col else ""
    codes: Dict[str, str] = {}
    errors: List[str] = []
    for mode, col in questionnaire_cols.items():
        header = QUESTIONNAIRE_HEADERS[mode].replace(" (Mandatory)", "")
        label = _cell_text(row.get(col)) if col else ""
        if not label:
            errors.append(f"{header} is required")
            continue
        if not category:
            errors.append(f"{header} cannot be checked: Category of Facility is empty")
            continue
        code = options.form_code(category, mode, label)
        if not code:
            allowed = options.labels(mode, [category])
            hint = f" Allowed: {', '.join(allowed)}" if allowed else " No questionnaire is configured for it"
            errors.append(f"{header} '{label}' is not valid for category {category}.{hint}")
            continue
        codes[QUESTIONNAIRE_PAYLOAD_KEYS[mode]] = code
    return codes, errors
