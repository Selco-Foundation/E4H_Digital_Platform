import React, { useEffect, useState } from "react";
import { Button, CardLabel, DownloadIcon, DustbinIcon } from "@egovernments/digit-ui-react-components";
import { UploadFile } from "@egovernments/digit-ui-components";
import { ICCService } from "../../services/ICC";
import { FilestoreService } from "../../services/Filestore";
import { FieldPlanService } from "../../services/FieldPlan";
import { IngestionService } from "../../services/Ingestion";
import CommonUtils from "../../utilities/CommonUtils";

const getEmptyRow = (id = "icc-row-3") => ({
  id,
  systemType: null,
  totalSystemCapacity: null,
  file: null,
  template: null,
  templateFile: null,
  templateOptions: [],
  capacityOptions: [],
  isCustomCapacity: false,
});

const getDefaultRows = () => [getEmptyRow()];

const getOption = (value) => value ? ({ code: value, name: value }) : null;

const getMDMSSystemTypeCode = (systemType = {}) => systemType?.data?.code || systemType?.code || systemType?.uniqueIdentifier;

const getMDMSSystemTypeName = (systemType = {}) => systemType?.data?.name || systemType?.name;

const getColumnValue = (row = {}, possibleKeys = []) => {
  const rowKeys = Object.keys(row);
  const matchedKey = rowKeys.find((key) => possibleKeys.includes(key?.trim?.().toLowerCase()));

  return matchedKey ? row[matchedKey] : "";
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

const normalizeSystemTypeKey = (value) => normalizeValue(value).replace(/[\s_-]+/g, "");

const normalizeCapacity = (value) => {
  const matchedCapacity = value?.toString?.()?.match(/[\d.]+/);
  if (!matchedCapacity?.[0]) {
    return normalizeValue(value);
  }

  const numericCapacity = Number(matchedCapacity[0]);
  return Number.isNaN(numericCapacity) ? matchedCapacity[0] : numericCapacity.toString();
};

const getICCReportFormData = (row, file, fieldPlanId, tenantId) => {
  const formData = new FormData();
  const items = [{
    systemType: row.systemType?.code,
    totalSystemCapacity: normalizeCapacity(row.totalSystemCapacity?.code || row.totalSystemCapacity?.name),
    fieldPlanId,
    tenantId,
  }];

  formData.append("items", JSON.stringify(items));
  formData.append("icc_files", file);

  return formData;
};

const getRowKey = (row = {}) => {
  const systemTypeKey = normalizeSystemTypeKey(row.systemType?.name || row.systemType?.code);
  const capacityKey = normalizeCapacity(row.totalSystemCapacity?.name || row.totalSystemCapacity?.code);

  return systemTypeKey && capacityKey ? `${systemTypeKey}-${capacityKey}` : "";
};

const getUniqueRows = (rows = []) => Object.values(rows.reduce((acc, row) => {
  const rowKey = getRowKey(row);
  const existingRow = acc[rowKey];

  if (!rowKey) {
    acc[row.id || `empty-row-${Object.keys(acc).length}`] = row;
    return acc;
  }

  if (!existingRow || (!existingRow.file && row.file) || (!existingRow.templateFile && row.templateFile)) {
    acc[rowKey] = row;
  }

  return acc;
}, {}));

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
  template.totalCapacity ||
  template.systemCapacity ||
  template.capacity ||
  template.systemCapacityValue ||
  template.additionalDetails?.totalSystemCapacity ||
  template.additionalDetails?.totalCapacity ||
  template.additionalDetails?.systemCapacity ||
  template.additionalDetails?.capacity ||
  template.additionalDetails?.systemCapacityValue ||
  template.template?.totalSystemCapacity ||
  template.template?.totalCapacity ||
  template.template?.capacity ||
  template.data?.totalSystemCapacity ||
  template.data?.totalCapacity ||
  template.data?.capacity ||
  ""
);

const getSavedTemplateFileStoreId = (template = {}) => (
  template.fileStoreId ||
  template.filestoreId ||
  template.fileStoreID ||
  template.iccFileStoreId ||
  template.uploadedFileStoreId ||
  template.preFillingTemplateFileStoreId ||
  template.additionalDetails?.fileStoreId ||
  template.additionalDetails?.iccFileStoreId ||
  template.additionalDetails?.uploadedFileStoreId ||
  template.additionalDetails?.preFillingTemplateFileStoreId ||
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
  template.preFillingTemplateFileName ||
  template.uploadedFileName ||
  template.additionalDetails?.fileName ||
  template.additionalDetails?.filename ||
  template.additionalDetails?.preFillingTemplateFileName ||
  template.additionalDetails?.uploadedFileName ||
  template.template?.fileName ||
  template.file?.name ||
  ""
);

