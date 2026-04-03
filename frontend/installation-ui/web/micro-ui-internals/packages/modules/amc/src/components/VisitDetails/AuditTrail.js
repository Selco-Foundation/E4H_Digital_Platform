import React from "react";
import { CheckPoint, ConnectingCheckPoints } from "@egovernments/digit-ui-react-components";

const AuditTrail = ({t, auditTrail}) => {

  const getTimelineCaptions = (checkpoint) => {
    return (
      <div style={{ marginTop: "12px", width: "800px" }}>
        <div style={{fontSize: "14px", color: "#666"}}>{checkpoint.date}</div>
        {checkpoint.reasons?.length > 0 && (
          <div style={{ marginTop: 12 }}>
            <div style={{
              backgroundColor: "#f9f9f9",
              border: "1px solid #eee",
              borderRadius: 4,
              padding: 10,
              marginTop: 10
            }}>
              {checkpoint.reasons.map((reason, i) => (
                <div style={{display: "flex", justifyContent: "space-between", padding: "4px 0"}} key={i}>
                  <div style={{fontWeight: "bold", width: "50%", marginRight: "10px"}}>{reason.reason}</div>
                  <div style={{color: "#555", width: "50%"}}>{reason.comment}</div>
                </div>
              ))}
            </div>
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
        {t("AUDIT_TRAIL")}
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
                auditTrail.map((checkpoint, index) => {
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