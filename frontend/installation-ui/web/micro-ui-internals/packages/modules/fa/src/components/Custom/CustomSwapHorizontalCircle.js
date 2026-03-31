import React from "react";

const CustomSwapHorizontalCircle = ({
                                      size = 28,
                                      color = "#0B0C0C",
                                      strokeWidth = 2,
                                      style = {}
                                    }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      style={{display: "block", ...style}}
    >
      <circle cx="12" cy="12" r="10" stroke={color} strokeWidth={strokeWidth}/>
      <path
        d="M7 9H16"
        stroke={color}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
      />
      <path
        d="M14.2 7.2L16 9L14.2 10.8"
        stroke={color}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M17 15H8"
        stroke={color}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
      />
      <path
        d="M9.8 13.2L8 15L9.8 16.8"
        stroke={color}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
};

export default CustomSwapHorizontalCircle;