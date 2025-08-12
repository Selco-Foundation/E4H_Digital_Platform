import React from "react";

const InfoCard = ({ t, selectedFieldPlan }) => {

  const { startDate, endDate, completionRate, projectFacilityInfo } = selectedFieldPlan;

  const PropertyCard = (infoName, infoValue) => (
    <div style={{ width: "100%", display: "flex", alignItems: "center" }}>
      <div style={{ fontFamily: "Roboto", width: "100%", fontWeight: 700, fontSize: "20px", lineHeight: "100%", letterSpacing: "0px", color: "#0B0C0C" }}>
        {infoName}
      </div>
      <div style={{ fontFamily: "Roboto", width: "250px", fontWeight: 400, fontSize: "20px", lineHeight: "137%", letterSpacing: "0px", color: "#0B0C0C" }}>
        {infoValue}
      </div>
    </div>
  )

  const GetProgress = (value) => (
    <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
      <div style={{ width: "100px", height: "20px", background: "#E0E0E0", borderRadius: "5px" }}>
        <div style={{ position: "absolute", height: "20px", width: `${value}px`, background: "#00703C", borderRadius: "5px" }}></div>
      </div>
      <div>{value}%</div>
    </div>
  );

  const getStatusCount = (status) => {
    return projectFacilityInfo?.[status] || 0;
  }

  const getAssignedCount = () => {
    let assignedCount = 0;
    for (const key in projectFacilityInfo) {
      if (key !== "SCHEDULED")
        assignedCount += projectFacilityInfo[key];
    }

    return assignedCount;
  }

  return (
    <React.Fragment>
      <div
        style={{
          width: "99%",
          background: "white",
          height: "fit-content",
          marginBottom: "15px",
          padding: "20px",
          display: "flex",
          justifyContent: "space-between",
        }}
      >
        <div style={{ width: "30%" }}>
          <div style={{ width: "100%", display: "flex", flexDirection: "column", gap: "10px" }}>
            <div>
              {PropertyCard(t("CORE_COMMON_START_DATE"), startDate)}
            </div>
            <div>
              {PropertyCard(t("CORE_COMMON_COMPLETION_DATE"), endDate)}
            </div>
          </div>
        </div>
        <div style={{ width: "30%" }}>
          <div style={{ width: "100%", display: "flex", flexDirection: "column", gap: "10px" }}>
            <div>
              {PropertyCard(t("FIELD_PLAN_NO_OF_FACILITIES_UNASSIGNED"), getStatusCount("SCHEDULED"))}
            </div>
            <div>
              {
                PropertyCard(
                  t("FIELD_PLAN_NO_OF_FACILITIES_ASSIGNED"),
                  getAssignedCount()
                )
              }
            </div>
            <div>
              {
                PropertyCard(
                  t("FIELD_PLAN_COMPLETION_RATE"),
                  GetProgress(completionRate)
                )
              }
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default InfoCard;
