import React from "react";

const CustomArrowRight = ({ color = "white", width = "17", height = "16" }) => (
  <svg width={width} height={height} viewBox="0 0 17 16" fill={color} xmlns="http://www.w3.org/2000/svg">
    <path d="M8.5 0L7.09 1.41L12.67 7H0.5V9H12.67L7.09 14.59L8.5 16L16.5 8L8.5 0Z" fill={color} />
  </svg>
);

export default CustomArrowRight;