import React from "react";

const ReportingSelection = ({ name, question, options, optionKey = "key", selectedKey, onSelect, required = true }) => (
  <fieldset style={{ border: 0, padding: 0, margin: 0, minWidth: 0 }}>
    <legend className="im-report-question">{question}</legend>
    {options.map((option) => (
      <label key={option[optionKey]} style={{ display: "flex", alignItems: "center", gap: "12px", padding: "10px 0", minHeight: "44px", boxSizing: "border-box", cursor: "pointer" }}>
        <input type="radio" name={name} value={option[optionKey]} checked={selectedKey === option[optionKey]}
          required={required} onChange={() => onSelect(option[optionKey])} />
        <span style={{ overflowWrap: "anywhere" }}>{option.name}</span>
      </label>
    ))}
  </fieldset>
);

export default ReportingSelection;
