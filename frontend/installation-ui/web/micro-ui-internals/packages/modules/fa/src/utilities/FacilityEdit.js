/**
 * Facility edit: what a save changes that needs the user's confirmation, and how the backend's
 * errors map back onto the form. Pure functions; no React, no network.
 */

/** Form fields holding the facility identifiers, with the label the backend uses for each. */
const IDENTIFIER_FIELDS = [
  { key: "hfrId", labelKey: "FACILITY_HFR_ID", backendLabel: "HFR ID" },
  { key: "ninId", labelKey: "FACILITY_NIN_ID", backendLabel: "NIN ID" },
  { key: "facilityPocUsername", labelKey: "FACILITY_POC_USERNAME", backendLabel: "PoC Username" },
];

const trimmed = (value) => (value === null || value === undefined ? "" : `${value}`.trim());

/**
 * The facility PoC's login username, as health-facility-registry derives it: the PoC username
 * for a non-HEALTH facility, otherwise the HFR ID, falling back to the NIN.
 */
export const pocLoginOf = (categoryCode, values) =>
  categoryCode && categoryCode !== "HEALTH" ? trimmed(values.facilityPocUsername) : trimmed(values.hfrId) || trimmed(values.ninId);

/**
 * What saving `formData` over `before` (the loaded facility, in the form's shape) changes among
 * the fields that need a confirmation:
 * - `changes`: `[{key, labelKey, from, to, translate}]` for each identifier or District/Block that
 *   differs. `translate` marks boundary keys (`Boundary_<code>`), which are shown through `t()`.
 * - `loginChange`: `{from, to}` when the PoC's login username changes, else null.
 * - `blockBoundaryCode`: the new block's code when the block changed, else null. Only the block
 *   is ever sent; a new district is just a block in another district.
 */
export const facilityEditChanges = (before, formData) => {
  const categoryCode = formData?.facilityCategory?.code || before?.facilityCategory?.code;
  const changes = [];

  IDENTIFIER_FIELDS.forEach(({ key, labelKey }) => {
    // A field the form doesn't show for this category isn't edited.
    if (!(key in (formData || {}))) return;
    const from = trimmed(before?.[key]);
    const to = trimmed(formData[key]);
    if (from !== to) changes.push({ key, labelKey, from, to, translate: false });
  });

  const fromBlock = before?.blockCode || before?.block?.code || "";
  const toBlock = formData?.block?.code || "";
  const blockChanged = !!toBlock && toBlock !== fromBlock;
  if (blockChanged) {
    const fromDistrict = before?.districtCode || before?.district?.code || "";
    const toDistrict = formData?.district?.code || "";
    if (toDistrict && toDistrict !== fromDistrict) {
      changes.push({ key: "district", labelKey: "CS_DISTRICT", from: fromDistrict && `Boundary_${fromDistrict}`, to: `Boundary_${toDistrict}`, translate: true });
    }
    changes.push({ key: "block", labelKey: "CS_BLOCK", from: fromBlock && `Boundary_${fromBlock}`, to: `Boundary_${toBlock}`, translate: true });
  }

  const loginBefore = pocLoginOf(before?.facilityCategory?.code, before || {});
  const loginAfter = pocLoginOf(categoryCode, { ...(before || {}), ...(formData || {}) });

  return {
    changes,
    loginChange: loginBefore !== loginAfter ? { from: loginBefore, to: loginAfter } : null,
    blockBoundaryCode: blockChanged ? toBlock : null,
  };
};

/** Every `{code, message}` in a failed request's `Errors`; one request can carry several. */
export const apiErrors = (error) => error?.response?.data?.Errors || [];

/** Every error message of a failed request, joined; empty when there are none. */
export const apiErrorText = (error) =>
  apiErrors(error)
    .map((entry) => entry?.message)
    .filter(Boolean)
    .join(" ");

/**
 * The form fields a FACILITY_DUPLICATE_IDENTIFIER error names. Its message lists each clash as
 * "<field> '<value>' of facility ...", joined with "; ".
 */
export const duplicateIdentifierFields = (error) => {
  const fields = new Set();
  apiErrors(error)
    .filter((entry) => entry?.code === "FACILITY_DUPLICATE_IDENTIFIER")
    .forEach((entry) =>
      `${entry.message || ""}`.split(";").forEach((part) => {
        const clash = part.trim();
        const field = IDENTIFIER_FIELDS.find(({ backendLabel }) => clash.startsWith(`${backendLabel} '`));
        if (field) fields.add(field.key);
      })
    );
  return [...fields];
};
