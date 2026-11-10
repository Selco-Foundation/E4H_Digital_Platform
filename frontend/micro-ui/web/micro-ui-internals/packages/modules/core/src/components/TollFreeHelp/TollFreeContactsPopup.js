import React, { useEffect, useMemo, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Loader, Phone, PopUp } from "@selco/digit-ui-react-components";

// Same brand colour the login button uses (login.js -> buttonStyle.backgroundColor).
const BRAND_COLOR = "#7a2829";
const MUTED_TEXT = "#505a5f";
const DIVIDER = "#e0e0e0";
const SEARCH_THRESHOLD = 6;

/**
 * Pop-up listing the CRM toll-free contact(s) of EVERY state.
 *
 *   State name
 *   Label                     (e.g. "CRM Helpline"; omitted when not configured)
 *   [phone] number   [Copy]   (tel: link; Copy only on desktop)
 *   Availability              (e.g. "Mon-Sat, 9 AM - 6 PM"; omitted when not configured)
 *
 */
const TollFreeContactsPopup = ({ contacts, isLoading, isError, onRetry, onClose, t }) => {
  const [query, setQuery] = useState("");
  const [copiedNumber, setCopiedNumber] = useState(null);
  const closeButtonRef = useRef(null);
  const isMobile = window?.Digit?.Utils?.browser?.isMobile?.() || false;
  // Desktops usually have no dialer, so they get a Copy button (only when the clipboard API exists).
  const canCopy = !isMobile && !!navigator?.clipboard?.writeText;

  // Login page convention: use the translation when it exists, otherwise an English fallback,
  const tf = (key, fallback) => {
    const translated = t(key);
    return translated === key ? fallback : translated;
  };

  const getStateLabel = (state) => state.stateName || state.stateCode;

  useEffect(() => {
    closeButtonRef.current?.focus();
    const onKeyDown = (event) => {
      if (event.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  // Stop the login page behind the overlay from scrolling while the popup is open (mobile).
  useEffect(() => {
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previousOverflow;
    };
  }, []);

  // Reset the "Copied" label after 2 seconds.
  useEffect(() => {
    if (!copiedNumber) return undefined;
    const timer = setTimeout(() => setCopiedNumber(null), 2000);
    return () => clearTimeout(timer);
  }, [copiedNumber]);

  // Filter by state name or state code (case-insensitive).
  const visibleStates = useMemo(() => {
    const term = query.trim().toLowerCase();
    if (!term) return contacts;
    return contacts.filter((state) => getStateLabel(state).toLowerCase().includes(term) || state.stateCode.toLowerCase().includes(term));
  }, [contacts, query]);

  const copyNumber = async (dial) => {
    try {
      await navigator.clipboard.writeText(dial);
      setCopiedNumber(dial);
    } catch (e) {
      // Clipboard can be blocked (http origin, permissions). The number is still visible.
      console.warn("CRM toll-free: copy failed", e);
    }
  };

  const modalStyle = {
    background: "#ffffff",
    borderRadius: "4px",
    boxShadow: "0 8px 24px rgba(0, 0, 0, 0.2)",
    display: "flex",
    flexDirection: "column",
    left: "50%",
    maxHeight: isMobile ? "calc(100vh - 48px)" : "calc(100vh - 64px)",
    position: "fixed",
    textAlign: "left",
    top: "50%",
    transform: "translate(-50%, -50%)",
    width: isMobile ? "calc(100% - 32px)" : "min(520px, 90vw)",
    zIndex: 10001,
  };
  const renderContact = (state, contact) => (
    <li key={contact.dial} style={{ marginTop: "0.5rem" }}>
      {contact.label && <div style={{ color: MUTED_TEXT, fontSize: "0.875rem" }}>{contact.label}</div>}
      <div style={{ alignItems: "center", display: "flex", flexWrap: "wrap", gap: "0.75rem" }}>
        {/* tel: link -> opens the phone dialer on mobile */}
        <a
          href={`tel:${contact.dial}`}
          aria-label={`${tf("CORE_LOGIN_TOLL_FREE_CALL", "Call")} ${getStateLabel(state)} ${contact.label} ${contact.display}`}
          style={{
            alignItems: "center",
            color: BRAND_COLOR,
            display: "inline-flex",
            fontSize: "1.125rem",
            fontWeight: 700,
            gap: "0.375rem",
            letterSpacing: "0.02em",
            minHeight: isMobile ? "44px" : "auto",
            textDecoration: "none",
          }}
        >
          <Phone fillcolor={BRAND_COLOR} style={{ flexShrink: 0, height: "18px", marginLeft: 0, width: "18px" }} />
          <span style={{ textDecoration: "underline" }}>{contact.display}</span>
        </a>
        {canCopy && (
          <button
            type="button"
            onClick={() => copyNumber(contact.dial)}
            aria-label={`${tf("CORE_COMMON_COPY", "Copy")} ${contact.display}`}
            style={{
              background: "#ffffff",
              border: `1px solid ${BRAND_COLOR}`,
              borderRadius: "2px",
              color: BRAND_COLOR,
              cursor: "pointer",
              fontSize: "0.75rem",
              fontWeight: 700,
              padding: "0.125rem 0.625rem",
            }}
          >
            {copiedNumber === contact.dial ? tf("CORE_COMMON_COPIED", "Copied") : tf("CORE_COMMON_COPY", "Copy")}
          </button>
        )}
      </div>
      {contact.availability && <div style={{ color: MUTED_TEXT, fontSize: "0.875rem" }}>{contact.availability}</div>}
    </li>
  );

  const renderBody = () => {
    if (isLoading) return <Loader />;

    if (isError || contacts.length === 0) {
      return (
        <div style={{ padding: "1rem 0", textAlign: "center" }}>
          <p style={{ marginBottom: "1rem" }}>
            {tf("CORE_LOGIN_TOLL_FREE_NOT_AVAILABLE", "Toll-free numbers are not available right now. Please try again later.")}
          </p>
          {isError && (
            <button
              type="button"
              onClick={onRetry}
              style={{ background: BRAND_COLOR, border: "none", color: "#fff", cursor: "pointer", padding: "0.5rem 1.25rem" }}
            >
              {tf("CORE_COMMON_RETRY", "Retry")}
            </button>
          )}
        </div>
      );
    }

    return (
      <React.Fragment>
        {contacts.length > SEARCH_THRESHOLD && (
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={tf("CORE_LOGIN_TOLL_FREE_SEARCH_STATE", "Search state")}
            aria-label={tf("CORE_LOGIN_TOLL_FREE_SEARCH_STATE", "Search state")}
            style={{ border: "1px solid #b1b4b6", boxSizing: "border-box", marginBottom: "0.5rem", padding: "0.5rem", width: "100%" }}
          />
        )}

        {visibleStates.length === 0 ? (
          <p style={{ textAlign: "center" }}>{tf("CORE_LOGIN_TOLL_FREE_NO_MATCH", "No state matches your search.")}</p>
        ) : (
          // Each block is rendered from ONE MDMS row, so a contact can never drift to another state.
          <ul style={{ listStyle: "none", margin: 0, padding: 0 }}>
            {visibleStates.map((state, index) => (
              <li
                key={state.stateCode || state.stateName}
                style={{
                  borderTop: index === 0 ? "none" : `1px solid ${DIVIDER}`,
                  padding: "0.875rem 0",
                }}
              >
                <h3 style={{ fontSize: "1rem", fontWeight: 700, margin: 0 }}>{getStateLabel(state)}</h3>
                <ul style={{ listStyle: "none", margin: 0, padding: 0 }}>{state.contacts.map((contact) => renderContact(state, contact))}</ul>
              </li>
            ))}
          </ul>
        )}
      </React.Fragment>
    );
  };

  const popup = (
    <PopUp className="crm-toll-free-popup">
      {/* Transparent full-screen layer behind the dialog: clicking the dark area closes the popup.
          It is a sibling of the dialog, so clicks inside the dialog never reach it.
          top/left/right/bottom is used instead of `inset` for older Android WebViews. */}
      <div style={{ bottom: 0, left: 0, position: "fixed", right: 0, top: 0 }} onClick={onClose} aria-hidden="true" />
      <div role="dialog" aria-modal="true" aria-labelledby="crm-toll-free-title" style={modalStyle}>
        <div
          style={{
            alignItems: "center",
            borderBottom: `1px solid ${DIVIDER}`,
            display: "flex",
            gap: "1rem",
            padding: isMobile ? "1rem" : "1.25rem 1.5rem",
          }}
        >
          <h2 id="crm-toll-free-title" style={{ flex: 1, fontSize: isMobile ? "1.25rem" : "1.5rem", fontWeight: 700, margin: 0 }}>
            {tf("CORE_LOGIN_TOLL_FREE_HEADER", "CRM Toll-free Numbers")}
          </h2>
          <button
            ref={closeButtonRef}
            type="button"
            onClick={onClose}
            aria-label={tf("CS_COMMON_CLOSE", "Close")}
            style={{
              background: "transparent",
              border: "none",
              color: "#505a5f",
              cursor: "pointer",
              fontSize: "1.75rem",
              fontWeight: 400,
              lineHeight: 1,
              padding: "0.25rem 0.5rem",
            }}
          >
            ×
          </button>
        </div>

        <div style={{ flex: 1, overflowY: "auto", padding: isMobile ? "0.75rem 1rem 1rem" : "1rem 1.5rem 1.25rem" }}>
          <p style={{ color: MUTED_TEXT, margin: "0 0 0.25rem" }}>
            {tf("CORE_LOGIN_TOLL_FREE_SUBTEXT", "If you are unable to log in, call the CRM toll-free number for your state.")}
          </p>
          {renderBody()}
        </div>
      </div>
    </PopUp>
  );

  // Portal to <body> so the popup does not inherit the login card's centring or stacking context.
  return typeof document !== "undefined" && document.body ? ReactDOM.createPortal(popup, document.body) : popup;
};

export default TollFreeContactsPopup;
