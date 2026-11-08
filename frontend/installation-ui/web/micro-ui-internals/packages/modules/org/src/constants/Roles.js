/**
 * Role codes this module gates on - see Module.js, ORGCard.js and pages/employee/index.js.
 *
 * Codes rather than names, because names are not unique in MDMS: `VENDOR_ADMIN` and the retired
 * `AMC_VENDOR_ADMIN` are both called "Vendor Administrator", so a gate written against the name
 * let the retired one through.
 */

/** Manages every organisation. Unscoped by design - see the jurisdiction plan, §2.1. */
export const ORG_PLATFORM_ADMIN = "ORG_PLATFORM_ADMIN";

/** Manages the users of their own vendor organisation, within their own jurisdictions. */
export const VENDOR_ADMIN = "VENDOR_ADMIN";
