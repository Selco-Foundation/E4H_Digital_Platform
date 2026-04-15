import React, {useState} from "react";
import { PdfIcon } from "@egovernments/digit-ui-svg-components";
import { ImageViewer } from "@egovernments/digit-ui-react-components";
import CustomFileIcon from "../Custom/CustomFileIcon";

const SystemParameterReport = ({ t, file, supportingDocuments, installationImages }) => {

  const [imageToView, setImageToView] = useState(null);
  console.debug("installationImages", installationImages);

  return (
    <div style={{ padding: "20px" }}>
      <div
        style={{
          paddingBottom: supportingDocuments.length ? "20px" : "0",
          marginBottom: supportingDocuments.length ? "20px" : "0",
          borderBottom: supportingDocuments.length ? "1px solid #D6D5D4" : "0",
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
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "20px",
          paddingBottom: installationImages.length ? "20px" : "0",
          marginBottom: installationImages.length ? "20px" : "0",
          borderBottom: installationImages.length ? "1px solid #D6D5D4" : "0",
        }}
      >
        <div style={{
          color: "#0B3954",
          fontSize: "20px",
        }}>
          {t("SUPPORTING_DOCUMENTS")}
        </div>
        {supportingDocuments?.map((supportingDocument) => (
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
              position: "relative"
            }}
          >
            <a
              style={{ textDecoration: "none", color: "unset" }}
              target="_blank"
              rel="noopener noreferrer"
              href={supportingDocument.fileUrl}
              download={supportingDocument.name || "supporting-doc"}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                <CustomFileIcon fileName={supportingDocument.name || supportingDocument.fileType} />
                <div>
                  <div style={{ fontWeight: "bold", fontSize: "16px" }}>{supportingDocument.name}</div>
                  {supportingDocument?.size && <div style={{ color: "#666", fontSize: "14px" }}>{supportingDocument.size}</div>}
                </div>
              </div>
            </a>
          </div>
        ))}
      </div>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "20px"
        }}
      >
        <div style={{
          color: "#0B3954",
          fontSize: "20px",
        }}>
          {t("INSTALLATION_IMAGES")}
        </div>
        {installationImages?.map((installationImage) => (
          <div>
            <div style={{fontWeight: "bold"}}>
              {installationImage.description}
            </div>
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
      {imageToView && <ImageViewer imageSrc={imageToView} onClose={() => setImageToView(null)} />}
    </div>
  );
};

export default SystemParameterReport;
