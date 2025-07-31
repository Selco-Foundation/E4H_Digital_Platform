import React from "react";
import { CheckPoint, ConnectingCheckPoints } from "@egovernments/digit-ui-react-components";

const AuditTrial = ({t, auditTrial}) => {
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
      overflow: "hidden",
    }}>
      <h2 style={{ fontWeight: "bold", fontSize: "18px", marginBottom: 20 }}>Audit Trail</h2>
      <div style={{ display: "flex", flexDirection: "column", position: "relative" }}>
        <React.Fragment>
          {auditTrial?.length === 1 ? (
            <CheckPoint isCompleted={true} label={t("CS_COMMON_" + auditTrial[0]?.status.toUpperCase())} />
          ) : (
            <ConnectingCheckPoints>
              {auditTrial &&
                auditTrial.map((checkpoint, index, arr) => {
                  return (
                    <React.Fragment key={index}>
                      <CheckPoint
                        keyValue={index}
                        isCompleted={index === 0}
                        label={t("CS_COMMON_" + checkpoint.status.toUpperCase())}
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

const getTimelineCaptions = (checkpoint) => {
  return (
    <div style={{ marginTop: "12px", width: "800px" }}>
      {/*<div style={{fontWeight: "bold", marginBottom: 4}}>{checkpoint.status}</div>*/}
      <div style={{fontSize: "14px", color: "#666"}}>{checkpoint.date}</div>
      {checkpoint.reasons && (
        <div style={{ marginTop: 12 }}>
          {checkpoint.reasons.map((section, i) => (
            <div key={i} style={{
              backgroundColor: "#f9f9f9",
              border: "1px solid #eee",
              borderRadius: 4,
              padding: 10,
              marginTop: 10
            }}>
              <div style={{color: "#007acc", fontWeight: "bold", marginBottom: 6}}>{section.section}</div>
              {section.reasons.map((r, j) => (
                <div style={{display: "flex", justifyContent: "space-between", padding: "4px 0"}} key={j}>
                  <div style={{fontWeight: "bold", width: "50%", marginRight: "10px"}}>{r.title}</div>
                  <div style={{color: "#555", width: "50%"}}>{r.details}</div>
                </div>
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default AuditTrial;