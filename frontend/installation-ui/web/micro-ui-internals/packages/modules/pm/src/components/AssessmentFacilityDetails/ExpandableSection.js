import React, { useState } from "react";

const ExpandableSection = ({ title, defaultExpanded = false, children }) => {

  const [expanded, setExpanded] = useState(defaultExpanded);

  return (
    <div
      style={{
        width: "100%",
        background: "white",
        marginBottom: "15px",
        borderRadius: "4px",
        boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
        overflow: "hidden",
      }}
    >
      <div
        style={{
          padding: "16px 20px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          borderBottom: expanded ? "1px solid #eee" : "none",
          cursor: "pointer",
        }}
        onClick={() => setExpanded((prev) => !prev)}
      >
        <div style={{ fontSize: "18px", fontWeight: 700, color: "#0B0C0C" }}>{title}</div>
        <button
          type="button"
          style={{
            width: "25px",
            height: "25px",
            borderRadius: "5px",
            background: "#0B4B66",
            color: "white",
            fontSize: "20px",
            fontWeight: "bold",
            border: "none",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            flexShrink: 0,
          }}
          onClick={(e) => {
            e.stopPropagation();
            setExpanded((prev) => !prev);
          }}
        >
          {expanded ? "−" : "+"}
        </button>
      </div>
      {expanded && (
        <div style={{ padding: "16px 20px" }}>
          {children}
        </div>
      )}
    </div>
  );
};

export default ExpandableSection;
