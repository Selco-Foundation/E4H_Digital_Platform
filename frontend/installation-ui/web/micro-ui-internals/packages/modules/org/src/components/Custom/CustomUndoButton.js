import React from "react";

const CustomUndoIcon = ({
  style,
  width = "16",
  height = "16",
  viewBox = "0 0 24 24",
  colourFill = "#c84c0e",
  strokeWidth = "2.2"
}) => (
  <svg
    style={style}
    width={width}
    height={height}
    viewBox={viewBox}
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M9 7H5V3"
      stroke={colourFill}
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <path
      d="M5 7C7 5 9.5 4 12 4C16.5 4 20 7.5 20 12C20 16.5 16.5 20 12 20C8.5 20 5.5 17.7 4.4 14.6"
      stroke={colourFill}
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

export default CustomUndoIcon;