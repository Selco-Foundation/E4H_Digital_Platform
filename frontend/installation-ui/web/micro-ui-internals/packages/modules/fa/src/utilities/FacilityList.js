/**
 * View Facilities: the search criteria built from the page's filter state, and the row each
 * facility becomes. Pure functions; no React, no network.
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
});

/** `{code: name}` for an MDMS master's rows. */
export const namesByCode = (rows) => Object.fromEntries((rows || []).map((row) => [row.code, row.name]));

/**
 * The Download's filters: the list's own criteria, without paging or tenant, which the export
 * sets itself.
 */
export const facilityExportFilters = (filter, tenantId) => {
  const { tenantId: _tenant, ...filters } = buildFacilitySearchCriteria(filter, tenantId);
  return filters;
};
