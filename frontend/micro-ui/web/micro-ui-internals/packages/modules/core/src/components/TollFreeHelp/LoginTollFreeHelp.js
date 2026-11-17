import React, { useCallback, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import TollFreeContactsPopup from "./TollFreeContactsPopup";
import useCrmTollFreeContacts from "./useCrmTollFreeContacts";

/**
 * "Unable to log in? Call toll-free" link for the employee login page
 *
 * Renders only public, pre-authentication information: it does not read the user, token or tenant
 * of a logged-in session, so it is safe to show before login.
 *
 */
const LoginTollFreeHelp = ({ t: tFromProps }) => {
  const { t: tFromHook } = useTranslation();
  const t = tFromProps || tFromHook;

  const [isOpen, setIsOpen] = useState(false);
  // Remember the link so keyboard focus can go back to it when the popup closes.
  const triggerRef = useRef(null);

  // Data is fetched only once the popup is opened (see the hook for the reasoning).
  const { contacts, isLoading, isError, refetch } = useCrmTollFreeContacts(isOpen);

  const translated = t("CORE_LOGIN_UNABLE_TO_LOGIN_CALL_TOLL_FREE");
  const linkLabel = translated === "CORE_LOGIN_UNABLE_TO_LOGIN_CALL_TOLL_FREE" ? "Unable to log in? Call toll-free" : translated;

  const openPopup = (event) => {
    event.preventDefault();
    setIsOpen(true);
    try {
      window?.Digit?.Utils?.analytics?.trackButtonClick?.("login_toll_free_help", { page_name: "login_page" });
    } catch (e) {
      /* analytics must never break the login page */
    }
  };

  // useCallback keeps the reference stable, so the popup's Escape-key listener is not re-bound.
  const closePopup = useCallback(() => {
    setIsOpen(false);
    triggerRef.current?.focus();
  }, []);

  return (
    <div style={{ textAlign: "center", marginTop: "0.75rem" }}>
      <button
        ref={triggerRef}
        type="button"
        onClick={openPopup}
        aria-haspopup="dialog"
        aria-expanded={isOpen}
        style={{
          background: "transparent",
          border: "none",
          color: "blue", //check
          cursor: "pointer",
          padding: 0,
          textDecoration: "underline",
        }}
      >
        {linkLabel}
      </button>

      {isOpen && (
        <TollFreeContactsPopup
          contacts={contacts}
          isLoading={isLoading}
          isError={isError}
          onRetry={refetch}
          onClose={closePopup}
          t={t}
        />
      )}
    </div>
  );
};

export default LoginTollFreeHelp;
