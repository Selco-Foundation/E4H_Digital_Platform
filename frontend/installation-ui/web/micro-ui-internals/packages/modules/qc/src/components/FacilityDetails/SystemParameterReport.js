import React from "react";
import { PdfIcon } from "@egovernments/digit-ui-svg-components";

const SystemParameterReport = ({ file }) => {
  return (
    <div>
      <div style={{ padding: "20px" }}>
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
    </div>
  );
};

export default SystemParameterReport;
