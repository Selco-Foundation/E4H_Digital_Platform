import { GA_MEASUREMENT_ID, DEBUG_MODE } from "./config";

const queue = [];
let ready = false;

function flush() {
    if (typeof window.gtag !== "function") return;
    ready = true;
    while (queue.length) {
      window.gtag(...queue.shift());
    }
}

function waitForGtag() {
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
}

waitForGtag();

export function gtag(...args) {
  if (DEBUG_MODE && args[0] === "event") {
    const params = args[2] || {};
    args[2] = { debug_mode: true, ...params };
  }
  if (ready && typeof window.gtag === "function") {
    window.gtag(...args);
  } else {
    queue.push(args);
  }
}
