import React, {useState, useRef} from "react";
import { PdfIcon } from "@egovernments/digit-ui-svg-components";
import { ImageViewer, DownloadIcon, Toast } from "@egovernments/digit-ui-react-components";
import CustomFileIcon from "../File/CustomFileIcon";

import { useInstallationReportDownload } from "../../hooks/useInstallationReport";

const SystemParameterReport = ({ t, file, supportingDocuments, installationImages, installationCompletionCertificate, assetHandoverDocument }) => {

  const [imageToView, setImageToView] = useState(null);
  const [downloadError, setDownloadError] = useState(false);
  const downloading = useRef(false);
  const { fetchPdf, isFetching } = useInstallationReportDownload(file.fileUrl);

  // Save a local blob so the download uses the displayed name instead of the storage URL filename.
  const handleDownload = async () => {
    // Block repeated clicks immediately, before the query loading state updates.
    if (downloading.current) return;
    downloading.current = true;
    setDownloadError(false);
    try {
      const blob = await fetchPdf();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = file.name;
      document.body.appendChild(link);
      link.click();
      link.remove();
      // Allow the browser to start saving before releasing the temporary URL.
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (error) {
      setDownloadError(true);
    } finally {
      downloading.current = false;
    }
  };

  const installationCompletionCertificatePresent = installationCompletionCertificate.length > 0;
  const assetHandoverDocumentPresent = assetHandoverDocument.length > 0;
  const supportingDocumentsPresent = supportingDocuments.length > 0;
  const installationImagesPresent = installationImages.some(({ images }) => images.length > 0);

  const isNotLastSection = (index) => {
    if (index === 1) return installationCompletionCertificatePresent || assetHandoverDocumentPresent || supportingDocumentsPresent || installationImagesPresent;
    if (index === 2) return assetHandoverDocumentPresent || supportingDocumentsPresent || installationImagesPresent;
    if (index === 3) return supportingDocumentsPresent || installationImagesPresent;
    if (index === 4) return installationImagesPresent;
  }

  const AdditionalDocuments = ({ index, heading, additionalDocuments }) => (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: "20px",
        paddingBottom: isNotLastSection(index) ? "20px" : "0",
        marginBottom: isNotLastSection(index) ? "20px" : "0",
        borderBottom: isNotLastSection(index) ? "1px solid #D6D5D4" : "0",
      }}
    >
      <div
        style={{
          color: "#0B3954",
          fontSize: "20px",
        }}
      >
        {heading}
      </div>
      {additionalDocuments?.map((additionalDocument) => (
        <div
          style={{
            border: "1px solid #eee",
            borderRadius: "6px",
            padding: "16px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            minWidth: "fit-content",
            width: "20%",
            position: "relative",
          }}
        >
          <a
            style={{ textDecoration: "none", color: "unset" }}
            target="_blank"
            rel="noopener noreferrer"
            href={additionalDocument.fileUrl}
            download={additionalDocument.name || "supporting-doc"}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
              <CustomFileIcon file={{ name: additionalDocument.name || additionalDocument.fileType }} />
              <div>
                <div style={{ fontWeight: "bold", fontSize: "16px" }}>{additionalDocument.name}</div>
                {additionalDocument?.size && <div style={{ color: "#666", fontSize: "14px" }}>{additionalDocument.size}</div>}
              </div>
            </div>
          </a>
        </div>
      ))}
    </div>
  );

  return (
    <div style={{ padding: "20px" }}>
      <div
        style={{
          paddingBottom: isNotLastSection(1) ? "20px" : "0",
          marginBottom: isNotLastSection(1) ? "20px" : "0",
          borderBottom: isNotLastSection(1) ? "1px solid #D6D5D4" : "0",
        }}
      >
        <div
          style={{
            border: "1px solid #eee",
            borderRadius: "6px",
            padding: "16px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            minWidth: "fit-content",
            width: "20%",
            position: "relative",
          }}
        >
          <a
            style={{ textDecoration: "none", color: "unset" }}
            target="_blank"
            rel="noopener noreferrer"
            href={file.fileUrl}
            download={file.name}
          >
            <div style={{ display: "flex", alignItems: "center" }}>
              <PdfIcon style={{ marginRight: "12px" }} />
              <div>
                <div style={{ fontWeight: "bold", fontSize: "16px" }}>{file.name}</div>
                {file?.size && <div style={{ color: "#666", fontSize: "14px" }}>{file.size}</div>}
              </div>
            </div>
          </a>
          <button
            type="button"
            onClick={handleDownload}
            disabled={isFetching}
            aria-busy={isFetching}
            aria-label={t("CORE_COMMON_DOWNLOAD")}
            title={t("CORE_COMMON_DOWNLOAD")}
            style={{ marginLeft: "16px", opacity: isFetching ? 0.5 : 1, backgroundColor: "transparent", border: "none", color: "#d35400", padding: "8px", cursor: "pointer", fontWeight: "bold", fontSize: "16px", display: "flex", alignItems: "center", gap: "5px", height: "40px" }}
          >
            <DownloadIcon fill="#d35400" />
          </button>
        </div>
      </div>
      {downloadError && <Toast error label={t("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED")} onClose={() => setDownloadError(false)} />}
      {installationCompletionCertificatePresent && <AdditionalDocuments index={2} heading={t("INSTALLATION_COMPLETION_CERTIFICATE")} additionalDocuments={installationCompletionCertificate} />}
      {assetHandoverDocumentPresent && <AdditionalDocuments index={3} heading={t("ASSET_HANDOVER_DOCUMENT")} additionalDocuments={assetHandoverDocument} />}
      {supportingDocumentsPresent && <AdditionalDocuments index={4} heading={t("SUPPORTING_DOCUMENTS")} additionalDocuments={supportingDocuments} />}
      {installationImagesPresent && (
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: "20px",
          }}
        >
          <div
            style={{
              color: "#0B3954",
              fontSize: "20px",
            }}
          >
            {t("INSTALLATION_IMAGES")}
          </div>
          {installationImages?.map((installationImage) => (
            <div>
              <div style={{ fontWeight: "bold" }}>{installationImage.description}</div>
              <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                {installationImage.images.map((image, idx) => (
                  <div key={idx} style={{ cursor: "pointer" }} onClick={() => setImageToView(image.fileUrl)}>
                    <img src={image.fileUrl} alt={`Installation Image - ${idx}`} style={{ width: "100px", marginTop: "8px" }} />
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
      {imageToView && <ImageViewer imageSrc={imageToView} onClose={() => setImageToView(null)} />}
    </div>
  );
};

export default SystemParameterReport;
