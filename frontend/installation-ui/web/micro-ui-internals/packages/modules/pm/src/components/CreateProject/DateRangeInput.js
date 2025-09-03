import React, {useEffect, useState} from "react";

const DateRangeInput = ({
  data = {},
  register,
  setValue,
  props,
  setError,
  clearErrors,
}) => {
  const [dateRange, setDateRange] = useState(data?.projectDates || {
    startDate: "",
    endDate: "",
  });
  const { name } = props;

  useEffect(() => {
    const startDate = dateRange.startDate;
    const endDate = dateRange.endDate;

    if (startDate && endDate) {
      if (new Date(startDate) > new Date(endDate)) {
        setError(name, {
          type: "manual",
          message: "Start date cannot be after End date.",
        });
        setDateRange(prevDateRange => ({
          ...prevDateRange,
          endDate: ""
        }));
        setValue(name, null);
      } else {
        clearErrors(name);
        setValue(name, { startDate, endDate });
      }
    }
  }, [dateRange, setError, clearErrors, name]);

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
      }}
    >
      <input
        style={{ width: "240px" }}
        className="employee-card-input"
        type="date"
        value={dateRange.startDate}
        {...register("projectDates.startDate")}
        onChange={(e) =>
          handleDateChange("startDate", e.target.value)
        }
      />
      <input
        style={{ width: "240px" }}
        className="employee-card-input"
        type="date"
        value={dateRange.endDate}
        min={dateRange.startDate}
        {...register("projectDates.endDate")}
        onChange={(e) =>
          handleDateChange("endDate", e.target.value)
        }
      />
    </div>
  );
};

export default DateRangeInput;
