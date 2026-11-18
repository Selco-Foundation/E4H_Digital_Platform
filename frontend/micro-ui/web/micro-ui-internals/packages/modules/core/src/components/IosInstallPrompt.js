import { ActionBar, CardText, Close } from "@selco/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { IOS_INSTALL_PROMPT_EVENT } from "../utilities/pwaInstallPrompt";

// DIGIT's ActionBar is the standard white bar fixed to the bottom of the screen. These overrides lay out
// text + buttons in one row, keep it above page action bars and clear of the iPhone home indicator.
const barStyle = {
  alignItems: "center",
  display: "flex",
  gap: "12px",
  paddingBottom: "calc(8px + env(safe-area-inset-bottom))",
  textAlign: "left",
  zIndex: 1000,
};

// Which iOS browser we are in, from its user agent marker. Every iOS browser uses Safari's engine,
// but only real Safari has "Version/x Safari" without another browser's or an in-app browser's marker.
const getIosBrowser = () => {
  const ua = window.navigator.userAgent;
  if (/CriOS/.test(ua)) return "chrome";
  if (/Version\/[\d.]+.*Safari/.test(ua) && !/FxiOS|EdgiOS|FBAN|FBAV/.test(ua)) return "safari";
  return "other";
};

// Where the Share button is in each browser. Safari's compact tab bar can hide it behind the ⋯ menu.
const INSTRUCTIONS = {
  safari: `Install Saura-eMitra: tap Share in Safari's toolbar (or ⋯ and then Share), then "Add to Home Screen".`,
  chrome: `Install Saura-eMitra: tap Share Icon in Chrome's address bar, then "Add to Home Screen".`,
  other: `Install Saura-eMitra: tap the Share button in your browser's toolbar, then "Add to Home Screen".`,
};

/**
 * iOS cannot open an install dialog from a web page, so after login (see showPwaInstallPromptIfEligible)
 * this shows a bottom banner explaining how to add the app to the home screen.
 * There is deliberately no Share button: the sheet opened by navigator.share() never contains
 * "Add to Home Screen" (iOS only offers it from the browser's own Share button), so we point users there.
 */
const IosInstallPrompt = () => {
  const [open, setOpen] = useState(() => window.selcoShowIosInstallPrompt === true);

  useEffect(() => {
    const onShow = () => setOpen(true);
    window.addEventListener(IOS_INSTALL_PROMPT_EVENT, onShow);
    return () => window.removeEventListener(IOS_INSTALL_PROMPT_EVENT, onShow);
  }, []);

  useEffect(() => {
    // The banner appears right after login, while the password field may still have focus.
    // Blurring it closes the iOS keyboard so the banner is not pushed up or hidden behind it.
    if (open) document.activeElement?.blur?.();
  }, [open]);

  if (!open) return null;

  const close = () => {
    window.selcoShowIosInstallPrompt = false;
    setOpen(false);
  };

  // Children are not divs on purpose: ActionBar's CSS makes every nested div full width.
  return (
    <ActionBar style={barStyle}>
      <CardText style={{ flex: 1, margin: 0 }}>{INSTRUCTIONS[getIosBrowser()]}</CardText>
      <button type="button" onClick={close} aria-label="Close" style={{ background: "none", border: "none", cursor: "pointer", flexShrink: 0, padding: 0 }}>
        <Close />
      </button>
    </ActionBar>
  );
};

export default IosInstallPrompt;
