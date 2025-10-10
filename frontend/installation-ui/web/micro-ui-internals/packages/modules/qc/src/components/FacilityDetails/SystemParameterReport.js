import React from "react";
import { PdfIcon } from "@egovernments/digit-ui-svg-components";
import CustomFileIcon from "../Custom/CustomFileIcon";

const SystemParameterReport = ({ file, supportingDocuments }) => {

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
          gap: "20px"
        }}
      >
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
    </div>
  );
};

export default SystemParameterReport;
