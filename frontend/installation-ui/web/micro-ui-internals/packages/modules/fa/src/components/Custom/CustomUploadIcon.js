import React from "react";

const CustomUploadIcon = ({ styles, className, fill = "#B1B4B6", height = "64", width = "64" }) => (
  <svg style={{ ...styles }} className={className} width={width} height={height} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path
      d="M24.0007 42.6667H40.0007V26.6667H50.6673L32.0007 8L13.334 26.6667H24.0007V42.6667ZM13.334 48H50.6673V53.3333H13.334V48Z"
      fill={fill}
    />
  </svg>
);

export default CustomUploadIcon;