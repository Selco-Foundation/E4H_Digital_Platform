import React, { useEffect, useState } from "react";
import { AddIcon, Button, CardLabel, DownloadIcon, DustbinIcon, Dropdown } from "@egovernments/digit-ui-react-components";
import { UploadFile } from "@egovernments/digit-ui-components";
import { ICCService } from "../../services/ICC";
import { FilestoreService } from "../../services/Filestore";
import { FieldPlanService } from "../../services/FieldPlan";

const getEmptyRow = (id = "icc-row-3") => ({
  id,
  systemType: null,
  totalSystemCapacity: null,
  file: null,
  template: null,
  templateFile: null,
  templateOptions: [],
  capacityOptions: [],
});

const getDefaultRows = () => [getEmptyRow()];

const getOption = (value) => value ? ({ code: value, name: value }) : null;

const getUniqueOptions = (values = []) => {
  return [...new Set(values.filter(Boolean))].map(getOption);
};

const getMDMSSystemTypeCode = (systemType = {}) => systemType?.data?.code || systemType?.code || systemType?.uniqueIdentifier;

const getMDMSSystemTypeName = (systemType = {}) => systemType?.data?.name || systemType?.name;

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

const getTemplateFileNameForRow = (row, template) => {
  if (!template?.fileStoreId) {
    return "";
  }

  return `${row.systemType?.code || template.systemType || "ICC"}_${row.totalSystemCapacity?.name || template.totalSystemCapacity || "template"}.xlsx`
    .replace(/\s+/g, "_");
};

const getICCApiSystemType = (systemType) => {
  return systemType?.split(/\s+[–—]\s+/)?.[0] || systemType;
};

const normalizeValue = (value) => (value || "").toString().trim().toLowerCase();

const normalizeCapacity = (value) => {
  const matchedCapacity = value?.toString?.()?.match(/[\d.]+/);
  return matchedCapacity?.[0] || normalizeValue(value);
};

const getSystemTypeOption = (systemType, systemTypeMaster = []) => {
  const matchedSystemType = systemTypeMaster.find((mdmsSystemType) => (
    normalizeValue(getMDMSSystemTypeName(mdmsSystemType)) === normalizeValue(systemType) ||
    normalizeValue(getMDMSSystemTypeCode(mdmsSystemType)) === normalizeValue(systemType)
  ));

  return {
    code: getMDMSSystemTypeCode(matchedSystemType) || systemType,
    name: getMDMSSystemTypeName(matchedSystemType) || systemType,
  };
};

const getSavedTemplateSystemType = (template = {}) => (
  template.systemType ||
  template.systemTypeCode ||
  template.additionalDetails?.systemType ||
  template.additionalDetails?.systemTypeCode ||
  template.template?.systemType ||
  template.data?.systemType ||
  template.data?.code ||
  ""
);

const getSavedTemplateCapacity = (template = {}) => (
  template.totalSystemCapacity ||
  template.systemCapacity ||
  template.capacity ||
  template.additionalDetails?.totalSystemCapacity ||
  template.additionalDetails?.systemCapacity ||
  template.additionalDetails?.capacity ||
  template.template?.totalSystemCapacity ||
  template.data?.totalSystemCapacity ||
  ""
);

const getSavedTemplateFileStoreId = (template = {}) => (
  template.fileStoreId ||
  template.filestoreId ||
  template.fileStoreID ||
  template.additionalDetails?.fileStoreId ||
  template.template?.fileStoreId ||
  template.file?.fileStoreId ||
  ""
);

const getSavedTemplateFileName = (template = {}) => (
  template.fileName ||
  template.filename ||
  template.name ||
  template.templateFileName ||
  template.originalFileName ||
  template.additionalDetails?.fileName ||
  template.additionalDetails?.filename ||
  template.template?.fileName ||
  template.file?.name ||
  ""
);

const getTemplateForRow = (row, templates = []) => {
  const systemTypeName = getICCApiSystemType(row.systemType?.name);
  const systemTypeCode = row.systemType?.code;
  const capacity = row.totalSystemCapacity?.name;

  return templates.find((template) => (
    [systemTypeCode, systemTypeName].some((systemType) => normalizeValue(template.systemType) === normalizeValue(systemType)) &&
    normalizeValue(template.totalSystemCapacity) === normalizeValue(capacity)
  )) || templates.find((template) => (
    [systemTypeCode, systemTypeName].some((systemType) => normalizeValue(template.systemType) === normalizeValue(systemType))
  ));
};

const getSavedTemplateForRow = (row, templates = []) => {
  const systemTypeName = getICCApiSystemType(row.systemType?.name);
  const systemTypeCode = row.systemType?.code;
  const capacity = row.totalSystemCapacity?.name;

  return templates.find((template) => (
    [systemTypeCode, systemTypeName].some((systemType) => normalizeValue(getSavedTemplateSystemType(template)) === normalizeValue(systemType)) &&
    (!capacity || normalizeCapacity(getSavedTemplateCapacity(template)) === normalizeCapacity(capacity))
  )) || templates.find((template) => (
    [systemTypeCode, systemTypeName].some((systemType) => normalizeValue(getSavedTemplateSystemType(template)) === normalizeValue(systemType))
  ));
};

