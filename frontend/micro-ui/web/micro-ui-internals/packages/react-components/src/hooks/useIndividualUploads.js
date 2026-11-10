import { useEffect, useMemo, useState } from "react";
import { UploadRecoveryService } from "../services/UploadRecovery";
import { IndividualUploadService } from "../services/IndividualUpload";

// Retain selected File objects during navigation within the current browser session.
// IndexedDB restores pending files after refresh; incomplete requests require an explicit retry.
const uploadQueues = new Map();
const newQueue = () => ({ entries: [], nextId: 0, listeners: new Set(), ready: false, recoveryUnavailable: false, writes: Promise.resolve() });

const useIndividualUploads = ({ enabled, queueKey, module, tenantId, onResult }) => {
  const queue = useMemo(() => {
    if (!enabled || !queueKey) return newQueue();
    if (!uploadQueues.has(queueKey)) uploadQueues.set(queueKey, newQueue());
    return uploadQueues.get(queueKey);
  }, [enabled, queueKey]);
  const [entries, setEntries] = useState(queue.entries);
  useEffect(() => {
    setEntries(queue.entries);
    queue.listeners.add(setEntries);
    if (!queue.restore) {
      queue.restore = (enabled && queueKey ? UploadRecoveryService.read(queueKey) : Promise.resolve([]))
        .then((restored) => {
          queue.entries = restored.map((entry) => ({ ...entry, status: entry.status === "uploaded" ? "uploaded" : "failed" }));
          queue.nextId = queue.entries.reduce((max, entry) => Math.max(max, entry.id), 0);
        }).catch(() => { queue.recoveryUnavailable = true; })
        .finally(() => {
          queue.ready = true;
          queue.listeners.forEach((listener) => listener([...queue.entries]));
        });
    }
    return () => {
      queue.listeners.delete(setEntries);
      queue.writes.then(() => {
        if (queue.ready && !queue.entries.length && !queue.listeners.size && uploadQueues.get(queueKey) === queue) uploadQueues.delete(queueKey);
      });
    };
  }, [queue, enabled, queueKey]);
  const persist = () => {
    if (!enabled || !queueKey || queue.recoveryUnavailable) return Promise.resolve();
    const snapshot = queue.entries;
    queue.writes = queue.writes.then(() => UploadRecoveryService.save(queueKey, snapshot)).catch(() => {
      queue.recoveryUnavailable = true;
      queue.listeners.forEach((listener) => listener([...queue.entries]));
    });
    return queue.writes;
  };
  const publish = (values, save = true) => {
    queue.entries = values;
    queue.listeners.forEach((listener) => listener(values));
    if (save) persist();
  };
  const update = (id, changes, save = true) => {
    const current = queue.entries.find((entry) => entry.id === id);
    if (!current || Object.keys(changes).every((key) => current[key] === changes[key])) return;
    publish(queue.entries.map((entry) => entry.id === id ? { ...entry, ...changes } : entry), save);
  };
  const run = async (id) => {
    if (!queue.ready) return;
    const entry = queue.entries.find((item) => item.id === id);
    if (!entry || !["queued", "failed"].includes(entry.status)) return;
    // Update synchronously before awaiting to prevent repeated Retry clicks.
    update(id, { status: "uploading", progress: null });
    try {
      const reference = await IndividualUploadService.upload(module, entry.file, tenantId, (event) => {
        const progress = event.total > 0 ? Math.min(100, Math.floor(event.loaded * 100 / event.total)) : null;
        update(id, { progress, status: progress === 100 ? "processing" : "uploading" }, false);
      });
      update(id, { status: "uploaded", progress: 100, reference, fileStoreId: reference.fileStoreId });
      if (onResult) onResult(entry.file, "success");
    } catch (error) {
      update(id, { status: "failed" });
      if (onResult) onResult(entry.file, "error");
    }
  };
  const add = async (files) => {
    await queue.restore;
    const added = files.map((file) => ({ id: ++queue.nextId, file, name: file.name, status: "queued", progress: null }));
    publish([...queue.entries, ...added]);
    await queue.writes;
    await Promise.allSettled(added.map(({ id }) => run(id)));
  };
  const remove = (id) => {
    const entry = queue.entries.find((item) => item.id === id);
    if (entry?.status === "failed") publish(queue.entries.filter((item) => item.id !== id));
  };
  return { entries, add, retry: run, remove, acknowledgeUploads: () => publish(queue.entries.filter(({ status }) => status !== "uploaded")), restoring: !queue.ready, recoveryUnavailable: queue.recoveryUnavailable, unresolved: !queue.ready || entries.some(({ status }) => status !== "uploaded") };
};

export default useIndividualUploads;