const getSavedTemplateDisplayName = (template = {}) => {
  const fileName = getSavedTemplateFileName(template);

  if (fileName) {
    return fileName;
  }

  if (!getSavedTemplateFileStoreId(template)) {
    return "";
  }

  return `${getSavedTemplateSystemType(template) || "ICC"}_${getSavedTemplateCapacity(template) || "template"}.xlsx`.replace(/\s+/g, "_");
};

const getICCUploadErrorMessage = (error) => (
  error?.response?.data?.detail?.message ||
  CommonUtils.getApiErrorMessage(error) ||
  "CORE_COMMON_ERROR"
);

const getTemplateForRow = (row, templates = []) => {
  const systemTypeName = getICCApiSystemType(row.systemType?.name);
  const systemTypeCode = row.systemType?.code;
  const capacity = row.totalSystemCapacity?.name;

  const matchedTemplate = templates.find((template) => (
    [systemTypeCode, systemTypeName].some((systemType) => normalizeValue(template.systemType) === normalizeValue(systemType)) &&
    normalizeCapacity(template.totalSystemCapacity) === normalizeCapacity(capacity)
  ));

  if (matchedTemplate) {
    return matchedTemplate;
  }

  return templates.find((template) => (
    [systemTypeCode, systemTypeName].some((systemType) => normalizeValue(template.systemType) === normalizeValue(systemType))
  ));
};

const getSavedTemplateForRow = (row, templates = []) => {
  const systemTypeName = getICCApiSystemType(row.systemType?.name);
  const systemTypeCode = row.systemType?.code;
  const capacity = row.totalSystemCapacity?.name;

  const matchedTemplate = templates.find((template) => (
    [systemTypeCode, systemTypeName].some((systemType) => normalizeValue(getSavedTemplateSystemType(template)) === normalizeValue(systemType)) &&
    (!capacity || normalizeCapacity(getSavedTemplateCapacity(template)) === normalizeCapacity(capacity))
  ));

  if (matchedTemplate || capacity) {
    return matchedTemplate;
  }

  return templates.find((template) => (
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

    const savedCapacity = getSavedTemplateCapacity(savedTemplate);
    const savedCapacityOption = savedCapacity ? getOption(savedCapacity) : null;
    const fileName = getSavedTemplateDisplayName(savedTemplate);
    const fileStoreId = getSavedTemplateFileStoreId(savedTemplate);

    return {
      ...row,
      totalSystemCapacity: row.totalSystemCapacity || savedCapacityOption,
      capacityOptions: row.capacityOptions?.length ? row.capacityOptions : savedCapacityOption ? [savedCapacityOption] : row.capacityOptions,
      file: fileName || fileStoreId ? {
        name: fileName,
        isSavedTemplate: true,
        fileStoreId,
      } : row.file,
      template: savedTemplate,
      templateFile: fileName || fileStoreId ? {
        name: fileName,
        fileStoreId,
        isTemplate: true,
      } : row.templateFile,
    };
  });
};

const getRowsFromSavedTemplates = (templates = [], systemTypeMaster = []) => (
  templates.map((template, index) => {
    const systemTypeValue = getSavedTemplateSystemType(template);
    const capacityValue = getSavedTemplateCapacity(template);
    const systemType = getSystemTypeOption(systemTypeValue, systemTypeMaster);
    const capacity = getOption(capacityValue);
    const fileName = getSavedTemplateDisplayName(template);
    const fileStoreId = getSavedTemplateFileStoreId(template);

    return {
      ...getEmptyRow(`icc-row-saved-${template.id || index}`),
      systemType,
      totalSystemCapacity: capacity,
      file: fileName || fileStoreId ? {
        name: fileName,
        isSavedTemplate: true,
        fileStoreId,
      } : null,
      template,
      templateFile: fileName || fileStoreId ? {
        name: fileName,
        fileStoreId,
        isTemplate: true,
      } : null,
      capacityOptions: capacity ? [capacity] : [],
    };
  })
);

