import React from "react";
import { Loader, PopUp, Button, CheckPoint, ConnectingCheckPoints } from "@egovernments/digit-ui-react-components";
import { PdfIcon } from "@egovernments/digit-ui-svg-components";
import useAMCVisit from "../../../hooks/useAMCVisit";

const VisitsModal = ({ t, amcConfigurationId, onClose }) => {

  const { isLoading, data: visitData } = useAMCVisit(amcConfigurationId);

  const getTimelineCaptions = (checkpoint) => {
    return (
      <div style={{ marginTop: "12px" }}>
        <div style={{ fontSize: "14px", color: "#666", marginBottom: "10px" }}>{checkpoint.scheduledDate}</div>
        {!!checkpoint.visitReport && (
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
              href={checkpoint.visitReport.fileUrl}
              download={"installation-completion-report.pdf"}
            >
              <div style={{ display: "flex", alignItems: "center" }}>
                <PdfIcon style={{ marginRight: "12px" }} />
                <div>
                  <div style={{ fontWeight: "bold", fontSize: "16px" }}>{checkpoint.visitReport.name}</div>
                  {checkpoint.visitReport.size && <div style={{ color: "#666", fontSize: "14px" }}>{checkpoint.visitReport.size}</div>}
                </div>
              </div>
            </a>
          </div>
        )}
      </div>
    );
  };

  const renderAMCVisits = () => {
    if (isLoading) return <Loader />;

    return (
      <div
        style={{
          display: "flex",
          overflowY: "auto",
        }}
      >
        {!visitData?.amcVisits?.length && <div style={{ padding: "30px" }}>{t("AMC_NO_VISIT_FOUND")}</div>}
        {!!visitData?.amcVisits?.length && (
          <div
            style={{
              display: "flex",
              flexDirection: "column",
              position: "relative",
              padding: "20px",
              overflowY: "auto",
              maxHeight: "70vh",
              width: "100%",
            }}
          >
            <React.Fragment>
              {visitData.amcVisits.length === 1 ? (
                <CheckPoint
                  isCompleted={true}
                  label={t("CS_" + visitData.amcVisits[0].status?.toUpperCase())}
                  customChild={getTimelineCaptions(visitData.amcVisits[0])}
                />
              ) : (
                <ConnectingCheckPoints>
                  {visitData.amcVisits.map((checkpoint, index) => {
                    return (
                      <React.Fragment key={index}>
                        <CheckPoint
                          keyValue={index}
                          isCompleted={!(["DRAFT"].includes(checkpoint.status))}
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
        )}
      </div>
    );
  }

  return (
    <PopUp>
      <div
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "700px",
          maxWidth: "95%",
          overflow: "hidden",
          borderRadius: "5px",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "20px 30px 0px",
          }}
        >
          <div
            style={{
              fontFamily: "Roboto",
              fontWeight: 700,
              fontSize: "24px",
              color: "#0B0C0C",
            }}
          >
            {t("AMC_VISITS")}
          </div>
          <Button
            variation="secondary"
            label={t("CORE_COMMON_CLOSE")}
            onButtonClick={onClose}
            style={{
              backgroundColor: "white",
              border: "1px solid #d35400",
              color: "#d35400",
              padding: "8px 20px",
              cursor: "pointer",
              fontWeight: "bold",
              fontSize: "16px",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              gap: "5px",
              height: "40px",
            }}
          />
        </div>
        {renderAMCVisits()}
      </div>
    </PopUp>
  );
};

export default VisitsModal;