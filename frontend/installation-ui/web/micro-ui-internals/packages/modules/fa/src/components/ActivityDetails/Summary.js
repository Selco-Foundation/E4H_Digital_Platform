import React, { useState } from "react";
import Section from "./Section";
import SystemParameterReport from "./SystemParameterReport";
import { ImageViewer } from "@egovernments/digit-ui-react-components";

const Summary = ({ t, sectionName, section, count, specifications, details, items, images, videos, report, isReport, supportingDocuments = [] }) => {

  const [expanded, setExpanded] = useState(false);
  const [imageToView, setImageToView] = useState(null);

  const AssetInfoItem = (title, value) => (
    <div style={{
      width: "300px",
      display: "flex",
      marginBottom: "10px"
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

  const AssetImages = (images) => (
    <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
      {images.map((doc, idx) => (
        <div key={idx} style={{ cursor: "pointer" }} onClick={() => setImageToView(doc)}>
          <img src={doc} alt={`${sectionName}-${idx}`} style={{ width: "100px", marginTop: "8px" }} />
        </div>
      ))}
    </div>
  )

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
            {t(`QC_${section}_SUMMARY`)}
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
        (isReport ? (
          report && <SystemParameterReport file={report} supportingDocuments={supportingDocuments} />
        ) : (
          <div style={{ padding: "20px" }}>
            <Section title={t(`QC_INSTALLATION_ASSET_COUNT`)}>
              {AssetInfoItem(sectionName, count)}
            </Section>

            <Section title={t(`QC_INSTALLATION_${section}_SPECIFICATIONS`)}>
              {AssetInfoItem(t(`QC_INSTALLATION_ASSET_SYSTEM`), t(`QC_INSTALLATION_SYSTEM_${specifications.system}`))}
              {AssetInfoItem(t(`QC_INSTALLATION_ASSET_CAPACITY`), specifications.capacity)}
            </Section>

            <Section title={t(`QC_INSTALLATION_${section}_DETAILS`)}>
              {AssetInfoItem(t(`QC_INSTALLATION_ASSET_COUNT`), details.count)}
              {AssetInfoItem(t(`QC_INSTALLATION_ASSET_WARRANTY_START_DATE`), details.warrantyStartDate)}
              {AssetInfoItem(t(`QC_INSTALLATION_ASSET_WARRANTY_DURATION`), details.warrantyDuration)}
              {AssetInfoItem(t(`QC_INSTALLATION_ASSET_BRAND`), t(`QC_INSTALLATION_BRAND_${details.brand}`))}
              {AssetInfoItem(t(`QC_INSTALLATION_ASSET_MODEL_NUMBER`), details.modelNumber)}
            </Section>

            {section === "BATTERY" && (
              <Section title={t(`QC_INSTALLATION_CAPACITY`)}>
                {AssetInfoItem(t(`QC_INSTALLATION_ASSET_VOLTAGE`), specifications.voltage)}
              </Section>
            )}

            {items?.map((item, index) => (
              <Section key={index} title={`${t(`QC_INSTALLATION_${section}`)} ${index + 1}`}>
                {AssetInfoItem(t(`QC_INSTALLATION_ASSET_SERIAL_NUMBER`), item.serialNumber)}
                {AssetInfoItem(t(`QC_INSTALLATION_ASSET_CAPACITY`), item.capacity)}
                {item.documents && item.documents.length > 0 && (
                  <div style={{display: "flex", gap: "10px"}}>
                    {AssetInfoItem(
                      t(`QC_INSTALLATION_ASSET_IMAGE`),
                      AssetImages(item.documents)
                    )}
                  </div>
                )}
              </Section>
            ))}

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

            {videos?.length > 0 && (
              <Section title={t(`QC_INSTALLATION_${section}_VIDEOS`)}>
                <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                  {videos.map((video, idx) => (
                    <div
                      key={idx}
                      style={{
                        minWidth: "fit-content",
                        width: "20%",
                        border: "1px solid #ccc",
                        padding: "10px",
                        borderRadius: "6px",
                      }}
                    >
                      <a
                        style={{ textDecoration: "none", color: "unset" }}
                        target="_blank"
                        rel="noopener noreferrer"
                        href={video.fileUrl}
                        download={`Video ${idx + 1}.mp4`}
                      >
                        <div
                          key={idx}
                          style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "10px",
                          }}
                        >
                          <video width="50" height="50" controls={true}>
                            <source src={video.fileUrl} type="video/mp4" />
                          </video>
                          <div>
                            <div>
                              <strong>{`Video ${idx + 1}.mp4`}</strong>
                            </div>
                            <div style={{ fontSize: "12px", color: "#666" }}>{video.size}</div>
                          </div>
                        </div>
                      </a>
                    </div>
                  ))}
                </div>
              </Section>
            )}
          </div>
        ))}
    </div>
  );
};

export default Summary;