import React, { useEffect, useState } from "react";
import { AddIcon, Button, CardLabel, DeleteIcon, Dropdown } from "@egovernments/digit-ui-react-components";
import { UploadFile } from "@egovernments/digit-ui-components";

const getDefaultRows = () => [
  {
    id: "icc-row-3",
    systemType: null,
    totalSystemCapacity: null,
    file: null,
  },
];

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

  const handleFileUpload = (rowId, event) => {
    const uploadedFile = event.target.files?.[0];
    if (uploadedFile) {
      updateRow(rowId, "file", uploadedFile);
    }
  };

  const FieldLabel = ({ label }) => (
    <CardLabel
      className={"card-label-smaller"}
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
    </CardLabel>
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
      <style>
        {`
          .icc-prepopulation-upload .upload-file {
            min-height: 40px !important;
            height: 40px !important;
            width: 200px !important;
            max-width: 200px !important;
            box-sizing: border-box;
            border: 1px solid #D1D5DB;
          }

          .icc-prepopulation-upload .digit-upload-file {
            min-height: 40px !important;
            height: 40px !important;
            width: 200px !important;
            max-width: 200px !important;
            box-sizing: border-box;
            border: 1px solid #D1D5DB;
          }

          .icc-prepopulation-upload .upload-file > div {
            height: 100%;
            margin: 0px;
            padding: 0px;
            align-items: center;
            flex-wrap: nowrap;
          }

          .icc-prepopulation-upload .digit-upload-file > div {
            height: 100%;
            margin: 0px;
            padding: 0px;
            align-items: center;
            flex-wrap: nowrap;
          }

          .icc-prepopulation-upload .upload-file button {
            height: 38px !important;
            min-height: 38px !important;
            max-height: 38px !important;
            width: 100% !important;
            margin: 0px !important;
            padding: 0px 12px !important;
            border: none !important;
            background: transparent !important;
          }

          .icc-prepopulation-upload .digit-upload-file button {
            height: 38px !important;
            min-height: 38px !important;
            max-height: 38px !important;
            width: 100% !important;
            margin: 0px !important;
            padding: 0px 12px !important;
            border: none !important;
            background: transparent !important;
          }

          .icc-prepopulation-upload .upload-file button h2 {
            font-family: Roboto;
            font-size: 14px;
            font-weight: 500;
            line-height: 20px;
            color: #0B0C0C;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .icc-prepopulation-upload .digit-upload-file button h2 {
            font-family: Roboto;
            font-size: 14px;
            font-weight: 500;
            line-height: 20px;
            color: #0B0C0C;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .icc-prepopulation-upload .upload-file input {
            height: 40px !important;
            width: 200px !important;
          }

          .icc-prepopulation-upload .digit-upload-file input {
            height: 40px !important;
            width: 200px !important;
          }

          .icc-prepopulation-upload .tag-container {
            max-width: 100%;
          }

          .icc-prepopulation-upload .digit-tag-container {
            max-width: 100%;
          }
        `}
      </style>
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
              <Dropdown
                t={t}
                option={systemTypeOptions}
                optionKey={"name"}
                selected={row.systemType}
                select={(option) => updateRow(row.id, "systemType", option)}
                optionsCardStyle={{
                  zIndex: 10000000,
                  maxHeight: "400px",
                }}
                style={{
                  minWidth: "200px",
                  display: "flex",
                  justifyContent: "space-between",
                }}
              />
            </FieldWrapper>
            <FieldWrapper>
              <FieldLabel label={"ICC_TOTAL_SYSTEM_CAPACITY"} />
              <Dropdown
                t={t}
                option={capacityOptions}
                optionKey={"name"}
                selected={row.totalSystemCapacity}
                select={(option) => updateRow(row.id, "totalSystemCapacity", option)}
                optionsCardStyle={{
                  zIndex: 10000000,
                  maxHeight: "400px",
                }}
                style={{
                  minWidth: "200px",
                  display: "flex",
                  justifyContent: "space-between",
                }}
              />
            </FieldWrapper>
            <FieldWrapper>
              <FieldLabel label={row.file ? "ICC_PRE_FILLING_TEMPLATE" : "ICC_UPLOAD_PRE_FILLING_TEMPLATE"} />
              <div className={"icc-prepopulation-upload"}>
                <UploadFile
                  accept={".xlsx,.xls"}
                  customClass={"icc-prepopulation-upload-file"}
                  enableButton={true}
                  onUpload={(event) => handleFileUpload(row.id, event)}
                  onDelete={() => updateRow(row.id, "file", null)}
                  removeTargetedFile={() => updateRow(row.id, "file", null)}
                  uploadedFiles={row.file ? [[row.file.name, row.file]] : []}
                  message={""}
                  textStyles={{
                    fontSize: "14px",
                    fontFamily: "Roboto",
                    fontWeight: "500",
                  }}
                  style={{
                    minHeight: "40px",
                    height: "40px",
                    width: "200px",
                    maxWidth: "200px",
                  }}
                  extraStyles={{
                    buttonStyles: {
                      height: "38px",
                      minHeight: "38px",
                      maxHeight: "38px",
                      width: "100%",
                      margin: "0px",
                      padding: "0px 12px",
                    },
                  }}
                />
              </div>
            </FieldWrapper>
            <Button
              variation={"secondary"}
              label={""}
              icon={<DeleteIcon fill={"#C84C0E"} />}
              onButtonClick={() => deleteRow(row.id)}
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
            />
          </div>
        ))}
      </div>
      <Button
        variation={"secondary"}
        label={t("ICC_ADD_ANOTHER_SYSTEM")}
        icon={<AddIcon fill={"#C84C0E"} />}
        onButtonClick={addRow}
        style={{
          width: "fit-content",
          height: "fit-content",
          padding: "0px",
          border: "none",
          backgroundColor: "transparent",
          marginTop: "20px",
        }}
        textStyles={{
          color: "#C84C0E",
          fontSize: "13px",
          fontWeight: "700",
          fontFamily: "Roboto",
        }}
      />
    </div>
  );
};

export default ICCPrepopulationConfiguration;
