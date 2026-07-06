import React, { useEffect, useState } from "react";
import { AddIcon, Button, CardLabel, DownloadIcon, DustbinIcon, Dropdown } from "@egovernments/digit-ui-react-components";
import { UploadFile } from "@egovernments/digit-ui-components";
import { ICCService } from "../../services/ICC";
import { FilestoreService } from "../../services/Filestore";

const getDefaultRows = () => [
  {
    id: "icc-row-3",
    systemType: null,
    totalSystemCapacity: null,
    file: null,
    template: null,
    templateFile: null,
    templateOptions: [],
    capacityOptions: [],
  },
];

const getOption = (value) => value ? ({ code: value, name: value }) : null;

const getUniqueOptions = (values = []) => {
  return [...new Set(values.filter(Boolean))].map(getOption);
};

const getColumnValue = (row = {}, possibleKeys = []) => {
  const rowKeys = Object.keys(row);
  const matchedKey = rowKeys.find((key) => possibleKeys.includes(key?.trim?.().toLowerCase()));

  return matchedKey ? row[matchedKey] : "";
};

const getTemplateFileName = (template) => {
  if (!template?.fileStoreId) {
    return "";
  }

  return `${template.systemType || "ICC"}_${template.totalSystemCapacity || "template"}.xlsx`.replace(/\s+/g, "_");
};

const getICCApiSystemType = (systemType) => {
  return systemType?.split(/\s+[–—]\s+/)?.[0] || systemType;
};

const normalizeValue = (value) => (value || "").toString().trim().toLowerCase();

const getTemplateForRow = (row, templates = []) => {
  const systemType = getICCApiSystemType(row.systemType?.name);
  const capacity = row.totalSystemCapacity?.name;

  return templates.find((template) => (
    normalizeValue(template.systemType) === normalizeValue(systemType) &&
    (!capacity || normalizeValue(template.totalSystemCapacity) === normalizeValue(capacity))
  )) || templates.find((template) => normalizeValue(template.systemType) === normalizeValue(systemType));
};

