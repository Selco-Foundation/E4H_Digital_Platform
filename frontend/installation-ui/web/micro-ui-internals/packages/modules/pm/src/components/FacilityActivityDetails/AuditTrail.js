import React from "react";
import { CheckPoint, ConnectingCheckPoints } from "@egovernments/digit-ui-react-components";

const AuditTrail = ({t, auditTrail}) => {


  const getTimelineCaptions = (checkpoint) => {
    return (
      <div style={{ marginTop: "12px", width: "100%", maxWidth: "1200px" }}>
        <div style={{fontSize: "14px", color: "#666"}}>{checkpoint.date}</div>
        {checkpoint.reasons?.length > 0 && (
          <div style={{ marginTop: 12 }}>
            {checkpoint.reasons.map((section, i) => (
              <div key={i} style={{
                backgroundColor: "#f9f9f9",
                border: "1px solid #eee",
                borderRadius: 4,
                padding: 10,
                marginTop: 10,
                width: "100%",
                maxWidth: "100%",
                boxSizing: "border-box"
              }}>
                <div style={{color: "#0B4B66", fontWeight: "bold", marginBottom: 6, overflowWrap: "anywhere"}}>{section.sectionLabel || t(`QC_INSTALLATION_${section.name}`)}</div>
                {section.reasons.map((reason, j) => (
                  <div style={{display: "flex", gap: "16px", alignItems: "flex-start", padding: "4px 0"}} key={j}>
                    <div style={{fontWeight: "bold", flex: "0 0 35%", minWidth: 0, overflowWrap: "anywhere"}}>{reason.reason}</div>
                    <div style={{color: "#555", flex: "1 1 auto", minWidth: 0, overflowWrap: "anywhere", wordBreak: "break-word"}}>{reason.comment}</div>
                  </div>
                ))}
              </div>
            ))}
          </div>
        )}
      </div>
    )
  }

  return (
    <div style={{
      marginTop: "15px",
      width: "95%",
      padding: "20px",
      background: "white",
      borderRadius: "4px",
      boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
      border: "1px solid #eee",
      borderTop: "none",
      borderBottom: "none",
      minWidth: "900px"
    }}>
      <h2 style={{ fontWeight: "bold", fontSize: "18px", marginBottom: 20 }}>
        {t("QC_INSTALLATION_AUDIT_TRAIL")}
      </h2>
      <div style={{ display: "flex", flexDirection: "column", position: "relative" }}>
        <React.Fragment>
          {auditTrail?.length === 1 ? (
            <CheckPoint
              isCompleted={true}
              label={t("CS_" + auditTrail[0]?.status.toUpperCase())}
              customChild={getTimelineCaptions(auditTrail[0])}
            />
          ) : (
            <ConnectingCheckPoints>
              {auditTrail &&
                auditTrail.map((checkpoint, index, arr) => {
                  return (
                    <React.Fragment key={index}>
                      <CheckPoint
                        keyValue={index}
                        isCompleted={index === 0}
                        label={t("CS_" + checkpoint.status.toUpperCase())}
                        customChild={getTimelineCaptions(checkpoint)}
                      />
                    </React.Fragment>
                  );
                })}
            </ConnectingCheckPoints>
          )}
        </React.Fragment>
      </div>
    </div>
  );
}


export default AuditTrail;