const ICCPrepopulationConfiguration = ({ data = {}, setValue, props }) => {

  const { t, name, uploadFacilityData, iccTemplates = [], validationAttempt = 0, fieldPlanId, setToast, setBlockUI } = props;
  const [rows, setRows] = useState(data[name] || getDefaultRows());
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
    const formRows = getUniqueRows(rows).map(({ templateOptions, capacityOptions, ...row }) => row);
    setValue(name, formRows);
  }, [name, rows, setValue]);

  useEffect(() => {
    setRows((prevRows) => {
      const uniqueRows = getUniqueRows(prevRows);
      return uniqueRows.length === prevRows.length ? prevRows : uniqueRows;
    });
  }, [rows]);

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
    }
  }, [data?.uploadFacilityData, savedTemplates, systemTypeMDMSResponse, uploadFacilityData]);

  useEffect(() => {
    const parseFacilityData = async () => {
      const uploadedFacilityFile = uploadFacilityData || data?.uploadFacilityData;
      const file = uploadedFacilityFile?.originalData || uploadedFacilityFile?.data || uploadedFacilityFile;

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
        const systemCapacityRows = {};

        facilityRows.forEach((row) => {
          const includedInFieldPlan = getColumnValue(row, ["included in field plan", "include in field plan"]);
          const systemTypeValue = getColumnValue(row, ["system type (mandatory)", "system type"]);
          const systemType = getSystemTypeOption(systemTypeValue, systemTypeMaster);
          const solutionDesignType = getColumnValue(row, ["solution design type (mandatory)", "solution design type"]);
          const selectedCapacity = getColumnValue(row, ["total system capacity (mandatory)", "total system capacity"]);
          const customCapacity = getColumnValue(row, ["custom total system capacity"]);
          const isCustomCapacity = normalizeValue(solutionDesignType) === "custom solution design" &&
            normalizeValue(selectedCapacity) === "custom capacity";
          const capacity = isCustomCapacity ? customCapacity : selectedCapacity;

          if (normalizeValue(includedInFieldPlan) !== "yes" || !systemTypeValue) {
            return;
          }

          const rowKey = `${normalizeSystemTypeKey(systemType.name || systemType.code)}-${normalizeCapacity(capacity)}`;

          systemCapacityRows[rowKey] = {
            systemType,
            capacity,
            isCustomCapacity,
          };
        });

        const parsedRows = Object.values(systemCapacityRows).map(({ systemType, capacity, isCustomCapacity }, index) => ({
          ...getEmptyRow(`icc-row-${systemType.code || systemType.name || index}-${normalizeCapacity(capacity)}`),
          systemType,
          totalSystemCapacity: getOption(capacity),
          capacityOptions: capacity ? [getOption(capacity)] : [],
          isCustomCapacity,
        }));

        setRows((prevRows) => {
          const updatedRows = parsedRows.map((parsedRow) => {
            const existingRow = prevRows.find((prevRow) => getRowKey(prevRow) === getRowKey(parsedRow));

            return existingRow ? {
              ...parsedRow,
              file: existingRow.file,
              template: existingRow.template,
              templateFile: existingRow.templateFile,
              templateOptions: existingRow.templateOptions,
              totalSystemCapacity: parsedRow.isCustomCapacity ? existingRow.totalSystemCapacity || parsedRow.totalSystemCapacity : parsedRow.totalSystemCapacity,
            } : parsedRow;
          });

          return getUniqueRows(applySavedTemplatesToRows(updatedRows.length ? updatedRows : getDefaultRows(), savedTemplates));
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

  const deletePreFillingTemplate = (rowId) => {
    setRows((prevRows) => prevRows.map((row) => {
      if (row.id !== rowId) return row;

      return {
        ...row,
        file: null,
      };
    }));
  };

  const handleFileUpload = async (rowId, event) => {
    const uploadedFile = event.target.files?.[0];
    const selectedRow = rows.find((row) => row.id === rowId);

    if (!uploadedFile || !selectedRow?.systemType || !selectedRow?.totalSystemCapacity || !fieldPlanId) {
      return;
    }

    updateRow(rowId, "file", uploadedFile);
    setBlockUI?.(true);

    try {
      const formData = getICCReportFormData(selectedRow, uploadedFile, fieldPlanId, tenantId);
      await IngestionService.uploadICCReports(formData);

      setRows((prevRows) => prevRows.map((row) => {
        if (row.id !== rowId) return row;

        return {
          ...row,
          file: {
            name: uploadedFile.name,
            isSavedTemplate: true,
          },
        };
      }));
    } catch (error) {
      const uploadError = getICCUploadErrorMessage(error);

      setRows((prevRows) => prevRows.map((row) => {
        if (row.id !== rowId) return row;

        return {
          ...row,
          file: null,
        };
      }));
      setToast?.({
        key: "error",
        label: uploadError,
        translate: false,
      });
    } finally {
      setBlockUI?.(false);
    }
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

  const FieldWrapper = ({ children, className = "" }) => (
    <div
      className={`icc-prepopulation-field ${className}`}
      style={{
        display: "flex",
        flexDirection: "column",
      }}
    >
      {children}
    </div>
  );

  const ReadOnlyField = ({ value }) => (
    <div
      className={"icc-prepopulation-read-only"}
      style={{
        height: "40px",
        width: "100%",
      }}
    >
      {value || ""}
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

  const displayRows = getUniqueRows(rows);

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
            width: 100% !important;
            max-width: 100% !important;
            box-sizing: border-box;
            border: 1px solid #D1D5DB;
          }

          .icc-prepopulation-upload .digit-upload-file {
            min-height: 40px !important;
            height: 40px !important;
            width: 100% !important;
            max-width: 100% !important;
            box-sizing: border-box;
            border: 1px solid #D1D5DB;
          }

          .icc-prepopulation-upload {
            position: relative;
            height: 40px;
            width: 100%;
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
            width: 100% !important;
          }

          .icc-prepopulation-upload .digit-upload-file input {
            height: 40px !important;
            width: 100% !important;
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

          .icc-prepopulation-read-only {
            border: 1px solid #0B0C0C;
            box-sizing: border-box;
            display: flex;
            align-items: center;
            padding: 0px 12px;
            font-family: Roboto;
            font-size: 16px;
            font-weight: 400;
            line-height: 24px;
            color: #0B0C0C;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .icc-prepopulation-row {
            border: 1px solid #EEEEEE;
            padding: 16px 20px;
            display: flex;
            gap: 12px;
            align-items: flex-start;
            flex-wrap: wrap;
            box-sizing: border-box;
            width: 100%;
          }

          .icc-prepopulation-field {
            flex: 1 1 280px;
            max-width: 380px;
            min-width: 0;
          }

          .icc-prepopulation-file-field {
            flex: 1.2 1 320px;
            max-width: 580px;
          }

          .icc-prepopulation-template-actions {
            display: flex;
            align-items: center;
            gap: 12px;
            width: 100%;
          }

          .icc-prepopulation-template-input {
            flex: 1 1 auto;
            min-width: 0;
          }

          @media (max-width: 768px) {
            .icc-prepopulation-row {
              padding: 16px;
              flex-direction: column;
            }

            .icc-prepopulation-field,
            .icc-prepopulation-file-field {
              width: 100%;
              max-width: 100%;
              flex-basis: auto;
            }

            .icc-prepopulation-template-actions {
              align-items: center;
            }

            .icc-prepopulation-download-icon svg {
              width: 22px;
              height: 22px;
            }

            .icc-prepopulation-delete-icon svg {
              width: 24px;
              height: 24px;
            }
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
          maxHeight: displayRows.length > 3 ? "390px" : "none",
          overflowY: displayRows.length > 3 ? "auto" : "visible",
          paddingRight: displayRows.length > 3 ? "4px" : "0px",
        }}
      >
        {displayRows.map((row) => (
          <div
            key={row.id}
            className={"icc-prepopulation-row"}
          >
            <FieldWrapper>
              <FieldLabel label={"ICC_SYSTEM_TYPE"} />
              <ReadOnlyField value={row.systemType?.name} />
              {validationAttempt > 0 && !row.systemType && <RequiredError />}
            </FieldWrapper>
            <FieldWrapper>
              <FieldLabel label={"ICC_TOTAL_SYSTEM_CAPACITY"} />
              <ReadOnlyField value={row.totalSystemCapacity?.name} />
              {validationAttempt > 0 && !row.totalSystemCapacity && <RequiredError />}
            </FieldWrapper>
            <FieldWrapper className={"icc-prepopulation-file-field"}>
              <FieldLabel label={row.file ? "ICC_PRE_FILLING_TEMPLATE" : "ICC_UPLOAD_PRE_FILLING_TEMPLATE"} />
              <div className={"icc-prepopulation-template-actions"}>
                <div className={`icc-prepopulation-upload icc-prepopulation-template-input ${row.file ? "has-file" : ""}`}>
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
                      width: "100%",
                      maxWidth: "100%",
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
                  onButtonClick={() => deletePreFillingTemplate(row.id)}
                  style={{
                    border: "none",
                    backgroundColor: "transparent",
                    cursor: row.file ? "pointer" : "not-allowed",
                    height: "40px",
                    width: "40px",
                    padding: "0px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    opacity: row.file ? 1 : 0.5,
                  }}
                  aria-label={t("CORE_COMMON_DELETE")}
                />
              </div>
              {validationAttempt > 0 && !row.file && <RequiredError />}
            </FieldWrapper>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ICCPrepopulationConfiguration;
