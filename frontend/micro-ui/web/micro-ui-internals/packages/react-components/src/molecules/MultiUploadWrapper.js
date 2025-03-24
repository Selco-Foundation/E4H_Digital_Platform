import React, { useEffect, useReducer, useState } from "react";
import UploadFile from "../atoms/UploadFile";

const fileValidationStatus = (file, regex, maxSize, t, specificFileConstraint) => {
  if (file?.type.includes(specificFileConstraint?.type)) {
    maxSize = specificFileConstraint.maxSize;
  }

  const fileExtention = file.name.split(".").pop().toLowerCase();
  const status = { valid: true, name: file?.name?.substring(0, 15), error: "" };
  if (!file) return;

  const invalidType = !regex.test(file.type) || !regex.test(fileExtention);
  const fileTooLarge = file.size / 1024 / 1024 > maxSize;

  if (invalidType && fileTooLarge) {
    status.valid = false;
    status.error = t(`NOT_SUPPORTED_FILE_TYPE_AND_FILE_SIZE_EXCEEDED_5MB`);
  } else if (invalidType) {
    status.valid = false;
    status.error = t(`NOT_SUPPORTED_FILE_TYPE`);
  } else if (fileTooLarge) {
    status.valid = false;
    status.error = t(`FILE_SIZE_EXCEEDED`).replace("{}", `${maxSize}`);
  }

  return status;
};
const checkIfAllValidFiles = (files, otherFilesLength, videoFilesLength, regex, maxSize, t, maxFilesAllowed, state, specificFileConstraint) => {
  if (!files.length || !regex || !maxSize) return [{}, false];

  // added another condition files.length > 0 , for when  user uploads files more than maxFilesAllowed in one go the
  const uploadedVideos = state.filter((f) => f[1].file.type.startsWith("video/")).length;
  const uploadedOthers = state.filter((f) => !f[1].file.type.startsWith("video/")).length;

  // Validate count separately for videos & others
  const fileLimitErrors = [];

  if (otherFilesLength && maxFilesAllowed && uploadedOthers + otherFilesLength > maxFilesAllowed) {
    fileLimitErrors.push({
      valid: false,
      name: "",
      error: t(`FILE_LIMIT_EXCEEDED`).replace("{}", `${maxFilesAllowed}`),
    });
  }
  if (videoFilesLength && specificFileConstraint?.maxFiles && uploadedVideos + videoFilesLength > specificFileConstraint.maxFiles) {
    fileLimitErrors.push({
      valid: false,
      name: "",
      error: t(`VIDEO_FILE_LIMIT_EXCEEDED`).replace("{}", `${specificFileConstraint.maxFiles}`),
    });
  }

  if (fileLimitErrors.length) {
    return [fileLimitErrors, true];
  }

  // Adding a check for fileSize > maxSize
  // const maxSizeInBytes = maxSize * 1000000
  // if(files?.some(file => file.size > maxSizeInBytes)){
  //     return [[{ valid: false, name: "", error: t(`FILE_SIZE_EXCEEDED_5MB`) }], true]
  // }

  const messages = [];
  let isInValidGroup = false;
  for (let file of files) {
    const fileStatus = fileValidationStatus(file, regex, maxSize, t, specificFileConstraint);
    if (!fileStatus.valid) {
      isInValidGroup = true;
    }
    messages.push(fileStatus);
  }

  return [messages, isInValidGroup];
};

