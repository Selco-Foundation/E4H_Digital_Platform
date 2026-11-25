/**
 * View Facilities: the search criteria built from the page's filter state, the row each facility
 * becomes, and the CSV the Download button saves. Pure functions; no React, no network.
 */

/**
 * Sets one filter from the user's own choice, or leaves it unset.
 *
 * The user's own jurisdiction is deliberately not sent as a fallback. Facility search is scoped
 * on the server from the `x-acting-role` header this module sends and the caller's HRMS grants,
 * so echoing those same grants back narrowed nothing the server was not already narrowing. What
 * it could do is narrow *more*: a stale or half-written client copy silently dropped rows the
 * user is entitled to see, and that failure looks identical to having no such facilities.
 *
 * The pickers only offer values already inside the user's scope, so a chosen value narrows within
 * the server's ceiling rather than escaping it.
 */
const applyFilter = (criteria, key, chosen) => {
  if (chosen?.length) criteria[key] = chosen;
};

/** The search box's term as the page keeps it; a filter saved by an older version has none. */
export const searchTextOf = (filter) => (filter?.facilitySearchQuery?.searchText || "").trim();

/**
 * `/_bulk-search` criteria for the page's filters and search text, without paging.
 *
 * With no filters this sends no boundary criteria at all and lets the server scope the list.
 * That makes the server the only thing scoping it: with `facility.jurisdiction.scope.mode` set to
 * `off` or `warn`, this search returns every facility in the country.
 */
export const buildFacilitySearchCriteria = (filter, tenantId) => {
  const { facilityFilterQuery } = filter || {};
  const criteria = { tenantId: [tenantId] };

  applyFilter(criteria, "state", facilityFilterQuery?.state);
  applyFilter(criteria, "district", facilityFilterQuery?.district);
  applyFilter(criteria, "block", facilityFilterQuery?.block);
  applyFilter(criteria, "boundaryCodes", facilityFilterQuery?.facility);

  // Matched on the server, partially and case-insensitively, against name, HFR ID, NIN and PoC
  // username.
  const searchText = searchTextOf(filter);
  if (searchText) criteria.searchText = searchText;

  return criteria;
};

/** One facility from `/_bulk-search` as a list row. */
export const toFacilityRow = (facility) => ({
  id: facility?.facility_id,
  facilityName: facility?.facility_name,
  state: facility?.boundary?.state,
  district: facility?.boundary?.district,
  block: facility?.boundary?.block,
  categoryCode: facility?.facility_category,
  typeCode: facility?.facility_type,
  hfrId: facility?.hfr_id,
  ninId: facility?.nin_id,
  pocUsername: facility?.facility_poc_username,
});

/** `{code: name}` for an MDMS master's rows. */
export const namesByCode = (rows) => Object.fromEntries((rows || []).map((row) => [row.code, row.name]));

/**
 * One CSV cell. Quoted when it holds a comma, quote or line break, and with a leading `'` when it
 * starts like a spreadsheet formula, so a facility name such as `=HYPERLINK(...)` opens as text.
 */
const csvCell = (value) => {
  let text = value === null || value === undefined ? "" : String(value);
  if (/^[=+\-@\t\r]/.test(text)) text = `'${text}`;
  return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
};

/** `columns` is `[{header, value: (row) => cell}]`. Lines end in CRLF, as RFC 4180 has it. */
export const toCsv = (columns, rows) =>
  [columns.map((column) => csvCell(column.header)), ...rows.map((row) => columns.map((column) => csvCell(column.value(row))))]
    .map((cells) => cells.join(","))
    .join("\r\n");

/**
 * The Download file's columns: the list's own, plus the facility id and identifiers, which are
 * what someone working from the file needs to find a facility again.
 */
export const facilityCsvColumns = (t, categoryNames, typeNames) => [
  { header: t("CS_FACILITY_ID"), value: (row) => row.id },
  { header: t("CS_HEALTH_FACILITY"), value: (row) => row.facilityName },
  { header: t("CS_STATE"), value: (row) => (row.state ? t(`Boundary_${row.state}`) : "") },
  { header: t("CS_DISTRICT"), value: (row) => (row.district ? t(`Boundary_${row.district}`) : "") },
  { header: t("CS_BLOCK"), value: (row) => (row.block ? t(`Boundary_${row.block}`) : "") },
  { header: t("FACILITY_CATEGORY"), value: (row) => categoryNames[row.categoryCode] || row.categoryCode },
  { header: t("FACILITY_TYPE"), value: (row) => typeNames[row.typeCode] || row.typeCode },
  { header: t("FACILITY_HFR_ID"), value: (row) => row.hfrId },
  { header: t("FACILITY_NIN_ID"), value: (row) => row.ninId },
  { header: t("FACILITY_POC_USERNAME"), value: (row) => row.pocUsername },
];
