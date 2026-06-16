import React, {useState} from "react";
import { PdfIcon } from "@egovernments/digit-ui-svg-components";
import { ImageViewer } from "@egovernments/digit-ui-react-components";
import CustomFileIcon from "../Custom/CustomFileIcon";

const SystemParameterReport = ({ t, file, supportingDocuments, installationImages, installationCompletionCertificate, assetHandoverDocument }) => {

  const [imageToView, setImageToView] = useState(null);

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
              <CustomFileIcon fileName={additionalDocument.name || additionalDocument.fileType} />
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
            download={"installation-completion-report.pdf"}
          >
            <div style={{ display: "flex", alignItems: "center" }}>
              <PdfIcon style={{ marginRight: "12px" }} />
              <div>
                <div style={{ fontWeight: "bold", fontSize: "16px" }}>{file.name}</div>
                {file?.size && <div style={{ color: "#666", fontSize: "14px" }}>{file.size}</div>}
              </div>
            </div>
          </a>
        </div>
      </div>
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
