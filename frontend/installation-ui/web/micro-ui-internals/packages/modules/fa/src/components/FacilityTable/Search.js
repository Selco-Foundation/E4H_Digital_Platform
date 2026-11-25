import React, { useEffect, useRef, useState } from "react";

const DEBOUNCE_MS = 500;

/**
 * The View Facilities search box. Reports the trimmed term once typing pauses, or at once when
 * cleared; the server matches it against name, HFR ID, NIN and PoC username.
 */
const Search = ({ t, value = "", onSearch }) => {
  const [text, setText] = useState(value);
  const reported = useRef(value.trim());

  // A term restored from the URL, or reset from outside, replaces what is in the box.
  useEffect(() => {
    if (value.trim() !== reported.current) {
      reported.current = value.trim();
      setText(value);
    }
  }, [value]);

  const report = (term) => {
    if (term !== reported.current) {
      reported.current = term;
      onSearch(term);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => report(text.trim()), DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [text]);

  const clear = () => {
    setText("");
    report("");
  };

  return (
    <div style={{ position: "relative", flex: 1, maxWidth: "520px" }}>
      <input
        type="text"
        className="employee-card-input"
        aria-label={t("FA_FACILITY_SEARCH_PLACEHOLDER")}
        placeholder={t("FA_FACILITY_SEARCH_PLACEHOLDER")}
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && report(text.trim())}
        style={{ width: "100%", margin: 0, paddingRight: "40px", height: "40px" }}
      />
      {text && (
        <button
          type="button"
          aria-label={t("FA_FACILITY_SEARCH_CLEAR")}
          title={t("FA_FACILITY_SEARCH_CLEAR")}
          onClick={clear}
          style={{
            position: "absolute",
            right: "8px",
            top: "50%",
            transform: "translateY(-50%)",
            border: "none",
            background: "transparent",
            cursor: "pointer",
            fontSize: "20px",
            lineHeight: "20px",
            color: "#505A5F",
          }}
        >
          ×
        </button>
      )}
    </div>
  );
};

export default Search;
