import React, { useEffect, useState } from "react";
import Section from "./Section";
import RejectionReasonModal from "./RejectionReasonModal";
import SystemParameterReport from "./SystemParameterReport";
import SingleRejectionReasonModal from "./SingleRejectionReasonModal";
import { useDispatch, useSelector } from "react-redux";
import { setRejectionReasons } from "../../../../redux/actions";

const Summary = ({ sectionName, count, specifications, details, items, images, videos, pdf, isReport }) => {

  const [expanded, setExpanded] = useState(false);
  const [showRejectionModal, setShowRejectionModal] = useState(false);
  const rejectionReasons = useSelector((state) => state.qc.rejectionReasons);
  const [rejectionData, setRejectionData] = useState(rejectionReasons?.[sectionName] || []);
  const [activeReasonId, setActiveReasonId] = useState(null);
  const dispatch = useDispatch();
  const selectedFacility = useSelector((state) => state.qc.common.selectedFacility);

  const handleSave = (data) => {
    setRejectionData([...rejectionData, ...data?.filter((reason) => reason?.reason?.trim())]);
  };

  const handleUpdate = (reason) => {
    setRejectionData(rejectionData.map((r) => r.id === reason.id ? reason : r));
  };

  const handleDelete = (reason) => {
    setRejectionData(rejectionData.filter((r) => r.id !== reason.id));
  };

  useEffect(() => {
    dispatch(setRejectionReasons(sectionName, rejectionData))
  }, [rejectionData]);

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
        overflow: "hidden",
        transition: "all 0.3s ease-in-out",
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
          <h2 style={{ margin: 0, color: "#004d66" }}>{isReport ? `Installation Completion Report` : `${sectionName} Summary`}</h2>
          <button
            style={{
              width: "32px",
              height: "32px",
              borderRadius: "10%",
              border: "1px solid #ccc",
              background: "#f9f9f9",
              fontSize: "20px",
              fontWeight: "bold",
              cursor: "pointer",
            }}
            onClick={() => setExpanded((prev) => !prev)}
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
                color: "#d35400",
                padding: "8px 14px",
                borderRadius: "2px",
                fontWeight: "bold",
                cursor: "pointer",
              }}
              onClick={() => setShowRejectionModal(true)}
            >
              Add Rejection Reason
            </button>
          )}
        </div>
      </div>

      {expanded && (
        isReport ? (
          pdf && <SystemParameterReport pdf={pdf} />
        ) : (
          <div style={{ padding: "20px" }}>
            <Section title="Count">
              <div>
                <strong>{sectionName}</strong>: {count}
              </div>
            </Section>

            <Section title={`${sectionName} Specifications`}>
              <div>
                <strong>System</strong>: {specifications.system}
              </div>
              <div>
                <strong>Capacity</strong>: {specifications.capacity}
              </div>
            </Section>

            <Section title={`${sectionName} Details`}>
              <div>
                <strong>Count</strong>: {details.count}
              </div>
              <div>
                <strong>Warranty Start Date</strong>: {details.warrantyStartDate}
              </div>
              <div>
                <strong>Warranty Duration</strong>: {details.warrantyDuration}
              </div>
              <div>
                <strong>Brand</strong>: {details.brand}
              </div>
              <div>
                <strong>Model No.</strong>: {details.modelNumber}
              </div>
            </Section>

            <Section title="Capacity">
              <div>
                <strong>Voltage</strong>: {specifications.voltage}
              </div>
            </Section>

            {items?.map((item, index) => (
              <Section key={index} title={`${sectionName} ${index + 1}`}>
                <div>
                  <strong>Serial Number</strong>: {item.serialNumber}
                </div>
                <div>
                  <strong>Capacity</strong>: {item.capacity}
                </div>
                {item.documents && item.documents.length > 0 && (
                  <div>
                    <strong>Images</strong>:<br />
                    <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                      {item.documents.map((doc, idx) => (
                        <img src={doc} alt={`panel-${idx}`} style={{ width: "100px", marginTop: "8px" }} />
                      ))}
                    </div>
                  </div>
                )}
              </Section>
            ))}

            {/*<Section title={`${sectionName} Images`}>*/}
            {/*  <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>*/}
            {/*    {images.map((img, idx) => (*/}
            {/*      <img key={idx} src={img} alt={`image-${idx}`} style={{ width: "100px", height: "100px", objectFit: "cover" }} />*/}
            {/*    ))}*/}
            {/*  </div>*/}
            {/*</Section>*/}

            {/*<Section title={`${sectionName} Videos`}>*/}
            {/*  <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>*/}
            {/*    {videos.map((video, idx) => (*/}
            {/*      <div*/}
            {/*        key={idx}*/}
            {/*        style={{ border: "1px solid #ccc", padding: "10px", display: "flex", alignItems: "center", gap: "10px", width: "300px" }}*/}
            {/*      >*/}
            {/*        <video width="50" height="50" controls>*/}
            {/*          <source src={video.url} type="video/mp4" />*/}
            {/*        </video>*/}
            {/*        <div>*/}
            {/*          <div>*/}
            {/*            <strong>{video.name}</strong>*/}
            {/*          </div>*/}
            {/*          <div style={{ fontSize: "12px", color: "#666" }}>{video.size}</div>*/}
            {/*        </div>*/}
            {/*      </div>*/}
            {/*    ))}*/}
            {/*  </div>*/}
            {/*</Section>*/}
          </div>
        )
      )}

      {showRejectionModal && (
        <RejectionReasonModal
          onClose={() => setShowRejectionModal(false)}
          onSave={handleSave}
        />
      )}

      {rejectionData.filter((reason) => reason.reason.trim()).length > 0 && (
        <div style={{display: "flex", gap: "10px", alignItems: "center", paddingLeft: "20px", paddingRight: "20px"}}>
          {rejectionData.filter((reason) => reason.reason.trim()).map((reason, index) => (
            <div key={reason.id}>
              <div>
                <button
                  style={{
                    border: "1px solid #d35400",
                    backgroundColor: "white",
                    color: "#d35400",
                    padding: "6px 10px",
                    borderRadius: "2px",
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                  onClick={() => setActiveReasonId(reason.id)}
                >
                  Reason {index + 1}
                </button>
              </div>
              {activeReasonId && activeReasonId === reason.id && (
                <SingleRejectionReasonModal
                  onClose={() => setActiveReasonId(null)}
                  onUpdate={handleUpdate}
                  onDelete={handleDelete}
                  existingReason={reason}
                  name={`Reason ${index + 1}`}
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