import React, { useState, useRef } from "react";
import { Card, CardText, Modal } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const ForgotPassword = ({ setPopup }) => {
  const { t } = useTranslation();
  const { contactPhone, contactEmail } = window?.globalConfigs?.getConfig("SUPPORT_CONTACTS") || {};

  const [copiedText, setCopiedText] = useState(null);
  const timeoutRef = useRef(null);

  const Heading = (props) => {
    return (
      <h1 className="heading-m" style={{ margin: 0, padding: 0 }}>
        {props.label}
      </h1>
    );
  };

  const copyToClipboard = async (text) => {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
      } else {
        const tempInput = document.createElement("input");
        document.body.appendChild(tempInput);
        tempInput.value = text;
        tempInput.select();
        tempInput.setSelectionRange(0, 99999);
        document.execCommand("copy");
        document.body.removeChild(tempInput);
      }
      showCopiedMessage(text);
    } catch (err) {
      console.error("Clipboard copy failed:", err);
    }
  };

  const showCopiedMessage = (text) => {
    setCopiedText(`${text} copied to clipboard!`);
    clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => setCopiedText(null), 3000);
  };

  return (
    <Modal
      headerBarMain={<Heading label={t("NEED_CREDENTIALS_HELP_MSG")} />}
      actionSaveLabel={t("OK")}
      actionSaveOnSubmit={() => {
        setPopup(false);
      }}
      headerBarMainStyle={{ marginBottom: 0, justifyContent: "center" }}
      popupStyles={{ margin: "auto", paddingTop: "1rem" }}
      popupModuleActionBarStyles={{ justifyContent: "center" }}
    >
      <Card style={{ paddingTop: "0px" }}>
        <CardText>
          {t("FORGOT_PASSWORD_INSTRUCTIONS", {
            email: (
              <span
                style={{ color: "blue", cursor: "pointer", textDecoration: "underline" }}
                onTouchStart={(e) => e.preventDefault()} // Prevents mobile popup
                onClick={() => copyToClipboard(contactEmail)}
              >
                {contactEmail}
              </span>
            ),
            phone: (
              <span
                style={{ color: "blue", cursor: "pointer", textDecoration: "underline" }}
                onTouchStart={(e) => e.preventDefault()} // Prevents mobile popup
                onClick={() => copyToClipboard(contactPhone)}
              >
                {contactPhone}
              </span>
            ),
          })}
        </CardText>
      </Card>

      {copiedText && <div style={{ textAlign: "center", color: "green", fontWeight: "bold", marginBottom: "1rem" }}>{copiedText}</div>}
    </Modal>
  );
};

export default ForgotPassword;
