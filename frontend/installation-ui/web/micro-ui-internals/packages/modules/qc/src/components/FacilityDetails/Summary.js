import React, { useState } from "react";
import Section from "./Section";
import AddRejectionReasonModal from "./AddRejectionReasonModal";
import SystemParameterReport from "./SystemParameterReport";
import EditRejectionReasonModal from "./EditRejectionReasonModal";
import AssetImageViewer from "./AssetImageViewer";
import { useDispatch, useSelector } from "react-redux";
import { setRejectionReasons } from "../../redux/actions";
import { ImageViewer, Loader } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "../CustomCloseSvg";

const Summary = ({
  t,
  sectionName,
  section,
  count,
  specifications,
  details,
  items,
  images,
  videos,
  report,
  isReport,
  customTitle,
  renderContent,
  isLoadingContent = false,
  onExpand,
  supportingDocuments = [],
  installationImages = [],
  installationCompletionCertificate = [],
  assetHandoverDocument = []
}) => {

  const [expanded, setExpanded] = useState(false);
  const [showRejectionModal, setShowRejectionModal] = useState(false);
  const rejectionData = useSelector((state) => state.qc.rejectionReasons);
  const [activeReasonId, setActiveReasonId] = useState(null);
  const dispatch = useDispatch();
  const selectedFacility = useSelector((state) => state.qc.common.selectedFacility);
  const rejectionReasons = rejectionData?.[section] || [];
  const [imageToView, setImageToView] = useState(null);
  const rejectionSectionLabel = customTitle || t(`QC_${section}_SUMMARY`);

  const toggleExpanded = () => {
    if (!expanded && onExpand) {
      onExpand();
    }
    setExpanded((prev) => !prev);
  };

  const handleSave = (data) => {
    dispatch(setRejectionReasons(section, [
      ...rejectionReasons,
      ...data
        .filter((reason) => reason?.reason?.trim())
        .map((reason) => ({ ...reason, sectionLabel: rejectionSectionLabel })),
    ]));
  };

  const handleUpdate = (reason) => {
    dispatch(setRejectionReasons(section, rejectionReasons.map((r) => r.id === reason.id ? { ...reason, sectionLabel: rejectionSectionLabel } : r)));
  };

  const handleDelete = (reason) => {
    dispatch(setRejectionReasons(section, rejectionReasons.filter((r) => r.id !== reason.id)));
  };

  const AssetInfoItem = (title, value) => (
    <div style={{
      display: "flex",
      marginBottom: "10px",
    }}>
      <div style={{
        fontWeight: "bold",
        width: "150px",
      }}>
        {title}
      </div>
      <div style={{
        width: "220px",
        wordBreak: "break-word"
      }}>
        {value || t("CORE_COMMON_NOT_APPLICABLE")}
      </div>
    </div>
  )

  const AssetImages = (images, item) => (
    <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
      {images.map((doc, idx) => (
        <div
          key={idx}
          style={{ cursor: "pointer" }}
          onClick={() => setImageToView({
            src: doc,
            serialNumber: item?.serialNumber,
            capacity: item?.capacity
          })}
        >
          <img loading="lazy" decoding="async" src={doc} alt={`${sectionName}-${idx}`} style={{ width: "100px", marginTop: "8px" }} />
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
        <div style={{ display: "flex", gap: "12px", alignItems: "center", flex: "1 1 auto", minWidth: 0, maxWidth: "calc(100% - 260px)" }}>
          <div
            style={{
              margin: 0,
              color: "#0B4B66",
              fontSize: "32px",
              fontWeight: "bold",
              maxWidth: "100%",
              overflowWrap: "break-word",
              lineHeight: "30px",
            }}
          >
            {customTitle || t(`QC_${section}_SUMMARY`)}
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
            onClick={toggleExpanded}
          >
            {expanded ? "−" : "+"}
          </button>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          {selectedFacility?.status && selectedFacility?.status.toUpperCase() === "SUBMITTED_BY_SUPERVISOR" && (
            <button
              style={{
                border: "1px solid #d35400",
                backgroundColor: "white",
                color: "#C84C0E",
                padding: "8px 14px",
                borderRadius: "2px",
                fontWeight: "bold",
                cursor: "pointer",
                fontSize: "18px",
              }}
              onClick={() => setShowRejectionModal(true)}
            >
              {t("CS_ACTION_ADD_REJECTION_REASON")}
            </button>
          )}
        </div>
      </div>

      {expanded &&
        (isLoadingContent ? (
          <div style={{ padding: "20px" }}>
            <Loader />
          </div>
        ) : renderContent ? (
          renderContent({ setImageToView })
        ) : isReport ? (
          report ? <SystemParameterReport
            t={t}
            file={report}
            supportingDocuments={supportingDocuments}
            installationImages={installationImages}
            installationCompletionCertificate={installationCompletionCertificate}
            assetHandoverDocument={assetHandoverDocument}
          /> : (
            <div style={{ padding: "20px" }}>{t("CORE_COMMON_NOT_APPLICABLE")}</div>
          )
        ) : (
          <div style={{ padding: "20px" }}>
            <Section title={t(`QC_INSTALLATION_ASSET_COUNT`)}>
              {AssetInfoItem(t(`QC_INSTALLATION_${section}`), count)}
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
            </Section>

            {items?.map((item, index) => (
              <Section key={index} title={`${t(`QC_INSTALLATION_${section}`)} ${index + 1}`}>
                {AssetInfoItem(t(`QC_INSTALLATION_ASSET_SERIAL_NUMBER`), item.serialNumber)}
                {AssetInfoItem(t(`QC_INSTALLATION_ASSET_CAPACITY`), item.capacity)}
                {item.documents && item.documents.length > 0 && (
                  <div style={{display: "flex", gap: "10px"}}>
                    {AssetInfoItem(
                      t(`QC_INSTALLATION_ASSET_IMAGE`),
                      AssetImages(item.documents, item)
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
                      <img loading="lazy" decoding="async" src={img} alt={`image-${idx}`} style={{ width: "100px", height: "100px", objectFit: "cover" }} />
                    </div>
                  ))}
                </div>
              </Section>
            )}

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
                          <video width="50" height="50" controls={true} preload="metadata">
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

      {imageToView && typeof imageToView !== "string" && (
        <AssetImageViewer t={t} image={imageToView} onClose={() => setImageToView(null)} />
      )}
      {typeof imageToView === "string" && <ImageViewer imageSrc={imageToView} onClose={() => setImageToView(null)} />}

      {showRejectionModal && (
        <AddRejectionReasonModal
          t={t}
          onClose={() => setShowRejectionModal(false)}
          onSave={handleSave}
          rejectionReasons={rejectionReasons}
        />
      )}

      {rejectionReasons.filter((reason) => reason.reason.trim()).length > 0 && (
        <div style={{ display: "flex", gap: "10px", alignItems: "center", paddingLeft: "20px", paddingRight: "20px" }}>
          {rejectionReasons
            .filter((reason) => reason.reason.trim())
            .map((reason, index) => (
              <div key={reason.id}>
                <div style={{ position: "relative", display: "inline-block" }}>
                  <button
                    style={{
                      border: "1px solid #d35400",
                      backgroundColor: "white",
                      color: "#d35400",
                      padding: "10px 20px",
                      borderRadius: "8px",
                      fontWeight: "bold",
                      cursor: "pointer",
                    }}
                    onClick={() => setActiveReasonId(reason.id)}
                  >
                    {reason.reason}
                  </button>
                    <button
                      style={{
                        cursor: "pointer",
                        position: "absolute",
                        top: "0",
                        right: "0",
                        height: "15px",
                        width: "15px",
                        color: "white",
                        backgroundColor: "#b71c1c",
                        display: "flex",
                        alignItems: "center",
                        padding: "0"
                      }}
                      onClick={(e) => {
                          e.stopPropagation();
                          handleDelete(reason);
                      }}
                    >
                      <CustomCloseSvg height={"15"} width={"15"} fill="#b71c1c" iconFill={"white"}/>
                    </button>
                </div>
                {activeReasonId && activeReasonId === reason.id && (
                  <EditRejectionReasonModal
                    t={t}
                    onClose={() => setActiveReasonId(null)}
                    onUpdate={handleUpdate}
                    onDelete={handleDelete}
                    existingReason={reason}
                    rejectionReasons={rejectionReasons}
                    name={`${t("QC_INSTALLATION_REJECTION_REASON")} ${index + 1}`}
                  />
                )}
              </div>
            ))}
        </div>
      )}
    </div>
  );
};

export default Summary;
