const RECOVERY_DB = "digit-reporting-upload-recovery";
const RECOVERY_TTL = 24 * 60 * 60 * 1000;

const recoveryKey = (key) => {
  let tab = window.sessionStorage.getItem("Digit.uploadRecoveryTab");
  if (!tab) {
    tab = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
    window.sessionStorage.setItem("Digit.uploadRecoveryTab", tab);
  }
  return `${tab}:${key}`;
};
const openRecovery = () => new Promise((resolve, reject) => {
  if (!window.indexedDB) { reject(new Error("Upload recovery storage is unavailable")); return; }
  const request = window.indexedDB.open(RECOVERY_DB, 1);
  request.onupgradeneeded = () => request.result.createObjectStore("queues");
  request.onerror = () => reject(request.error);
  request.onblocked = () => reject(new Error("Upload recovery storage is blocked"));
  request.onsuccess = () => resolve(request.result);
});

export const UploadRecoveryService = {
  read: async (key) => {
    const scopedKey = recoveryKey(key);
    const db = await openRecovery();
    return new Promise((resolve, reject) => {
      let entries = [];
      const transaction = db.transaction("queues", "readwrite");
      const store = transaction.objectStore("queues");
      const cursor = store.openCursor();
      cursor.onsuccess = () => {
        const current = cursor.result;
        if (!current) return;
        if (current.value.expires <= Date.now()) current.delete();
        else if (current.key === scopedKey) entries = current.value.entries;
        current.continue();
      };
      transaction.oncomplete = () => { db.close(); resolve(entries); };
      transaction.onabort = transaction.onerror = () => { db.close(); reject(transaction.error); };
    });
  },
  save: async (key, entries) => {
    const scopedKey = recoveryKey(key);
    const db = await openRecovery();
    return new Promise((resolve, reject) => {
      const transaction = db.transaction("queues", "readwrite");
      const store = transaction.objectStore("queues");
      if (entries.length) store.put({ entries, expires: Date.now() + RECOVERY_TTL }, scopedKey);
      else store.delete(scopedKey);
      transaction.oncomplete = () => { db.close(); resolve(); };
      transaction.onabort = transaction.onerror = () => { db.close(); reject(transaction.error); };
    });
  },
};