const applySavedTemplatesToRows = (rows = [], templates = []) => {
  if (!templates?.length) {
    return rows;
  }

  return rows.map((row) => {
    if (row.file || row.templateFile) {
      return row;
    }

    const savedTemplate = getSavedTemplateForRow(row, templates);

    if (!savedTemplate) {
      return row;
    }

    const fileName = getSavedTemplateFileName(savedTemplate);

    if (!fileName) {
      return row;
    }

    return {
      ...row,
      file: {
        name: fileName,
        isSavedTemplate: true,
      },
      template: savedTemplate,
      templateFile: {
        name: fileName,
        fileStoreId: getSavedTemplateFileStoreId(savedTemplate),
        isTemplate: true,
      },
    };
  });
};

const getRowsFromSavedTemplates = (templates = [], systemTypeMaster = []) => (
  templates.map((template, index) => {
    const systemTypeValue = getSavedTemplateSystemType(template);
    const capacityValue = getSavedTemplateCapacity(template);
    const systemType = getSystemTypeOption(systemTypeValue, systemTypeMaster);
    const capacity = getOption(capacityValue);
    const fileName = getSavedTemplateFileName(template);

    return {
      ...getEmptyRow(`icc-row-saved-${template.id || index}`),
      systemType,
      totalSystemCapacity: capacity,
      file: fileName ? {
        name: fileName,
        isSavedTemplate: true,
      } : null,
      template,
      templateFile: fileName ? {
        name: fileName,
        fileStoreId: getSavedTemplateFileStoreId(template),
        isTemplate: true,
      } : null,
      capacityOptions: capacity ? [capacity] : [],
    };
  })
);

