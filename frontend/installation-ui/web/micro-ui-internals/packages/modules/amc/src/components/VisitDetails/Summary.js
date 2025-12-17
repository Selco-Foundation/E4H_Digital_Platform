import React, { useState } from "react";
import Section from "./Section";
import SystemParameterReport from "./SystemParameterReport";
import { ImageViewer } from "@egovernments/digit-ui-react-components";

const Summary = ({ t, sectionName, section, data, images, report, isDocument, supportingDocuments = [] }) => {

  const [expanded, setExpanded] = useState(false);
  const [imageToView, setImageToView] = useState(null);

  const AssetInfoItem = (title, value) => (
    <div style={{
      display: "flex",
      marginBottom: "10px",
      gap: "15px",
    }}>
      <div style={{
        fontWeight: "bold",
        width: "50%"
      }}>
        {title}
      </div>
      <div>{value || t("CORE_COMMON_NOT_APPLICABLE")}</div>
    </div>
  )

  const renderSummary = (dataToRender, isParent) => {
    return (
      <div>
        {!!dataToRender?.properties?.length && (
          dataToRender.properties.map((data, index) => (
            <React.Fragment key={index}>
              {AssetInfoItem(data.label, t(data.value))}
            </React.Fragment>
          ))
        )}
        {!!dataToRender?.children?.length && (
          dataToRender.children.map((item, index) => (
            <Section title={t(item.sectionName)} key={index}>
              {renderSummary(item)}
            </Section>
          ))
        )}
      </div>
    )
  }

  return (
    <div
      style={{
        marginTop: "15px",
        width: "95%",
        padding: "20px",
        background: "white",
        borderRadius: "4px",
        boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
        border: "1px solid #eee",
        borderTop: "none",
        borderBottom: "none",
        transition: "all 0.3s ease-in-out",
        minWidth: "900px"
      }}
    >
      <div
        style={{
          padding: "16px 20px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          backgroundColor: "#fff",
          borderBottom: expanded ? "1px solid #eee" : "none",
        }}
      >
        <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
          <div
            style={{
              margin: 0,
              color: "#0B4B66",
              fontSize: "32px",
              fontWeight: "bold",
            }}
          >
            {t(section)}
          </div>
          <button
            style={{
              width: "25px",
              height: "25px",
              borderRadius: "5px",
              background: "#0B4B66",
              color: "white",
              fontSize: "20px",
              fontWeight: "bold",
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
            onClick={() => setExpanded((prev) => !prev)}
          >
            {expanded ? "−" : "+"}
          </button>
        </div>
      </div>
      {expanded &&
        (isDocument ? (
          report && <SystemParameterReport file={report} supportingDocuments={supportingDocuments} />
        ) : (
          <div style={{ padding: "20px" }}>

            {renderSummary(data)}

            {images?.length > 0 && (
              <Section title={t(`QC_INSTALLATION_${section}_IMAGES`)}>
                <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                  {images.map((img, idx) => (
                    <div key={idx} style={{ cursor: "pointer" }} onClick={() => setImageToView(img)}>
                      <img src={img} alt={`image-${idx}`} style={{ width: "100px", height: "100px", objectFit: "cover" }} />
                    </div>
                  ))}
                </div>
              </Section>
            )}

            {imageToView && <ImageViewer imageSrc={imageToView} onClose={() => setImageToView(null)} />}
          </div>
        ))}
    </div>
  );
};

export default Summary;