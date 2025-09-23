import React, {useEffect, useState} from "react";
import FormattedDateInput from "../Custom/FormattedDateInput";

const DateRangeInput = ({
  data = {},
  setValue,
  props,
  setError,
  clearErrors,
}) => {

  const {
    name, disableStartDate, disableEndDate,
    minimumStartDate = "", maximumStartDate = "",
    minimumEndDate = "", maximumEndDate = "",
  } = props;
  const [dateRange, setDateRange] = useState(data[name] || {
    startDate: "",
    endDate: "",
  });
  const [mobileView, setMobileView] = useState(window.innerWidth <= 460);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 460);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    const startDate = dateRange.startDate;
    const endDate = dateRange.endDate;

    if (startDate && endDate) {
      if (new Date(startDate) > new Date(endDate)) {
        setError(name, {
          type: "manual",
          message: "Start date cannot be after End date.",
        });
        setDateRange(prev => ({
          ...prev,
          endDate: ""
        }));
        setValue(name, undefined);
      } else {
        clearErrors(name);
        setValue(name, dateRange);
      }
    }
  }, [name, dateRange]);

  const handleDateChange = (key, value) => {
    setDateRange(prevDateRange => ({
      ...prevDateRange,
      [key]: value,
    }))
  }

  return (
    <div
      style={{
        width: "100%",
        maxWidth: "540px",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: "8px",
        flexDirection: mobileView ? "column" : "row",
      }}
    >
      <FormattedDateInput
        className="employee-card-input"
        value={dateRange.startDate}
        min={minimumStartDate}
        max={dateRange.endDate || maximumStartDate}
        onChange={(e) =>
          handleDateChange("startDate", e.target.value)
        }
        disabled={disableStartDate}
      />
      <FormattedDateInput
        className="employee-card-input"
        value={dateRange.endDate}
        min={dateRange.startDate || minimumEndDate}
        max={maximumEndDate}
        onChange={(e) =>
          handleDateChange("endDate", e.target.value)
        }
        disabled={disableEndDate}
      />
    </div>
  );
};

export default DateRangeInput;