// can use react hook form to set validations @neeraj-egov
const MultiUploadWrapper = ({
  t,
  module = "PGR",
  tenantId = Digit.ULBService.getStateId(),
  onUploadStatusChange,
  getFormState,
  requestSpecifcFileRemoval,
  extraStyleName = "",
  setuploadedstate = [],
  showHintBelow,
  hintText,
  allowedFileTypesRegex = /(.*?)(jpg|jpeg|webp|aif|png|image|pdf|msword|xlsx|openxmlformats-officedocument)$/i,
  allowedMaxSizeInMB = 10,
  acceptFiles = "image/*, .jpg, .jpeg, .webp, .aif, .png, .image, .pdf, .msword, .openxmlformats-officedocument, .dxf",
  maxFilesAllowed,
  customClass = "",
  customErrorMsg,
  containerStyles,
  disabled,
  ulb,
  specificFileConstraint,
}) => {
  const FILES_UPLOADED = "FILES_UPLOADED";
  const TARGET_FILE_REMOVAL = "TARGET_FILE_REMOVAL";
  const [isUploading, setIsUploading] = useState(false);

  const [fileErrors, setFileErrors] = useState([]);
  const [enableButton, setEnableButton] = useState(true);

  const uploadMultipleFiles = (state, payload) => {
    const { files, fileStoreIds } = payload;
    const filesData = Array.from(files);
    const newUploads = filesData
      .map((file, index) => {
        const fileStoreId = fileStoreIds[index];
        return fileStoreId ? [file.name, { file, fileStoreId }] : null;
      })
      .filter(Boolean);
    return [...state, ...newUploads];
  };

  const removeFile = (state, payload) => {
    const __indexOfItemToDelete = state.findIndex((e) => e[1].fileStoreId.fileStoreId === payload.fileStoreId.fileStoreId);
    const mutatedState = state.filter((e, index) => index !== __indexOfItemToDelete);
    return [...mutatedState];
  };

  const uploadReducer = (state, action) => {
    console.log("statestate123", state);
    switch (action.type) {
      case FILES_UPLOADED:
        return uploadMultipleFiles(state, action.payload);
      case TARGET_FILE_REMOVAL:
        return removeFile(state, action.payload);
      default:
        break;
    }
  };

  const [state, dispatch] = useReducer(uploadReducer, [...setuploadedstate]);

  const onUploadMultipleFiles = async (e) => {
    console.log("onUploadMultipleFiles");
    e.preventDefault();
    setEnableButton(false);
    setFileErrors([]);
    const files = Array.from(e.target.files);

    if (!files.length) return;

    // Separate files before uploading
    const videoFiles = [];
    const otherFiles = [];
    const fileIndexMap = {}; // Stores index mapping

    files.forEach((file, index) => {
      if (file.type.startsWith("video/")) {
        videoFiles.push(file);
        fileIndexMap[file.name] = index;
      } else {
        otherFiles.push(file);
        fileIndexMap[file.name] = index;
      }
    });

    const [validationMsg, error] = checkIfAllValidFiles(
      files,
      otherFiles.length,
      videoFiles.length,
      allowedFileTypesRegex,
      allowedMaxSizeInMB,
      t,
      maxFilesAllowed,
      state,
      specificFileConstraint
    );

    if (error) {
      setFileErrors(validationMsg);
      setEnableButton(true);
      return;
    }

    setIsUploading(true);
    try {
      let tenant = ulb || Digit.SessionStorage.get("Employee.tenantId");

      const uploadPromises = [];

      if (otherFiles.length > 0) {
        uploadPromises.push(
          Digit.UploadServices.MultipleFilesStorage(module, otherFiles, tenant)
            .then((res) => ({
              fileType: "other",
              response: res,
            }))
            .catch((err) => {
              throw { fileType: "other", error: err };
            })
        );
      }

      if (videoFiles.length > 0) {
        uploadPromises.push(
          Digit.UploadServices.MultipleFilesStorage(module, videoFiles, tenant, true)
            .then((res) => ({
              fileType: "video",
              response: res,
            }))
            .catch((err) => {
              throw { fileType: "video", error: err };
            })
        );
      }

      const results = await Promise.allSettled(uploadPromises);

      // Collect fileStore IDs and maintain order
      const fileStoreIds = new Array(files.length).fill(null);

      const errorMessages = [];
      let videoUploadFailed = false;
      let otherUploadFailed = false;

      results.forEach((result) => {
        if (result.status === "fulfilled") {
          const { fileType, response } = result.value;
          const uploadedFiles = fileType === "other" ? otherFiles : videoFiles;

          response?.data?.files.forEach((fileId, i) => {
            const fileName = uploadedFiles[i]?.name;
            if (fileName in fileIndexMap) {
              fileStoreIds[fileIndexMap[fileName]] = fileId;
            }
          });
        } else {
          console.error("File upload failed:", result.reason);

          if (result?.reason?.fileType === "video") {
            videoUploadFailed = true;
          } else {
            otherUploadFailed = true;
          }
        }
      });

      // Generate specific error messages
      if (videoUploadFailed && otherUploadFailed) {
        errorMessages.push({ valid: false, name: "", error: t("BOTH_VIDEO_AND_IMAGE_UPLOAD_FAILED") });
      } else if (videoUploadFailed) {
        errorMessages.push({ valid: false, name: "", error: t("VIDEO_UPLOAD_FAILED") });
      } else if (otherUploadFailed) {
        errorMessages.push({ valid: false, name: "", error: t("IMAGE_UPLOAD_FAILED") });
      }

      // Set errors if any failed uploads exist
      if (errorMessages.length > 0) {
        setFileErrors(errorMessages);
      }
      dispatch({ type: FILES_UPLOADED, payload: { files: e.target.files, fileStoreIds } });
    } catch (err) {
      console.error("File upload error:", err);
    } finally {
      setIsUploading(false);
      setEnableButton(true);
    }
  };

  const groupedErrors = fileErrors.reduce((acc, { valid, name, error }) => {
    if (!valid) {
      acc[error] = acc[error] ? [...acc[error], name] : [name];
    }
    return acc;
  }, {});

  useEffect(() => getFormState(state), [state]);

  useEffect(() => {
    if (onUploadStatusChange) onUploadStatusChange(isUploading);
  }, [isUploading]);

  useEffect(() => {
    requestSpecifcFileRemoval ? dispatch({ type: TARGET_FILE_REMOVAL, payload: requestSpecifcFileRemoval }) : null;
  }, [requestSpecifcFileRemoval]);

  return (
    <div style={containerStyles}>
      <UploadFile
        isUploading={isUploading}
        onUpload={(e) => onUploadMultipleFiles(e)}
        removeTargetedFile={(fileDetailsData) => {
          setFileErrors([]);
          dispatch({ type: TARGET_FILE_REMOVAL, payload: fileDetailsData });
        }}
        uploadedFiles={state}
        multiple={true}
        showHintBelow={showHintBelow}
        hintText={hintText}
        extraStyleName={extraStyleName}
        onDelete={() => {
          setFileErrors([]);
        }}
        accept={acceptFiles}
        message={t(`ACTION_NO_FILE_SELECTED`)}
        customClass={customClass}
        disabled={disabled}
        enableButton={enableButton}
        ulb={ulb}
      />
      <span style={{ display: "flex", flexDirection: "column" }}>
        {Object.entries(groupedErrors).map(([error, names]) => (
          <span key={error} style={{ display: "flex", flexDirection: "column" }}>
            <div className="validation-error">{customErrorMsg ? t(customErrorMsg) : t(error)}</div>
            {names.some((name) => name) && (
              <div className="validation-error">{`${t("ES_COMMON_DOC_FILENAME")} : ${names.filter((name) => name).join(", ")}`}</div>
            )}
          </span>
        ))}
      </span>
    </div>
  );
};

export default MultiUploadWrapper;
