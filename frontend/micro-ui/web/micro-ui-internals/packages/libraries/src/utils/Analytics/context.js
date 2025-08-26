let _ctx = {
  facility_name: "unknown",
};


export function setFacilityName(name) {
  const val = (name && String(name).trim()) || "unknown";
  _ctx.facility_name = val;
  try {
    sessionStorage.setItem("facility_name", val);
  } catch {}
}

export function getFacilityName() {
  // 1) in-memory
  if (_ctx.facility_name && _ctx.facility_name !== "unknown") return _ctx.facility_name;

  // 2) sessionStorage
  try {
    const fromSS = sessionStorage.getItem("facility_name");
    if (fromSS) {
      _ctx.facility_name = fromSS;
      return fromSS;
    }
  } catch {}

  // 3) current user object (as a last fallback)
  const info =
    window?.Digit?.SessionStorage?.get("User")?.info ||
    (JSON.parse(sessionStorage.getItem("Digit.User") || "null")?.value?.info) ||
    {};
  const fromUser =
    info?.facilityName ||
    info?.facility?.name ||
    info?.additionalDetails?.facilityName ||
    info?.additionalDetails?.facility?.name ||
    "";

  if (fromUser) {
    _ctx.facility_name = String(fromUser).trim();
    return _ctx.facility_name;
  }

  return "unknown";
}

export function getUserRoleCsv() {
  try {
    const roles =
      window?.Digit?.SessionStorage?.get("User")?.info?.roles ||
      (JSON.parse(sessionStorage.getItem("Digit.User") || "null")?.value?.info?.roles) ||
      [];
    return roles.map((r) => r.code).join(",") || "unknown";
  } catch {
    return "unknown";
  }
}

export function getGeography() {
  const state = window?.Digit?.ULBService?.getStateId?.() || "unknown";

  const info =
    window?.Digit?.SessionStorage?.get("User")?.info ||
    (JSON.parse(sessionStorage.getItem("Digit.User") || "null")?.value?.info) ||
    {};

  const district = info?.district || "unknown";
  const block = info?.block || "unknown";

  // ✅ read from the shared store (with fallbacks)
  const facility_name = getFacilityName();

  return {
    geography_state: state,
    geography_district: district,
    geography_block: block,
    facility_name,
  };
}
