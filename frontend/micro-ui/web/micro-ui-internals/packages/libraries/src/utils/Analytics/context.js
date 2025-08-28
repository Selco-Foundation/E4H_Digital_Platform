let _ctx = {
  facility_name: "unknown",
};

const STATE_MAP = {
  as: "Assam",
  gj: "Gujarat",
  mh: "Maharashtra",
  ml: "Meghalaya",
  mn: "Manipur",
  mz: "Mizoram",
  nl: "Nagaland",
  or: "Odisha",
  pg: "Karnataka",
  sk: "Sikkim",
};

export function setFacilityName(name) {
  const val = (name && String(name).trim()) || "unknown";
  _ctx.facility_name = val;
  try {
    sessionStorage.setItem("facility_name", val);
  } catch {}
}

export function getFacilityName() {
  if (_ctx.facility_name && _ctx.facility_name !== "unknown") return _ctx.facility_name;

  try {
    const fromSS = sessionStorage.getItem("facility_name");
    if (fromSS) {
      _ctx.facility_name = fromSS;
      return fromSS;
    }
  } catch {}

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
  let stateCode = window?.Digit?.ULBService?.getStateId?.() || "unknown";
  stateCode = String(stateCode).split(".")[0].toLowerCase();
  const state = STATE_MAP[stateCode] || stateCode || "unknown";

  const info =
    window?.Digit?.SessionStorage?.get("User")?.info ||
    (JSON.parse(sessionStorage.getItem("Digit.User") || "null")?.value?.info) ||
    {};

  const district = info?.district || "unknown";
  const block = info?.block || "unknown";
  const facility_name = getFacilityName();

  return {
    geography_state: state,
    geography_district: district,
    geography_block: block,
    facility_name,
  };
}
