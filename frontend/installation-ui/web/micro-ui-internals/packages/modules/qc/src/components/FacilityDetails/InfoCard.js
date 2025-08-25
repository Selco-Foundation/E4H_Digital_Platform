import React from "react";

const InfoCard = ({ t, facilityDetails }) => {

  if (!facilityDetails) return <div></div>;

  const InfoCardItem = (infoName, infoValue) => (
    <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
      <div style={{width: "30%"}}><strong>{infoName}</strong></div>
      <span>{infoValue}</span>
    </div>
  )

  return (
    <div style={{
      marginTop: "15px",
      width: "95%",
      padding: "20px",
      background: "white",
      borderRadius: "4px",
      boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
      border: "1px solid #eee",
      minWidth: "900px"
    }}>
      {InfoCardItem(t("CS_DISTRICT"), facilityDetails.district ? t(`DISTRICT_${facilityDetails.district.toUpperCase()}`) : "-")}
      {InfoCardItem(t("CS_BLOCK"), facilityDetails.block ? t(`BLOCK_${facilityDetails.block.toUpperCase()}`) : "-")}
      {InfoCardItem(t("CS_HEALTH_FACILITY_TYPE"), facilityDetails.facilityType ? facilityDetails.facilityType : "-")}
      {InfoCardItem(t("CS_STATUS"), facilityDetails.status ? t(`CS_${facilityDetails.status}`) : "-")}
    </div>
  );
}

export default InfoCard;