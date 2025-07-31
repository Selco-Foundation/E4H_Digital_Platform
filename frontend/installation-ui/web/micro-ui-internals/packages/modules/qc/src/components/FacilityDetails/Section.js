import React from "react";

const Section = ({ title, children }) => (
  <div style={{ backgroundColor: "#FAFAFA", padding: "15px", marginBottom: "20px", borderRadius: "4px" }}>
    <h2 style={{ color: "#0B3954" }}>{title}</h2>
    <div>
      {children}
    </div>
  </div>
);

export default Section;