import React from "react";
import Summary from "./Summary";
import CustomFileIcon from "../Custom/CustomFileIcon";

const IMAGE_EXTENSIONS = ["png", "jpg", "jpeg"];

const getExtension = (file) => {
  if (file.fileType && file.fileType !== "unknown") return file.fileType.toLowerCase();
  return file.name ? file.name.split(".").pop()?.toLowerCase() : "";
};

const fileTileStyle = {
  border: "1px solid #eee",
  borderRadius: "6px",
  padding: "16px",
  display: "flex",
  alignItems: "center",
  minWidth: "fit-content",
  width: "20%",
};

const WarrantyCardReviewCard = ({ t, files, isLoadingContent, onExpand }) => {
  const images = files.filter((file) => IMAGE_EXTENSIONS.includes(getExtension(file)));
  const documents = files.filter((file) => !IMAGE_EXTENSIONS.includes(getExtension(file)));

  return (
    <Summary
      t={t}
      sectionName="WarrantyCard"
      section="WARRANTY_CARD"
      isLoadingContent={isLoadingContent}
      onExpand={onExpand}
      renderContent={({ setImageToView }) => (
        <div style={{ padding: "20px", display: "flex", flexDirection: "column", gap: "20px" }}>
          {images.length > 0 && (
            <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
              {images.map((image, idx) => (
                <div key={idx} style={{ cursor: "pointer" }} onClick={() => setImageToView(image.fileUrl)}>
                  <img loading="lazy" decoding="async" src={image.fileUrl} alt={`Warranty Card ${idx + 1}`} style={{ width: "100px", marginTop: "8px" }} />
                </div>
              ))}
            </div>
          )}
          {documents.map((document, idx) => (
            <div key={idx} style={fileTileStyle}>
              <a
                style={{ textDecoration: "none", color: "unset" }}
                target="_blank"
                rel="noopener noreferrer"
                href={document.fileUrl}
                download={document.name || "warranty-card"}
              >
                <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                  <CustomFileIcon fileName={document.name || document.fileType} />
                  <div>
                    <div style={{ fontWeight: "bold", fontSize: "16px" }}>{document.name}</div>
                    {document.size && <div style={{ color: "#666", fontSize: "14px" }}>{document.size}</div>}
                  </div>
                </div>
              </a>
            </div>
          ))}
        </div>
      )}
    />
  );
};

export default WarrantyCardReviewCard;
