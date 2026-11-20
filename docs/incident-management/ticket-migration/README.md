# Ticket issue type / sub type migration

Moves existing tickets (`eg_incident_v2`) from the old issue types and sub types to the new ones listed in
`Ticket Migration.xlsx`:

- `reportertype = 'HCR'` tickets go to the facility user sub types (`Incident.ServiceDefs`, channel `HF`).
- `reportertype = 'CRM'` tickets go to the CRM sub types (channel `CRM`, or an offered entry without channel).

A ticket stores the ServiceDef `menuPath` in `incidenttype` and its `serviceCode` in `incidentsubtype`.
The workbook is written with labels, so the script turns them into codes against the MDMS of the
environment it runs on. The ids and codes are not the same in DEV and UAT.

A label is matched first against what the UI shows: the localized message of `SERVICEDEFS.<CODE>` (for a
sub type) or `SERVICEDEFS.<MENUPATH>` (for a type), read from the `rainmaker-im` and `rainmaker-common`
modules (the first module wins). The script then tries the ServiceDef name and code, and finally the overrides.

## Files

| File | Role |
|---|---|
| `build_mapping.py` | Reads `Ticket Migration.xlsx` (sheet `CRM` columns A-D, sheet `HCR` columns G-J) and writes `ticket_migration_mapping.json`. |
| `ticket_migration_mapping.json` | One row per old label → new label and reporter type. A `Remove` target has `"action": "skip"`. |
| `ticket_migration_overrides.json` | Corrections for labels that do not match a ServiceDef name: type aliases, extra old codes for an old label, and the code of a new label. |
| `migrate_ticket_subtypes.py` | Builds the report and the SQL, then applies them with `--apply`. |

## Prerequisite

The new ServiceDefs (HF and CRM) must exist in the environment's MDMS. A mapping row whose target is not
found is reported as `UNRESOLVED_TARGET`, and its tickets are left alone.

## Usage

```bash
# 1. (when the workbook changes) regenerate the mapping
python3 build_mapping.py "/path/to/Ticket Migration.xlsx"

# 2. dry run: report + SQL, nothing changed
MDMS_URL=http://localhost:8285 LOCALIZATION_URL=http://localhost:8098 \
DATABASE_URL=postgresql://user:pass@host:5432/db \
  python3 migrate_ticket_subtypes.py --out-dir out

# 3. apply: backup table + UPDATE in one transaction, then Elasticsearch
MDMS_URL=... DATABASE_URL=... LOCALIZATION_URL=http://localhost:8098 \
EGOV_ES_HOST=https://localhost:9200 EGOV_ES_USERNAME=... EGOV_ES_PASSWORD=... \
  python3 migrate_ticket_subtypes.py --apply --out-dir out
```

Options:

- `--servicedefs-file <export.json>` reads the ServiceDefs from an MDMS export instead of `MDMS_URL`.
- `--localization-file <response.json>` reads a saved `/localization/messages/v1/_search` response instead
  of `LOCALIZATION_URL`. Without either one, labels are matched on names and codes only.
- `--tickets-csv <export.csv>` reads the tickets from a table export. It works for a dry run only.
- `--skip-es` updates the database only.
- `--es-only <run_id>` redoes the Elasticsearch update of a run, from the backup table.

The script needs `psql` on the PATH. Set `AUTH_TOKEN` if MDMS or localization need a token.

## Output (`out/`)

- `report_<run>.csv`: one line per (reporter type, old type, old sub type), with the ticket count and a status:
  - `MIGRATE`: the ticket is moved to its target;
  - `ALREADY_NEW`: the ticket already uses its target, or a sub type of its channel;
  - `SKIP_REMOVE`: the target in the workbook is `Remove`;
  - `UNMAPPED`: the old code is not in the workbook, or its label does not match MDMS;
  - `UNRESOLVED_TARGET`: the target ServiceDef is missing;
  - `CONFLICT`: one old code is mapped to two different targets.
- `mapping_problems_<run>.csv`: workbook rows that do not resolve against MDMS:
  - `UNRESOLVED_SOURCE`: add the old codes in `source` of the overrides;
  - `UNRESOLVED_TARGET`: create the ServiceDef, or set its code in `target` of the overrides.
- `migration_<run>.sql`: the SQL that `--apply` runs. It copies each ticket into
  `eg_incident_v2_subtype_migration_bkp` (`run_id`) and then updates it. `lastmodifiedtime` is not changed.
- `rollback_<run>.sql`: puts the database back from the backup table. Elasticsearch is not rolled back.

Elasticsearch: the indices `computed-sla-im-services-write` and `audit-computed-sla-im-services-write` get
`Data.incident.incidentType`, `incidentSubType` and their `_localized` labels updated for the migrated
`incidentId`s. The labels come from the same localization messages, or from the ServiceDef name (sub type) or
the menuPath (type) when there is none.

Re-running is safe: tickets already migrated come out as `ALREADY_NEW`.
