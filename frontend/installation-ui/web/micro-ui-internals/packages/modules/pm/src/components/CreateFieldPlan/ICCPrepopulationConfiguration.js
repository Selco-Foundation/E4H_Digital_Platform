import React, { useEffect, useRef, useState } from "react";
import CustomDropdown from "../Custom/CustomDropdown";
import CustomCloseSvg from "../Custom/CustomCloseSvg";
import CustomFileIcon from "../File/CustomFileIcon";
import CustomUploadIcon from "../Custom/CustomUploadIcon";

const getDefaultRows = () => [
  {
    id: "icc-row-3",
    systemType: null,
    totalSystemCapacity: null,
    file: null,
  },
];

const UploadControl = ({ t, row, onFileSelect }) => {

  const fileInputRef = useRef(null);

  const handleFileChange = (event) => {
    const uploadedFile = event.target.files?.[0];
    if (uploadedFile) {
      onFileSelect(uploadedFile);
    }
  };

  return (
    <button
      type="button"
      onClick={() => fileInputRef.current?.click()}
      style={{
        border: "1px dashed #D6D5D4",
        backgroundColor: "#FAFAFA",
        color: row.file ? "#C84C0E" : "#787878",
        height: "40px",
        minWidth: "260px",
        padding: "0px 16px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "8px",
        cursor: "pointer",
        fontFamily: "Roboto",
        fontSize: "14px",
        overflow: "hidden",
      }}
    >
      <input
        ref={fileInputRef}
        type="file"
        accept=".xlsx,.xls"
        style={{ display: "none" }}
        onChange={handleFileChange}
      />
      {row.file ? (
        <CustomFileIcon file={row.file} width={"18px"} height={"18px"} />
      ) : (
        <CustomUploadIcon fill={"#787878"} height={"18px"} width={"18px"} />
      )}
      <span
        style={{
          overflow: "hidden",
          textOverflow: "ellipsis",
          whiteSpace: "nowrap",
        }}
      >
        {row.file?.name || t("CORE_COMMON_CHOOSE_FILE")}
      </span>
    </button>
  );
};

const ICCPrepopulationConfiguration = ({ data = {}, setValue, props }) => {

  const { t, name } = props;
  const [rows, setRows] = useState(data[name] || getDefaultRows());
  const [systemTypeOptions] = useState([]);
  const [capacityOptions] = useState([]);

  useEffect(() => {
    setValue(name, rows);
  }, [name, rows, setValue]);

  const updateRow = (rowId, fieldName, fieldValue) => {
    setRows((prevRows) => prevRows.map((row) => {
      if (row.id !== rowId) return row;

      return {
        ...row,
        [fieldName]: fieldValue,
      };
    }));
  };

  const addRow = () => {
    setRows((prevRows) => ([
      ...prevRows,
      {
        id: `icc-row-${Date.now()}`,
        systemType: null,
        totalSystemCapacity: null,
        file: null,
      },
    ]));
  };

  const deleteRow = (rowId) => {
    setRows((prevRows) => {
      const updatedRows = prevRows.filter((row) => row.id !== rowId);
      return updatedRows.length ? updatedRows : getDefaultRows();
    });
  };

  const FieldLabel = ({ label }) => (
    <label
      style={{
        color: "#505A5F",
        fontSize: "12px",
        fontWeight: "700",
        fontFamily: "Roboto",
        display: "block",
        marginBottom: "6px",
        minHeight: "14px",
      }}
    >
      {t(label)}
    </label>
  );

  const FieldWrapper = ({ children }) => (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
      }}
    >
      {children}
    </div>
  );

  return (
    <div
      style={{
        border: "1px solid #F4C6BD",
        padding: "24px",
        backgroundColor: "#FFFFFF",
      }}
    >
      <h2 style={{ margin: 0, fontSize: "32px", fontWeight: "700", marginBottom: "20px" }}>
        {t("ICC_PRE_POPULATION_CONFIGURATION")}
      </h2>
      <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
        {rows.map((row) => (
          <div
            key={row.id}
            style={{
              border: "1px solid #EEEEEE",
              padding: "16px 20px",
              display: "flex",
              gap: "12px",
              alignItems: "flex-start",
              flexWrap: "wrap",
            }}
          >
            <FieldWrapper>
              <FieldLabel label={"ICC_SYSTEM_TYPE"} />
              <CustomDropdown
                t={t}
                option={systemTypeOptions}
                optionKey={"name"}
                selected={row.systemType}
                select={(option) => updateRow(row.id, "systemType", option)}
                placeholder={t("ICC_SELECT_TYPE")}
                style={{ minWidth: "200px" }}
              />
            </FieldWrapper>
            <FieldWrapper>
              <FieldLabel label={"ICC_TOTAL_SYSTEM_CAPACITY"} />
              <CustomDropdown
                t={t}
                option={capacityOptions}
                optionKey={"name"}
                selected={row.totalSystemCapacity}
                select={(option) => updateRow(row.id, "totalSystemCapacity", option)}
                placeholder={t("ICC_SELECT_CAPACITY")}
                style={{ minWidth: "200px" }}
              />
            </FieldWrapper>
            <FieldWrapper>
              <FieldLabel label={row.file ? "ICC_PRE_FILLING_TEMPLATE" : "ICC_UPLOAD_PRE_FILLING_TEMPLATE"} />
              <UploadControl
                t={t}
                row={row}
                onFileSelect={(file) => updateRow(row.id, "file", file)}
              />
            </FieldWrapper>
            <button
              type="button"
              onClick={() => deleteRow(row.id)}
              style={{
                border: "none",
                backgroundColor: "transparent",
                cursor: "pointer",
                height: "44px",
                width: "44px",
                marginTop: "20px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
              aria-label={t("CORE_COMMON_DELETE")}
            >
              <CustomCloseSvg height={"24"} width={"24"} fill={"transparent"} iconFill={"#C84C0E"} />
            </button>
          </div>
        ))}
      </div>
      <button
        type="button"
        className={"jk-digit-secondary-btn"}
        style={{
          display: "flex",
          gap: "8px",
          alignItems: "center",
          width: "fit-content",
          height: "fit-content",
          padding: "0px",
          border: "none",
          backgroundColor: "transparent",
          marginTop: "20px",
        }}
        onClick={addRow}
      >
        <span
          style={{
            width: "14px",
            height: "14px",
            borderRadius: "50%",
            border: "1px solid #C84C0E",
            color: "#C84C0E",
            fontSize: "12px",
            fontWeight: "bold",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            textAlign: "center",
            lineHeight: "14px",
          }}
        >
          +
        </span>
        <span
          style={{
            fontSize: "13px",
            fontWeight: "700",
            fontFamily: "Roboto",
            color: "#C84C0E",
          }}
        >
          {t("ICC_ADD_ANOTHER_SYSTEM")}
        </span>
      </button>
    </div>
  );
};

export default ICCPrepopulationConfiguration;
