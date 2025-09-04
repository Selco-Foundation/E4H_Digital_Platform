import React from "react";

const CustomDownloadIcon = ({ styles, height="24", width = "19", className, onClick, fill = "#505A5F" }) => (
  <svg
    style={{ ...styles }}
    width={height}
    height={width}
    viewBox="0 0 19 24"
    fill={fill}
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    onClick={onClick}
  >
    <path d="M18.8337 8.5H13.5003V0.5H5.50033V8.5H0.166992L9.50033 17.8333L18.8337 8.5ZM0.166992 20.5V23.1667H18.8337V20.5H0.166992Z" fill={fill} />
  </svg>
);

export default CustomDownloadIcon;