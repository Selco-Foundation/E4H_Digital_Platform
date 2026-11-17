import { CardText, Modal } from "@selco/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { IOS_INSTALL_PROMPT_EVENT } from "../utilities/pwaInstallPrompt";

// The iOS Share glyph (square with an arrow), so the steps match what users see in Safari.
const IosShareIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#007aff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ verticalAlign: "text-bottom" }}>
    <path d="M12 3v12" />
    <path d="M8 7l4-4 4 4" />
    <path d="M7 10H5v11h14V10h-2" />
  </svg>
);

const Heading = ({ label }) => <h1 className="heading-m">{label}</h1>;

/**
 * iOS cannot open an install dialog from a web page, so after login (see showPwaInstallPromptIfEligible)
 * this shows how to add the app to the home screen. Share opens the iOS share sheet via navigator.share().
 */
const IosInstallPrompt = () => {
  const [open, setOpen] = useState(() => window.selcoShowIosInstallPrompt === true);

  useEffect(() => {
    const onShow = () => setOpen(true);
    window.addEventListener(IOS_INSTALL_PROMPT_EVENT, onShow);
    return () => window.removeEventListener(IOS_INSTALL_PROMPT_EVENT, onShow);
  }, []);

  if (!open) return null;

  const close = () => {
    window.selcoShowIosInstallPrompt = false;
    setOpen(false);
  };

  const share = async () => {
    if (!navigator.share) return;
    try {
      // Share the app's start page, which is what gets added to the home screen.
      await navigator.share({ title: "Saura-eMitra", url: `${window.location.origin}/${window.contextPath || "digit-ui"}/` });
      close();
    } catch (error) {
      // AbortError: the user closed the share sheet. Keep the instructions open so they can try again.
      if (error?.name !== "AbortError") console.warn("Could not open the share sheet", error);
    }
  };

  return (
    <Modal
      headerBarMain={<Heading label="Install Saura-eMitra" />}
      actionCancelLabel="OK"
      actionCancelOnSubmit={close}
      actionSaveLabel="Share"
      actionSaveOnSubmit={share}
      hideSubmit={!navigator.share}
      formId="modal-action"
      popupModuleMianStyles={{ padding: "18px" }}
      popupModuleActionBarStyles={{ display: "flex", justifyContent: "flex-end", paddingTop: "18px" }}
    >
      <CardText style={{ margin: 0 }}>Add Saura-eMitra to your home screen for quicker and easier access:</CardText>
      <ol style={{ margin: "12px 0 0", paddingLeft: "20px", lineHeight: 1.6 }}>
        <li>
          Tap Share <IosShareIcon />
        </li>
        <li>Scroll down and tap "Add to Home Screen"</li>
        <li>Tap "Add"</li>
      </ol>
    </Modal>
  );
};

export default IosInstallPrompt;
