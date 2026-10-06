import React from "react";
import CustomErrorIcon from "../Custom/CustomErrorIcon";

const UploadErrorCard = ({ cardLabel }) => {
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
      <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
        <CustomErrorIcon iconFill={"#B91900"} />
        <span style={{ color: "#B91900", fontSize: "16px", fontWeight: 600 }}>{cardLabel}</span>
      </div>
    </div>
  );
};

export default UploadErrorCard;
