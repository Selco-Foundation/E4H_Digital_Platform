import React from "react";

const CustomMenuIcon = ({ color = "white" }) => (
  <svg width="24" height="14" viewBox="0 0 24 14" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path
      d="M0 8.33203H2.66667V5.66536H0V8.33203ZM0 13.6654H2.66667V10.9987H0V13.6654ZM0 2.9987H2.66667V0.332031H0V2.9987ZM5.33333 8.33203H24V5.66536H5.33333V8.33203ZM5.33333 13.6654H24V10.9987H5.33333V13.6654ZM5.33333 0.332031V2.9987H24V0.332031H5.33333Z"
      fill={color}
    />
  </svg>
);

export default CustomMenuIcon;