const ICCPrepopulationConfiguration = ({ data = {}, setValue, props }) => {

  const { t, name, uploadFacilityData, iccTemplates = [], validationAttempt = 0, fieldPlanId } = props;
  const [rows, setRows] = useState(data[name] || getDefaultRows());
  const [systemTypeOptions, setSystemTypeOptions] = useState([]);
  const [facilitySystemCapacityMap, setFacilitySystemCapacityMap] = useState({});
  const [savedTemplates, setSavedTemplates] = useState([]);
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const { data: systemTypeMDMSResponse } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "facility",
    [
      {
        name: "SystemType",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );
  const systemTypeMaster = systemTypeMDMSResponse?.facility?.SystemType || [];

  useEffect(() => {
    const formRows = rows.map(({ templateOptions, capacityOptions, ...row }) => row);
    setValue(name, formRows);
  }, [name, rows, setValue]);

  useEffect(() => {
    const searchSavedTemplates = async () => {
      if (!fieldPlanId) {
        return;
      }

      try {
        const templates = await FieldPlanService.searchFieldPlanTemplates(fieldPlanId);
        setSavedTemplates(templates || []);
      } catch (error) {
        console.error("Error fetching ICC saved templates", error);
      }
    };

    searchSavedTemplates();
  }, [fieldPlanId]);

  useEffect(() => {
    const uploadedFacilityFile = uploadFacilityData || data?.uploadFacilityData;

    if (uploadedFacilityFile || !savedTemplates?.length) {
      return;
    }

    const savedRows = getRowsFromSavedTemplates(savedTemplates, systemTypeMaster);

    if (savedRows.length) {
      setRows(savedRows);
      setSystemTypeOptions(savedRows.map((row) => row.systemType).filter(Boolean));
      setFacilitySystemCapacityMap(savedRows.reduce((acc, row) => ({
        ...acc,
        [row.systemType?.name]: row.capacityOptions,
      }), {}));
    }
  }, [data?.uploadFacilityData, savedTemplates, systemTypeMDMSResponse, uploadFacilityData]);

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
          const systemTypeValue = getColumnValue(row, ["system type (mandatory)", "system type"]);
          const systemType = getSystemTypeOption(systemTypeValue, systemTypeMaster);
          const capacity = getColumnValue(row, ["total system capacity (mandatory)", "total system capacity"]);

          if (!systemTypeValue) {
            return;
          }

          systemCapacityMap[systemType.name] = systemCapacityMap[systemType.name] || {
            option: systemType,
            capacities: new Set(),
          };

          if (capacity) {
            systemCapacityMap[systemType.name].capacities.add(capacity);
          }
        });

        const parsedSystemTypeOptions = Object.values(systemCapacityMap).map(({ option }) => option);
        const parsedCapacityMap = Object.entries(systemCapacityMap).reduce((acc, [systemType, capacities]) => ({
          ...acc,
          [systemType]: getUniqueOptions([...capacities.capacities]),
        }), {});
        const parsedRows = Object.values(systemCapacityMap).map(({ option, capacities }, index) => {
          const capacityOptions = getUniqueOptions([...capacities]);

          return {
            ...getEmptyRow(`icc-row-${option.code || option.name || index}`),
            systemType: option,
            totalSystemCapacity: capacityOptions.length === 1 ? capacityOptions[0] : null,
            capacityOptions,
          };
        });

        setSystemTypeOptions(parsedSystemTypeOptions);
        setFacilitySystemCapacityMap(parsedCapacityMap);
        setRows((prevRows) => {
          const updatedRows = parsedRows.map((parsedRow) => {
            const existingRow = prevRows.find((prevRow) => (
              normalizeValue(prevRow.systemType?.code || prevRow.systemType?.name) === normalizeValue(parsedRow.systemType?.code || parsedRow.systemType?.name)
            ));

            return existingRow ? {
              ...parsedRow,
              file: existingRow.file,
              template: existingRow.template,
              templateFile: existingRow.templateFile,
              templateOptions: existingRow.templateOptions,
            } : parsedRow;
          });

          return applySavedTemplatesToRows(updatedRows.length ? updatedRows : getDefaultRows(), savedTemplates);
        });
      } catch (error) {
        console.error("Error parsing facility data for ICC pre-population", error);
      }
    };

    parseFacilityData();
  }, [data?.uploadFacilityData, savedTemplates, systemTypeMDMSResponse, uploadFacilityData]);

  useEffect(() => {
    if (!savedTemplates?.length) {
      return;
    }

    setRows((prevRows) => applySavedTemplatesToRows(prevRows, savedTemplates));
  }, [savedTemplates]);

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
      getEmptyRow(`icc-row-${Date.now()}`),
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
      const searchedTemplates = await ICCService.searchICCTemplates(row.systemType?.code || getICCApiSystemType(row.systemType.name));
      const templateOptions = searchedTemplates?.length ? searchedTemplates : iccTemplates;
      const selectedTemplate = getTemplateForRow(row, templateOptions);

      if (!selectedTemplate?.fileStoreId) {
        return;
      }

      const templateFile = {
        name: getTemplateFileNameForRow(row, selectedTemplate),
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
      className={"card-label-smaller icc-prepopulation-label"}
      style={{
        color: "#505A5F",
        fontSize: "12px",
        fontWeight: "700",
        fontFamily: "Roboto",
        display: "block",
        marginBottom: "6px",
        minHeight: "14px",
        width:"100%",
        whiteSpace: "nowrap",
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

  const DropdownWrapper = ({ children }) => (
    <div
      style={{
        height: "40px",
        width: "380px",
      }}
    >
      {children}
    </div>
  );

  const RequiredError = () => (
    <span
      style={{
        color: "#B91900",
        fontSize: "12px",
        fontFamily: "Roboto",
        marginTop: "4px",
      }}
    >
      {`*${t("CORE_COMMON_REQUIRED")}`}
    </span>
  );

  return (
    <div
      style={{
        padding: "24px",
        backgroundColor: "#FFFFFF",
      }}
    >
      <style>
        {`
          .icc-prepopulation-upload .upload-file {
            min-height: 40px !important;
            height: 40px !important;
            width: 500px !important;
            max-width: 500px !important;
            box-sizing: border-box;
            border: 1px solid #D1D5DB;
          }

          .icc-prepopulation-upload .digit-upload-file {
            min-height: 40px !important;
            height: 40px !important;
            width: 500px !important;
            max-width: 500px !important;
            box-sizing: border-box;
            border: 1px solid #D1D5DB;
          }

          .icc-prepopulation-upload {
            position: relative;
            height: 40px;
            width: 500px;
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
            width: 500px !important;
          }

          .icc-prepopulation-upload .digit-upload-file input {
            height: 40px !important;
            width: 500px !important;
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

          .icc-prepopulation-label {
            display: block !important;
            width: 100% !important;
            white-space: nowrap !important;
          }
        `}
      </style>
      <h2 style={{ margin: 0, fontSize: "32px", fontWeight: "700", marginBottom: "20px" }}>
        {t("ICC_PRE_POPULATION_CONFIGURATION")}
      </h2>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "20px",
          maxHeight: rows.length > 3 ? "390px" : "none",
          overflowY: rows.length > 3 ? "auto" : "visible",
          paddingRight: rows.length > 3 ? "4px" : "0px",
        }}
      >
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
              <DropdownWrapper>
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
                    minWidth: "380px",
                    display: "flex",
                    justifyContent: "space-between",
                  }}
                />
              </DropdownWrapper>
              {validationAttempt > 0 && !row.systemType && <RequiredError />}
            </FieldWrapper>
            <FieldWrapper>
              <FieldLabel label={"ICC_TOTAL_SYSTEM_CAPACITY"} />
              <DropdownWrapper>
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
                    minWidth: "380px",
                    display: "flex",
                    justifyContent: "space-between",
                  }}
                />
              </DropdownWrapper>
              {validationAttempt > 0 && !row.totalSystemCapacity && <RequiredError />}
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
                      width: "500px",
                      maxWidth: "500px",
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
              {validationAttempt > 0 && !row.file && <RequiredError />}
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
