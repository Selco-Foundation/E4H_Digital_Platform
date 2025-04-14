import React from "react";
import { Close } from "./svgindex";

const RemoveableTag = ({ text, onClick, extraStyles, disabled = false }) => (
  <div className="tag" style={{ height: "auto", maxWidth: "100%" }} >
    <span className="text" style={{ height: "auto", wordWrap: "break-word", overflowWrap: "break-word", whiteSpace: "normal", display: "block", maxWidth: "100%" }}>{text}</span>
    <span onClick={disabled ? null : onClick}>
      <Close className="close" style={extraStyles ? extraStyles?.closeIconStyles : {}} />
    </span>
  </div>
);

export default RemoveableTag;
