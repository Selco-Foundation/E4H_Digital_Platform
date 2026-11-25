/**
 * Vendor organisations as the Facility Admin picks them. Pure functions; no React, no network.
 */

/**
 * `{code, name}` options sorted by name. Names aren't unique across organisations, so a name
 * shared by two of them carries its code, as the Bulk Add template does: "Name (CODE)".
 */
export const toVendorOptions = (organisations) => {
  const rows = (organisations || []).filter((organisation) => organisation?.code);
  const nameCount = {};
  rows.forEach((organisation) => {
    const key = (organisation.name || "").trim().toLowerCase();
    nameCount[key] = (nameCount[key] || 0) + 1;
  });
  return rows
    .map((organisation) => {
      const name = (organisation.name || "").trim();
      const shared = !name || nameCount[name.toLowerCase()] > 1;
      return { code: organisation.code, name: shared ? `${name || organisation.code} (${organisation.code})` : name };
    })
    .sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: "base" }));
};
