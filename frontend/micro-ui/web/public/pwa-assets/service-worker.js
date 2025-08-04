// Based off of https://github.com/pwa-builder/PWABuilder/blob/main/docs/sw.js



const CACHE_NAME = 'digit-ui-cache-v1';
const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  '/Screenshot-Saura-eMitra-Home.png'
];

// Install event: cache files
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
      .catch(error => {
        console.error('Failed to cache resources during install:', error);
        throw error;
     })
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