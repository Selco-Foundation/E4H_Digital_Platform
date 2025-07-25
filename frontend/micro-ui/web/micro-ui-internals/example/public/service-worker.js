// Based off of https://github.com/pwa-builder/PWABuilder/blob/main/docs/sw.js



const CACHE_NAME = 'digit-ui-cache-v1';
const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  'micro-ui/web/public/manifest-icon-512.maskable.png',
  'micro-ui/web/public/Screenshot-Saura-eMitra-Home.png',
  '/Screenshot-Saura-eMitra-Home.png',
  '/manifest-icon-512.maskable.png',
  'micro-ui/web/public/manifest-icon-192.maskable.png',
  'manifest-icon-192.maskable.png',

];

// Install event: cache files
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

// Activate event: clean up old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.filter(key => key !== CACHE_NAME)
          .map(key => caches.delete(key))
      )
    )
  );
});

// Fetch event: serve cached files if offline
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );
});