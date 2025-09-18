import { getSessionId } from "./session";
import { getUserRoleCsv, getGeography } from "./context";

const getDeviceType = () => {
  const ua = navigator.userAgent || "";
  return /Mobi|Android|iPhone|iPad|iPod/i.test(ua) ? "mobile" : "desktop";
};


const getBrowser = (() => {
  let cache = "Other";

  const fromUAData = () => {
    const brands = (navigator.userAgentData?.brands || [])
      .map(b => b.brand)
      .filter(b => b && b !== "Not;A=Brand");
    const has = (re) => brands.some(n => re.test(n));

    if (has(/Brave/i)) return "Brave";
    if (has(/Microsoft Edge|Edge/i)) return "Edge";
    if (has(/Opera/i)) return "Opera";
    if (has(/Vivaldi/i)) return "Vivaldi";
    if (has(/Yandex/i)) return "Yandex";
    if (has(/Samsung/i)) return "Samsung Internet";
    if (has(/Firefox/i)) return "Firefox";
    if (has(/Google Chrome|Chromium/i)) return "Chrome";
    return null;
  };

  const fromUA = () => {
    const ua = navigator.userAgent || "";
    if (/EdgA?\/|EdgiOS\//.test(ua)) return "Edge";
    if (/OPR\/|Opera/.test(ua)) return "Opera";
    if (/Vivaldi\//.test(ua)) return "Vivaldi";
    if (/YaBrowser\//.test(ua)) return "Yandex";
    if (/SamsungBrowser\//.test(ua)) return "Samsung Internet";
    if (/FxiOS\/|Firefox\//.test(ua)) return "Firefox";
    if (/CriOS\//.test(ua)) return "Chrome"; // iOS Chrome
    if (/(Chrome|Chromium)\//.test(ua) && !/OPR\/|Edg\/|EdgA\/|EdgiOS\/|Vivaldi\/|YaBrowser\/|SamsungBrowser\//.test(ua)) return "Chrome";
    if (/Safari\//.test(ua) && !/(Chrome|CriOS|Chromium|OPR|Edg|EdgA|EdgiOS|Vivaldi|YaBrowser|SamsungBrowser)\//.test(ua)) return "Safari";
    return "Other";
  };

  cache = fromUAData() || fromUA();

  try {
    if (navigator.brave && typeof navigator.brave.isBrave === "function") {
      Promise.resolve(navigator.brave.isBrave())
        .then(isBrave => { if (isBrave) cache = "Brave"; })
        .catch(() => {});
    }
  } catch (_) {}

  return () => cache;
})();


const getOS = () => {
  const ua = navigator.userAgent || "";
  if (/Windows/i.test(ua)) return "Windows";
  if (/Android/i.test(ua)) return "Android";
  if (/iPhone|iPad|iPod/i.test(ua)) return "iOS";
  if (/CrOS/i.test(ua)) return "Chrome OS";
  if (/Mac OS X|Macintosh/i.test(ua)) return "macOS";
  if (/Ubuntu/i.test(ua)) return "Ubuntu";
  if (/Linux/i.test(ua)) return "Linux";
  return "unknown";
};

export function baseParams(extra = {}) {
  const geo = getGeography();
  return {
    session_id: getSessionId(),
    user_role: getUserRoleCsv(),
    device_type: getDeviceType(),
    browser: getBrowser(),
    os: getOS(),
    ...geo,
    ...extra,
  };
}