const ICCPrepopulationConfiguration = ({ data = {}, setValue, props }) => {

  const { t, name, uploadFacilityData, iccTemplates = [] } = props;
  const [rows, setRows] = useState(data[name] || getDefaultRows());
  const [systemTypeOptions, setSystemTypeOptions] = useState([]);
  const [facilitySystemCapacityMap, setFacilitySystemCapacityMap] = useState({});

  useEffect(() => {
    const formRows = rows.map(({ templateOptions, capacityOptions, ...row }) => row);
    setValue(name, formRows);
  }, [name, rows, setValue]);

  useEffect(() => {
    const parseFacilityData = async () => {
      const uploadedFacilityFile = uploadFacilityData || data?.uploadFacilityData;
      const file = uploadedFacilityFile?.data || uploadedFacilityFile;

      if (!file) {
        return;
      }

      try {
        const parsedSheets = await Digit.Utils.parsingUtils.parseXlsToJsonMultipleSheets({
          target: {
            files: [file],
          },
        });
        const facilityRows = Object.values(parsedSheets || {})?.[0] || [];
        const systemCapacityMap = {};

        facilityRows.forEach((row) => {
          const systemType = getColumnValue(row, ["system type (mandatory)", "system type"]);
          const capacity = getColumnValue(row, ["total system capacity (mandatory)", "total system capacity"]);

          if (!systemType) {
            return;
          }

          systemCapacityMap[systemType] = systemCapacityMap[systemType] || new Set();

          if (capacity) {
            systemCapacityMap[systemType].add(capacity);
          }
        });

        setSystemTypeOptions(getUniqueOptions(Object.keys(systemCapacityMap)));
        setFacilitySystemCapacityMap(Object.entries(systemCapacityMap).reduce((acc, [systemType, capacities]) => ({
          ...acc,
          [systemType]: getUniqueOptions([...capacities]),
        }), {}));
      } catch (error) {
        console.error("Error parsing facility data for ICC pre-population", error);
      }
    };

    parseFacilityData();
  }, [data?.uploadFacilityData, uploadFacilityData]);

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
        template: null,
        templateFile: null,
        templateOptions: [],
        capacityOptions: [],
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
      setRows((prevRows) => prevRows.map((row) => {
        if (row.id !== rowId) return row;

        return {
          ...row,
          file: uploadedFile,
        };
      }));
    }
  };

  const handleSystemTypeSelection = async (rowId, option) => {
    const capacityOptionsFromFacilityData = facilitySystemCapacityMap[option?.name] || [];

    setRows((prevRows) => prevRows.map((row) => {
      if (row.id !== rowId) return row;

      return {
        ...row,
        systemType: option,
        totalSystemCapacity: null,
        file: null,
        template: null,
        templateFile: null,
        templateOptions: [],
        capacityOptions: capacityOptionsFromFacilityData,
      };
    }));

    if (!option?.name) {
      return;
    }
  };

  const handleCapacitySelection = async (rowId, option) => {
    updateRow(rowId, "totalSystemCapacity", option);
  };

  const handleTemplateDownload = async (row) => {
    if (!row.systemType?.name) {
      return;
    }

    try {
      const searchedTemplates = await ICCService.searchICCTemplates(getICCApiSystemType(row.systemType.name));
      const templateOptions = searchedTemplates?.length ? searchedTemplates : iccTemplates;
      const selectedTemplate = getTemplateForRow(row, templateOptions);

      if (!selectedTemplate?.fileStoreId) {
        return;
      }

      const templateFile = {
        name: getTemplateFileName(selectedTemplate),
        fileStoreId: selectedTemplate.fileStoreId,
        isTemplate: true,
      };

      setRows((prevRows) => prevRows.map((prevRow) => {
        if (prevRow.id !== row.id) return prevRow;

        return {
          ...prevRow,
          template: selectedTemplate,
          templateFile: templateFile,
          templateOptions: templateOptions,
        };
      }));

      await FilestoreService.downloadFileFromFilestore(selectedTemplate.fileStoreId, templateFile.name);
    } catch (error) {
      console.error("Error downloading ICC template", error);
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

          .icc-prepopulation-upload {
            position: relative;
            height: 40px;
            width: 200px;
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
            justify-content: flex-start !important;
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
            justify-content: flex-start !important;
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

          .icc-prepopulation-upload.has-file .digit-upload-file button h2 {
            visibility: hidden;
          }

          .icc-prepopulation-file-name {
            position: absolute;
            top: 0px;
            left: 0px;
            right: 0px;
            height: 40px;
            padding: 0px 12px;
            display: flex;
            align-items: center;
            font-family: Roboto;
            font-size: 14px;
            font-weight: 500;
            line-height: 20px;
            color: #0B0C0C;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            pointer-events: none;
            box-sizing: border-box;
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
            display: none !important;
          }

          .icc-prepopulation-upload .digit-tag-container {
            display: none !important;
          }

          .icc-prepopulation-upload .tag {
            display: none !important;
          }

          .icc-prepopulation-upload .digit-tag {
            display: none !important;
          }

          .icc-prepopulation-upload .digit-file-upload-status {
            display: none !important;
          }

          .icc-prepopulation-delete-icon svg {
            width: 26px;
            height: 26px;
          }

          .icc-prepopulation-download-icon svg {
            width: 24px;
            height: 24px;
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
                select={(option) => handleSystemTypeSelection(row.id, option)}
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
                option={row.capacityOptions || []}
                optionKey={"name"}
                selected={row.totalSystemCapacity}
                select={(option) => handleCapacitySelection(row.id, option)}
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
              <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                <div className={`icc-prepopulation-upload ${row.file ? "has-file" : ""}`}>
                  <UploadFile
                    accept={".xlsx,.xls"}
                    customClass={"icc-prepopulation-upload-file"}
                    enableButton={true}
                    onUpload={(event) => handleFileUpload(row.id, event)}
                    onDelete={() => updateRow(row.id, "file", null)}
                    removeTargetedFile={() => updateRow(row.id, "file", null)}
                    uploadedFiles={[]}
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
                  {row.file && (
                    <span className={"icc-prepopulation-file-name"} title={row.file.name}>
                      {row.file.name}
                    </span>
                  )}
                </div>
                <Button
                  variation={"secondary"}
                  label={""}
                  icon={(
                    <span className={"icc-prepopulation-download-icon"}>
                      <DownloadIcon fill={"#C84C0E"} />
                    </span>
                  )}
                  onButtonClick={() => handleTemplateDownload(row)}
                  style={{
                    border: "none",
                    backgroundColor: "transparent",
                    cursor: row.systemType ? "pointer" : "not-allowed",
                    height: "40px",
                    width: "40px",
                    padding: "0px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    opacity: row.systemType ? 1 : 0.5,
                  }}
                  aria-label={t("CORE_COMMON_DOWNLOAD")}
                />
                <Button
                  variation={"secondary"}
                  label={""}
                  icon={(
                    <span className={"icc-prepopulation-delete-icon"}>
                      <DustbinIcon />
                    </span>
                  )}
                  onButtonClick={() => deleteRow(row.id)}
                  style={{
                    border: "none",
                    backgroundColor: "transparent",
                    cursor: "pointer",
                    height: "40px",
                    width: "40px",
                    padding: "0px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                  aria-label={t("CORE_COMMON_DELETE")}
                />
              </div>
            </FieldWrapper>
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
