
import { GA_MEASUREMENT_ID, DEBUG_MODE } from './config';

const queue = [];
let ready = false;

function flush() {
  const g = window.gtag;
  if (!g) return;
  ready = true;
  while (queue.length) g(...queue.shift());
}

export function loadGA() {
  if (typeof window === 'undefined') return;
  if (window.gtag) { ready = true; return; }

  const s = document.createElement('script');
  s.async = true;
  s.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`;
  s.onload = () => {
    window.dataLayer = window.dataLayer || [];
    window.gtag = function(){ window.dataLayer.push(arguments); };
    window.gtag('js', new Date());
    window.gtag('config', GA_MEASUREMENT_ID, {
      anonymize_ip: true,
      debug_mode: DEBUG_MODE
    });
    flush();
    console.log('✅ GA loaded:', GA_MEASUREMENT_ID);
  };
  document.head.appendChild(s);
}

export function gtag(...args) {
  if (ready && typeof window.gtag === 'function') {
    window.gtag(...args);
  } else {
    queue.push(args);
  }
}
