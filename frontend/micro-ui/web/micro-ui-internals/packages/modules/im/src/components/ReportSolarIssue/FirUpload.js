import React from "react";
import { MultiUploadWrapper } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const FirUpload = ({ files, onChange, onUploading, tenantId }) => {
  const { t } = useTranslation();
  return <MultiUploadWrapper
    t={t}
    module="Incident"
    tenantId={tenantId}
    setuploadedstate={files}
    getFormState={onChange}
    onUploadStatusChange={onUploading}
    allowedFileTypesRegex={/(pdf|jpg|jpeg|png|image)$/i}
    allowedMaxSizeInMB={5}
    maxFilesAllowed={5}
    ulb={Digit.SessionStorage.get("Employee.tenantId")}
    acceptFiles=".pdf, .jpg, .jpeg, .png, image/*"
    multiple={false}
    specificFileConstraint={{ type: "fir", maxSize: 5, maxFiles: 5 }}
    analyticsPage="new_ticket_page"
    mediaIntent="fir"
  />;
};

export default FirUpload;
