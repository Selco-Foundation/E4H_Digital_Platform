import React, {useEffect, useState} from "react";
import _ from "lodash";

const DateRangeInput = ({
  register,
  setValue,
  props,
  setError,
  clearErrors,
}) => {

  const { name, defaultValues = {} } = props;
  const [dateRange, setDateRange] = useState({
    startDate: "",
    endDate: "",
  });

  useEffect(() => {
    if (defaultValues[name] && !_.isEqual(defaultValues[name], dateRange)) {
      setDateRange(defaultValues[name]);
    }
  }, [defaultValues[name]]);

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
      }}
    >
      <input
        style={{ width: "240px" }}
        className="employee-card-input"
        type="date"
        value={dateRange.startDate}
        {...register(`${name}.startDate`)}
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
        {...register(`${name}.endDate`)}
        onChange={(e) =>
          handleDateChange("endDate", e.target.value)
        }
      />
    </div>
  );
};

export default DateRangeInput;
