
export function getUserRoleCsv() {
  try {
    const roles = window?.Digit?.SessionStorage?.get('User')?.info?.roles || [];
    return roles.map(r => r.code).join(',') || 'unknown';
  } catch {
    return 'unknown';
  }
}

export function getGeography() {

  const state = window?.Digit?.ULBService?.getStateId?.() || 'unknown';
  const district = window?.Digit?.SessionStorage?.get('User')?.info?.district || 'unknown';
  const block = window?.Digit?.SessionStorage?.get('User')?.info?.block || 'unknown';
  const facility = window?.Digit?.SessionStorage?.get('User')?.info?.facilityName || 'unknown';

  return {
    geography_state: state,
    geography_district: district,
    geography_block: block,
    facility_name: facility,
  };
}
