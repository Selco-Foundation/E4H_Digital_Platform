import React, { useState, useRef, useEffect } from "react";
import { UploadIcon } from "@egovernments/digit-ui-react-components";
import UploadedFilePreview from "./UploadedFilePreview";
import UploadErrorCard from "./UploadErrorCard";

const CustomUploadFile = ({ setError, setValue, clearErrors, props }) => {

  const { t, heading, description, name, allowedFileTypes = [], handleFileUpload, invalidDataError, errorViewLabel } = props;
  const [file, setFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (file && !file?.errorCodes?.length) {
      setValue(name, file);
    } else {
      setValue(name, undefined);
    }
  }, [file]);

  const validateAndSaveFile = async (uploadedFile) => {
    if (
      allowedFileTypes.length === 0 ||
      allowedFileTypes.includes(uploadedFile.type) ||
      allowedFileTypes.some((ext) =>
        uploadedFile.name.toLowerCase().endsWith(ext.toLowerCase())
      )
    ) {
      const savedFile = await handleFileUpload(uploadedFile, setFile);
      setFile(savedFile);
      clearErrors(name);
    } else {
      setError(name, {
        type: "manual",
        message: `Allowed file types are ${allowedFileTypes.join(", ")}`
      });
      setFile(null);
    }
  }

  const handleFileChange = async (e) => {
    const uploadedFile = e.target.files[0];
    if (uploadedFile) {
      await validateAndSaveFile(uploadedFile);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = async (e) => {
    e.preventDefault();
    setIsDragging(false);
    const uploadedFile = e.dataTransfer.files[0];

    if (uploadedFile) {
      await validateAndSaveFile(uploadedFile);
    }
  };

  const openFileDialog = () => {
    fileInputRef.current.click();
  };

  return (
    <div style={{marginBottom: "25px"}}>
      {heading && (
        <h2 style={{ margin: 0, fontSize: "32px", fontWeight: "700", marginBottom: "20px" }}>
          {t(heading)}
        </h2>
      )}
      {description && (
        <p
          style={{
            margin: 0,
            fontSize: "14px",
            lineHeight: "1.4",
            marginBottom: "30px"
          }}
        >
          {t(description)}
        </p>
      )}
      <div
        style={{
          border: "1px dashed #ccc",
          borderRadius: "6px",
          padding: "24px",
          textAlign: "center",
          backgroundColor: isDragging ? "#f0f0f0" : "#fafafa",
          fontFamily: "Arial, sans-serif",
          cursor: "pointer",
          marginBottom: file ? "5px" : "25px",
        }}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={openFileDialog}
      >
        <input
          type="file"
          ref={fileInputRef}
          style={{ display: "none" }}
          onChange={handleFileChange}
          accept={allowedFileTypes.join(",")}
        />

        <div style={{ fontSize: "40px", marginBottom: "12px" }}>
          <UploadIcon fill={"#B1B4B6"} />
        </div>

        <p style={{ margin: 0, fontSize: "14px", color: "#666" }}>
          {t("CORE_COMMON_DRAG_AND_DROP_OR")} {" "}
          <span
            onClick={openFileDialog}
            style={{
              color: "#c44d2d",
              fontWeight: "500",
              textDecoration: "underline",
            }}
          >
            {t("CORE_COMMON_BROWSE_IN_MY_FILES")}
          </span>
        </p>
      </div>
      {file && <UploadedFilePreview t={t} file={file} onRemove={() => setFile(null)} onReupload={openFileDialog} />}
      {invalidDataError && <UploadErrorCard t={t} cardLabel={invalidDataError.label} viewActionLabel={errorViewLabel || "CORE_COMMON_VIEW_ERRORS"} />}
    </div>
  );
};

export default CustomUploadFile;
