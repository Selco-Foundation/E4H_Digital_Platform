import React, { useRef } from "react";
import { format } from "date-fns";
import { DateRange } from "@egovernments/digit-ui-svg-components";

const FormattedDateInput = ({
  value,
  onChange,
  style = {},
  className,
  dateFormat = "dd MMMM yyyy",
  disabled = false,
  ...props
}) => {

  const inputRef = useRef(null);

  const handleDatePickerOpen = (e) => {
    if (disabled) return;
    inputRef?.current?.showPicker();
  }

  return (
    <div
      className={className}
      onClick={handleDatePickerOpen}
      style={{
        position: "relative",
        display: "inline-flex",
        alignItems: "center",
        border: "1px solid #444",
        padding: "8px 12px",
        justifyContent: "space-between",
        cursor: disabled ? "default" : "pointer",
        opacity: disabled ? 0.5 : 1,
        ...style,
      }}
    >
      <input
        ref={inputRef}
        type="date"
        value={value}
        onChange={onChange}
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          opacity: 0,
          pointerEvents: "none",
        }}
        disabled={disabled}
        {...props}
      />
      <span
        style={{
          fontSize: "16px",
          fontWeight: "Roboto",
          color: "#111"
        }}
      >
        {value ? format(new Date(value), dateFormat) : ""}
      </span>
      <DateRange fill={"#505A5F"} />
    </div>
  );
}

export default FormattedDateInput;