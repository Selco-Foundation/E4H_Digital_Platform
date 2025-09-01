import { getSessionId } from "./session";
import { getUserRoleCsv, getGeography } from "./context";

const getDeviceType = () => {
  const ua = navigator.userAgent || "";
  return /Mobi|Android|iPhone|iPad|iPod/i.test(ua) ? "mobile" : "desktop";
};

const getBrowser = () => {
  const ua = navigator.userAgent || "";
  const isBrave = typeof navigator.brave !== "undefined" && typeof navigator.brave.isBrave === "function";

  if (isBrave) return "Brave";
  if (/EdgA?\/|EdgiOS\//.test(ua)) return "Edge";
  if (/OPR\/|Opera/.test(ua)) return "Opera";
  if (/YaBrowser\//.test(ua)) return "Yandex";
  if (/Vivaldi\//.test(ua)) return "Vivaldi";
  if (/SamsungBrowser\//.test(ua)) return "Samsung Internet";
  if (/FxiOS\/|Firefox\//.test(ua)) return "Firefox";
  if (/CriOS\//.test(ua)) return "Chrome";
  if (/(Chrome|Chromium)\//.test(ua) && !/OPR\/|Edg\/|EdgA\/|EdgiOS\/|Vivaldi\/|YaBrowser\/|SamsungBrowser\//.test(ua)) return "Chrome";
  if (/Safari\//.test(ua) && !/(Chrome|CriOS|Chromium|OPR|Edg|EdgA|EdgiOS|Vivaldi|YaBrowser|SamsungBrowser)\//.test(ua)) return "Safari";
  return "Other";
};

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
