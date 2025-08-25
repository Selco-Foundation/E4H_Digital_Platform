import { GA_MEASUREMENT_ID, DEBUG_MODE } from "./config";

const queue = [];
let ready = false;

function flush() {
  const g = typeof window !== "undefined" ? window.gtag : undefined;
  if (!g) return;
  ready = true;

  while (queue.length) {
    const args = queue.shift();
    g(...args);
  }
}

function waitForGtag() {
  if (typeof window === "undefined") return;

  if (typeof window.gtag === "function") {
    flush();
    return;
  }

  const timer = window.setInterval(() => {
    if (typeof window.gtag === "function") {
      window.clearInterval(timer);
      flush();
    }
  }, 50);

  const onReady = () => {
    if (typeof window.gtag === "function") {
      flush();
      document.removeEventListener("DOMContentLoaded", onReady);
      window.clearInterval(timer);
    }
  };

  if (document.readyState === "interactive" || document.readyState === "complete") {
    onReady();
  } else {
    document.addEventListener("DOMContentLoaded", onReady);
  }
}

waitForGtag();

export function gtag(...args) {
  // Add debug_mode automatically if enabled in config
  if (DEBUG_MODE && args[0] === "event") {
    const params = args[2] || {};
    args[2] = { debug_mode: true, ...params };
  }
  if (ready && typeof window !== "undefined" && typeof window.gtag === "function") {
    window.gtag(...args);
  } else {
    queue.push(args);
  }
}
