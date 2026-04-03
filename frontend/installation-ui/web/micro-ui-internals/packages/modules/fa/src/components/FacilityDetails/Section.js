import React from "react";

const Section = ({ title, children }) => (
  <div style={{
    backgroundColor: "#FAFAFA",
    padding: "15px",
    border: "1px solid #D6D5D4",
    marginBottom: "20px",
    borderRadius: "4px"
  }}>
    <div style={{
      color: "#0B3954",
      fontSize: "20px",
      marginBottom: "10px",
    }}>
      {title}
    </div>
    <div>
      {children}
    </div>
  </div>
);

export default Section;