import React, { useState } from "react";
import { Card, DownloadIcon, Loader } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import CustomUploadFile from "../../components/File/CustomUploadFile";
import { TranslationService } from "../../services/Translation";

const ALLOWED_FILE_TYPES = [".xlsx", ".xls", ".csv"];

const getBackendErrorMessage = async (error) => {
  const responseData = error?.response?.data;

  if (responseData instanceof Blob) {
    try {
      const errorText = await responseData.text();
      const parsedError = JSON.parse(errorText);
      return parsedError?.message || parsedError?.error || parsedError?.Errors?.[0]?.message || errorText;
    } catch (parseError) {
      return error?.message || "Failed to translate file";
    }
  }

  return responseData?.message || responseData?.error || responseData?.Errors?.[0]?.message || error?.message || "Failed to translate file";
};

const Translation = () => {
  const { t } = useTranslation();
  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleFileUpload = async (uploadedFile, setUploadedFile) => {
    setUploadedFile({
      name: uploadedFile.name,
      type: uploadedFile.type,
      data: uploadedFile,
    });
  };

  const handleDownload = async () => {
    if (!file?.data) {
      return;
    }

    const formData = new FormData();
    formData.append("file", file.data);
    formData.append("sourceLanguage", Digit.StoreData.getCurrentLanguage());

    try {
      setIsLoading(true);
      setErrorMessage("");
      await TranslationService.translateExcel(formData);
    } catch (error) {
      setErrorMessage(await getBackendErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card style={{ margin: "10px", padding: "24px" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: "16px",
          marginBottom: "20px",
        }}
      >
        <h2 style={{ margin: 0, fontSize: "32px", fontWeight: "700" }}>
          {t("Upload Translation Data")}
        </h2>
        <button
          style={{
            backgroundColor: "white",
            border: "1px solid #C84C0E",
            padding: "8px 20px",
            cursor: file && !isLoading ? "pointer" : "not-allowed",
            fontWeight: "bold",
            fontSize: "16px",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: "5px",
            height: "40px",
            opacity: file && !isLoading ? 1 : 0.6,
          }}
          type="button"
          onClick={handleDownload}
          disabled={!file || isLoading}
        >
          <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
            <DownloadIcon fill={"#C84C0E"} />
          </div>
          <span style={{ color: "#C84C0E", fontFamily: "Roboto", fontWeight: "600" }}>
            {t("Download")}
          </span>
        </button>
      </div>
      {isLoading && <Loader />}
      {errorMessage && (
        <p style={{ color: "#D4351C", fontSize: "14px", fontWeight: "500", margin: "0 0 16px 0" }}>
          {errorMessage}
        </p>
      )}
      <CustomUploadFile
        setError={() => {}}
        setValue={() => {}}
        clearErrors={() => {}}
        props={{
          t,
          name: "translationData",
          description: "Upload the translation file and download it after processing.",
          allowedFileTypes: ALLOWED_FILE_TYPES,
          file,
          setFile,
          handleFileUpload,
          invalidDataError,
          setInvalidDataError,
        }}
      />
    </Card>
  );
};

export default Translation;
