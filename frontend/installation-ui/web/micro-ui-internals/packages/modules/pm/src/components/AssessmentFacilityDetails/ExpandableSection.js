import React, { useState } from "react";

const ExpandableSection = ({ title, defaultExpanded = false, children }) => {

  const [expanded, setExpanded] = useState(defaultExpanded);

  const toggleExpanded = () => setExpanded((prev) => !prev);

  return (
    <div
      style={{
        marginTop: "15px",
        width: "100%",
        padding: "20px",
        background: "white",
        borderRadius: "4px",
        boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
        border: "1px solid #eee",
        borderTop: "none",
        borderBottom: "none",
        transition: "all 0.3s ease-in-out",
      }}
    >
      <div
        style={{
          padding: "16px 20px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          backgroundColor: "#fff",
          borderBottom: expanded ? "1px solid #eee" : "none",
          cursor: "pointer",
        }}
        onClick={toggleExpanded}
      >
        <div
          style={{
            margin: 0,
            color: "#0B4B66",
            fontSize: "32px",
            fontWeight: "bold",
            maxWidth: "100%",
            overflowWrap: "break-word",
          }}
        >
          {title}
        </div>
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
            toggleExpanded();
          }}
        >
          {expanded ? "−" : "+"}
        </button>
      </div>
      {expanded && (
        <div style={{ padding: "20px" }}>
          {children}
        </div>
      )}
    </div>
  );
};

export default ExpandableSection;
