/**
 * Pure helper functions for the "Unable to log in? Call toll-free" feature.
 *
 * Shape of one MDMS row (common-masters.CRMTollFreeContact)
 *   {
 *     "stateCode": "KARNATAKA",
 *     "stateName": "Karnataka",
 *     "order": 1,
 *     "active": true,
 *     "contacts": [
 *       { "label": "CRM Helpline", "number": "1234567890", "availability": "Mon-Sat, 9 AM - 6 PM" }
 *     ]
 *   }
 */

export const TOLL_FREE_MDMS_MODULE = "common-masters";
export const TOLL_FREE_MDMS_MASTER = "CRMTollFreeContact";

// Optional fallback key in globalConfigs.js (window.globalConfigs.getConfig(...)), same row shape
// as MDMS. Used only when MDMS returns nothing, e.g. in an environment where the master has not
// been seeded yet.
export const TOLL_FREE_GLOBAL_CONFIG_KEY = "CRM_TOLL_FREE_CONTACTS";

/**
 * Converts a configured phone number into the value used inside a `tel:` link.
 *
 *   "1800-425-1234"    -> "18004251234"
 *   "+91 80 4040 1234" -> "+918040401234"
 *   "N/A"              -> null
 */
export const toDialableNumber = (rawNumber) => {
  if (rawNumber === null || rawNumber === undefined) return null;

  const value = String(rawNumber).trim();
  if (!value) return null;

  const hasPlus = value.startsWith("+");
  const digitsOnly = value.replace(/\D/g, "");

  if (digitsOnly.length < 3) return null;

  return hasPlus ? `+${digitsOnly}` : digitsOnly;
};

const cleanText = (value) => (typeof value === "string" || typeof value === "number" ? String(value).trim() : "");

/**
 * Converts one item of a row's `contacts` array into { label, display, dial, availability }.
 * Accepts the agreed object shape and, defensively, a plain string number.
 * Returns null when the item has no dialable number.
 */
const normalizeContact = (item) => {
  const isObject = item && typeof item === "object";
  const rawNumber = isObject ? item.number : item;
  const dial = toDialableNumber(rawNumber);
  if (!dial) return null;

  return {
    label: isObject ? cleanText(item.label) : "",
    display: cleanText(rawNumber),
    dial, // used in the tel: link
    availability: isObject ? cleanText(item.availability) : "",
  };
};

/**
 * Turns raw MDMS rows into a clean, de-duplicated, sorted list:
 *
 *   [{ stateCode, stateName, order, contacts: [{ label, display, dial, availability }] }]
 *
 * Rules (each one maps to an acceptance criterion or an edge case):
 *  - Rows with active === false are hidden. A missing `active` counts as active.
 *  - Rows with neither stateCode nor stateName are dropped, because the number could not be shown
 *    "against the respective state".
 *  - Contacts without a valid number are dropped one by one; a state left with no contact is dropped.
 *  - Two rows for the same stateCode are merged instead of showing the state twice.
 *  - The same number listed twice for a state is shown once (first label/availability wins).
 *  - A contact and its state always come from the SAME row, never matched by list position,
 *    so a number cannot end up next to the wrong state.
 */
export const normalizeTollFreeContacts = (rawRows) => {
  if (!Array.isArray(rawRows)) return [];

  const byState = new Map();

  rawRows.forEach((row) => {
    if (!row || typeof row !== "object") return;
    if (row.active === false) return;

    const stateCode = cleanText(row.stateCode).toUpperCase();
    const stateName = cleanText(row.stateName);
    if (!stateCode && !stateName) return;

    const contacts = (Array.isArray(row.contacts) ? row.contacts : []).map(normalizeContact).filter(Boolean);
    if (contacts.length === 0) return;

    const key = stateCode || stateName.toUpperCase();
    const order = Number(row.order);
    const existing = byState.get(key);

    if (existing) {
      // Merge duplicate state rows; keep the first non-empty name and the first valid order.
      existing.contacts.push(...contacts);
      existing.stateName = existing.stateName || stateName;
      existing.order = Number.isFinite(existing.order) ? existing.order : order;
    } else {
      byState.set(key, { stateCode, stateName, order, contacts });
    }
  });

  return Array.from(byState.values())
    .map((state) => {
      // De-duplicate on the dialable value so "1800-425-1234" and "1800 425 1234" collapse into one.
      const seen = new Set();
      const contacts = state.contacts.filter((contact) => {
        if (seen.has(contact.dial)) return false;
        seen.add(contact.dial);
        return true;
      });
      return { ...state, contacts };
    })
    .sort((a, b) => {
      // Explicit `order` first (ascending); rows without order go last, then alphabetical.
      const orderA = Number.isFinite(a.order) ? a.order : Number.MAX_SAFE_INTEGER;
      const orderB = Number.isFinite(b.order) ? b.order : Number.MAX_SAFE_INTEGER;
      if (orderA !== orderB) return orderA - orderB;
      return (a.stateName || a.stateCode).localeCompare(b.stateName || b.stateCode);
    });
};

/**
 * Reads the optional globalConfigs fallback. Wrapped in try/catch because globalConfigs is
 * an external script and getConfig may be missing or throw.
 */
export const readGlobalConfigContacts = () => {
  try {
    const value = window?.globalConfigs?.getConfig?.(TOLL_FREE_GLOBAL_CONFIG_KEY);
    return Array.isArray(value) ? value : [];
  } catch (e) {
    return [];
  }
};
