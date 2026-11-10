import React, { useEffect, useState } from "react";

const INSTALLED_KEY = "selco.pwaInstalled";
const getEmployeeKey = (event) => {
  let userInfo = event.detail;
  if (!userInfo) {
    try {
      userInfo = JSON.parse(window.localStorage.getItem("Employee.user-info") || window.localStorage.getItem("user-info") || "null");
    } catch (error) {
      userInfo = null;
    }
  }

  const employeeId = userInfo?.uuid || userInfo?.id || userInfo?.userId || userInfo?.userName || userInfo?.mobileNumber;
  return employeeId ? `selco.pwaInstallPromptCount.${encodeURIComponent(String(employeeId))}` : null;
};
const getPromptCount = key => {
  const cookie = document.cookie.split("; ").find(value => value.startsWith(`${key}=`));
  return Number(cookie ? cookie.slice(key.length + 1) : 0);
};
const setPromptCount = (key, count) => {
  // The cookie keeps this employee's count across logout, which clears localStorage.
  document.cookie = `${key}=${count}; Max-Age=63072000; Path=/; SameSite=Lax`;
};
const isInstalled = () =>
  window.localStorage.getItem(INSTALLED_KEY) === "true" ||
  window.navigator.standalone === true ||
  window.matchMedia?.("(display-mode: standalone)")?.matches === true;

if (typeof window !== "undefined") {
  window.addEventListener("beforeinstallprompt", (event) => {
    event.preventDefault();
    window.localStorage.removeItem(INSTALLED_KEY);
    window.deferredPwaInstallPrompt = event;
  });

  window.addEventListener("appinstalled", () => {
    window.localStorage.setItem(INSTALLED_KEY, "true");
    window.deferredPwaInstallPrompt = null;
  });
}

const PwaShell = () => {
  const [offline, setOffline] = useState(!navigator.onLine);

  useEffect(() => {
    const onOnline = () => setOffline(false);
    const onOffline = () => setOffline(true);
    const onSuccessfulLogin = async (event) => {
      const employeeKey = getEmployeeKey(event);
      if (!employeeKey) return;

      const installEvent = window.deferredPwaInstallPrompt;
      if (isInstalled() || !installEvent) return;

      const timesShown = getPromptCount(employeeKey);
      if (timesShown >= 3) return;

      try {
        // Count this eligible login before calling the browser API so failures cannot exceed the limit.
        setPromptCount(employeeKey, timesShown + 1);
        installEvent.prompt();
        window.deferredPwaInstallPrompt = null;
        const result = await installEvent.userChoice;
        if (result?.outcome === "accepted") window.localStorage.setItem(INSTALLED_KEY, "true");
      } catch (error) {
        console.warn("Could not open the browser's PWA install prompt", error);
      }
    };

    window.addEventListener("online", onOnline);
    window.addEventListener("offline", onOffline);
    window.addEventListener("selco:successful-login", onSuccessfulLogin);
    return () => {
      window.removeEventListener("online", onOnline);
      window.removeEventListener("offline", onOffline);
      window.removeEventListener("selco:successful-login", onSuccessfulLogin);
    };
  }, []);

  return offline ? (
    <div role="status" style={{ background: "#b42318", color: "#fff", left: 0, padding: "10px 16px", position: "fixed", right: 0, textAlign: "center", top: 0, zIndex: 10002 }}>
      No Internet Connection
    </div>
  ) : null;
};

export default PwaShell;
