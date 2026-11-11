import React, { useEffect, useState } from "react";

const OfflineBanner = () => {
  const [offline, setOffline] = useState(!navigator.onLine);

  useEffect(() => {
    const onOnline = () => setOffline(false);
    const onOffline = () => setOffline(true);

    window.addEventListener("online", onOnline);
    window.addEventListener("offline", onOffline);
    return () => {
      window.removeEventListener("online", onOnline);
      window.removeEventListener("offline", onOffline);
    };
  }, []);

  return offline ? (
    <div role="status" style={{ background: "#b42318", color: "#fff", left: 0, padding: "10px 16px", position: "fixed", right: 0, textAlign: "center", top: 0, zIndex: 10002 }}>
      No Internet Connection
    </div>
  ) : null;
};

export default OfflineBanner;
