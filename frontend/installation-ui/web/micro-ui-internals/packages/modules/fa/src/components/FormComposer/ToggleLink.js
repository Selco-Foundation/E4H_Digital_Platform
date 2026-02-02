import CustomSwapHorizontalCircle from "../Custom/CustomSwapHorizontalCircle";
import React from "react";

const ToggleLink = ({ label, onClick, disable, ariaLabel }) => {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disable}
      aria-label={ariaLabel || label || "Toggle input mode"}
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: "6px",
        border: "none",
        background: "transparent",
        cursor: disable ? "not-allowed" : "pointer",
        padding: 0,
        color: "#0B0C0C",
        fontSize: "16px",
        fontFamily: "Roboto Condensed",
        fontWeight: 600,
        whiteSpace: "nowrap",
        flexShrink: 0,
      }}
    >
      <CustomSwapHorizontalCircle size={28} color="#0B0C0C" style={{ opacity: disable ? 0.6 : 1 }} />
      {!!label && <span style={{ opacity: 0.6 }}>{label}</span>}
      <span style={{ opacity: 0.6 }}>{label}</span>
    </button>
  );
};

export default ToggleLink;