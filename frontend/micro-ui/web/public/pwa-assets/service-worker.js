// Based off of https://github.com/pwa-builder/PWABuilder/blob/main/docs/sw.js

// Bump this version whenever cached files change: the activate handler deletes every other cache.
const CACHE_NAME = 'digit-ui-cache-v2';
const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  '/Screenshot-Saura-eMitra-Home.png'
];

// Install event: cache files, and take over from an older worker without waiting for all tabs to close.
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
      .then(() => self.skipWaiting())
      .catch(error => {
        console.error('Failed to cache resources during install:', error);
        throw error;
     })
  );
});

// Activate event: clean up old caches and control already-open pages.
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.filter(key => key !== CACHE_NAME)
          .map(key => caches.delete(key))
      )
    ).then(() => self.clients.claim())
  );
});

// Pages and the manifest change with each deploy, so they come from the network first; the cache is only an offline fallback.
const isNetworkFirst = request =>
  request.mode === 'navigate' || new URL(request.url).pathname.endsWith('/manifest.json');

const networkFirst = request =>
  fetch(request)
    .then(response => {
      if (response.ok) {
        const copy = response.clone();
        caches.open(CACHE_NAME).then(cache => cache.put(request, copy));
      }
      return response;
    })
    .catch(() => caches.match(request).then(cached => cached || Promise.reject(new Error('Offline and not cached'))));

// Fetch event: network first for pages and the manifest, cache first for everything else.
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;

  if (isNetworkFirst(event.request)) {
    event.respondWith(networkFirst(event.request));
    return;
  }

  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );
});
