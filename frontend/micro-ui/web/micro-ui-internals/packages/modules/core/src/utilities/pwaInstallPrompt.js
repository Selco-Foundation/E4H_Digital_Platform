const INSTALLED_KEY = "selco.pwaInstalled";
const MAX_PROMPTS = 3;

// beforeinstallprompt fires once, early in the page load, so it is captured when this module loads (imported from Module.js).
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

const getPromptCountKey = (userInfo) => {
  const employeeId = userInfo?.uuid || userInfo?.id || userInfo?.userId || userInfo?.userName || userInfo?.mobileNumber;
  return employeeId ? `selco.pwaInstallPromptCount.${encodeURIComponent(String(employeeId))}` : null;
};

const getPromptCount = (key) => {
  const cookie = document.cookie.split("; ").find((value) => value.startsWith(`${key}=`));
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

/**
 * Opens the browser's native PWA install dialog after a successful login, at most MAX_PROMPTS times per user.
 * Must be called soon after the login click: the browser only allows prompt() within a few seconds of a user action.
 * The event itself is captured by the listener above and stored on window.deferredPwaInstallPrompt.
 */
export const showPwaInstallPromptIfEligible = async (userInfo) => {
  const installEvent = window.deferredPwaInstallPrompt;
  const key = getPromptCountKey(userInfo);
  if (!installEvent || !key || isInstalled()) return;

  const timesShown = getPromptCount(key);
  if (timesShown >= MAX_PROMPTS) return;

  try {
    await Promise.resolve(installEvent.prompt());
    // userChoice only settles once the dialog was actually shown, so refused prompts are not counted.
    const result = await installEvent.userChoice;
    setPromptCount(key, timesShown + 1);
    window.deferredPwaInstallPrompt = null;
    if (result?.outcome === "accepted") window.localStorage.setItem(INSTALLED_KEY, "true");
  } catch (error) {
    console.warn("Could not open the browser's PWA install prompt", error);
  }
};
