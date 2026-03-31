import React from "react";
import CustomErrorIcon from "../Custom/CustomErrorIcon";

const UploadErrorCard = ({ t, viewActionLabel, cardLabel, onViewErrors }) => {
  return (
    <div
      style={{
        border: "1px solid #B91900",
        borderLeft: "4px solid #B91900",
        backgroundColor: "#fff6f5",
        padding: "16px",
        borderRadius: "4px",
        fontFamily: "Arial, sans-serif",
        color: "#B91900",
      }}
    >
      <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "15px" }}>
        <CustomErrorIcon iconFill={"#B91900"} />
        <span style={{ color: "#B91900", fontSize: "16px", fontWeight: 500 }}>{cardLabel}</span>
      </div>

      <button
        type={"button"}
        onClick={onViewErrors}
        style={{
          backgroundColor: "#B91900",
          border: "none",
          padding: "10px 20px",
          color: "#fff",
          fontSize: "14px",
          fontWeight: "bold",
          borderRadius: "4px",
          cursor: "pointer",
        }}
      >
        {t(viewActionLabel)}
      </button>
    </div>
  );
};

export default UploadErrorCard;
