
import { getSessionId } from './session';
import { getUserRoleCsv, getGeography } from './context';

const getDeviceType = () =>
  window?.Digit?.Utils?.browser?.isMobile?.() ? 'mobile' : 'desktop';

const getBrowser = () => {
  const ua = navigator.userAgent;
  if (ua.includes('Edg')) return 'Edge';
  if (ua.includes('Chrome')) return 'Chrome';
  if (ua.includes('Firefox')) return 'Firefox';
  if (ua.includes('Safari')) return 'Safari';
  return 'Other';
};

export function baseParams(extra = {}) {
  const geo = getGeography();
  return {
    session_id: getSessionId(),
    user_role: getUserRoleCsv(),
    device_type: getDeviceType(),
    browser: getBrowser(),
    os: navigator.platform || 'unknown',
    ...geo,
    ...extra,
  };
}
