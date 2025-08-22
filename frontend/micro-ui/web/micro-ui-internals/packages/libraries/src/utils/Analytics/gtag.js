import { GA_MEASUREMENT_ID, DEBUG_MODE } from './config';

const queue = [];
let ready = false;
let loading = false; // prevent duplicate, in-flight loads

function flush() {
  const g = window.gtag;
  if (!g) return;
  ready = true;
  while (queue.length) g(...queue.shift());
}

export function loadGA() {
  if (typeof window === 'undefined') return;

  if (typeof window.gtag === 'function') {
    flush();
    return;
  }


  if (loading) return;
  loading = true;

  const s = document.createElement('script');
  s.async = true;
  s.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`;
  s.onload = () => {
    try {
      window.gtag('js', new Date());
      window.gtag('config', GA_MEASUREMENT_ID, {
        anonymize_ip: true,
        debug_mode: DEBUG_MODE,
        // If you manually track SPA route changes, consider:
        // send_page_view: false,
        // allow_ad_personalization_signals: false,
        // transport_type: 'beacon',
      });
      flush();
      if (DEBUG_MODE) console.log('✅ GA loaded:', GA_MEASUREMENT_ID);
    } finally {
      loading = false;
    }
  };

  s.onerror = (e) => {
    loading = false;
    console.error('[GA] Failed to load gtag.js', e);
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
