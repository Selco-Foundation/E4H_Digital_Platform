// Based off of https://github.com/pwa-builder/PWABuilder/blob/main/docs/sw.js

// Bump this version whenever cached files change: the activate handler deletes every other cache.
const CACHE_NAME = 'digit-ui-cache-v3';
const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  '/Screenshot-Saura-eMitra-Home.png'
];

// Install event: cache files. No skipWaiting(): a new worker waits until every tab of the app is closed,
// so open pages are never taken over (and auto-reloaded by index.js) while someone is using them.
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

// cache: 'no-cache' makes the browser revalidate with the server instead of reusing its own HTTP cache.
const networkFirst = request =>
  fetch(request, { cache: 'no-cache' })
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
