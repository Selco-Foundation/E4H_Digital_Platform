import { ActionBar, CardText, Close, SubmitBar } from "@selco/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { IOS_INSTALL_PROMPT_EVENT } from "../utilities/pwaInstallPrompt";

// The iOS Share glyph (square with an arrow), so the instructions match what users see in Safari.
const IosShareIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#007aff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ verticalAlign: "text-bottom" }}>
    <path d="M12 3v12" />
    <path d="M8 7l4-4 4 4" />
    <path d="M7 10H5v11h14V10h-2" />
  </svg>
);

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

/**
 * iOS cannot open an install dialog from a web page, so after login (see showPwaInstallPromptIfEligible)
 * this shows a bottom banner explaining how to add the app to the home screen.
 * Share opens the iOS share sheet via navigator.share().
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

  const share = async () => {
    try {
      // Share the app's start page, which is what gets added to the home screen.
      await navigator.share({ title: "Saura-eMitra", url: `${window.location.origin}/${window.contextPath || "digit-ui"}/` });
      close();
    } catch (error) {
      // AbortError: the user closed the share sheet. Keep the banner so they can try again.
      if (error?.name !== "AbortError") console.warn("Could not open the share sheet", error);
    }
  };

  // Children are not divs on purpose: ActionBar's CSS makes every nested div full width.
  return (
    <ActionBar style={barStyle}>
      <CardText style={{ flex: 1, margin: 0 }}>
        Install Saura-eMitra: tap Share <IosShareIcon />, then "Add to Home Screen".
      </CardText>
      {navigator.share && <SubmitBar label="Share" onSubmit={share} style={{ flexShrink: 0, padding: "0 16px", width: "auto" }} />}
      <button type="button" onClick={close} aria-label="Close" style={{ background: "none", border: "none", cursor: "pointer", flexShrink: 0, padding: 0 }}>
        <Close />
      </button>
    </ActionBar>
  );
};

export default IosInstallPrompt;
