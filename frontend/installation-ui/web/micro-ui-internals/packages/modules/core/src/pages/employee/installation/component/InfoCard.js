import React from "react";

const InfoCard = (props) => {
  const value = 40;
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
            <div style={{ width: "100%", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "20px", lineHeight: "100%", letterSpacing: "0px", color: "#0B0C0C" }}>
                Start Date
              </div>
              <div style={{ fontFamily: "Roboto", fontWeight: 400, fontSize: "20px", lineHeight: "137%", letterSpacing: "0px", color: "#0B0C0C" }}>
                08/03/2025
              </div>
            </div>
            <div style={{ width: "100%", display: "flex", justifyContent: "space-between" }}>
              <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "20px", lineHeight: "100%", letterSpacing: "0px", color: "#0B0C0C" }}>
                End Date
              </div>
              <div style={{ fontFamily: "Roboto", fontWeight: 400, fontSize: "20px", lineHeight: "137%", letterSpacing: "0px", color: "#0B0C0C" }}>
                08/03/2025
              </div>
            </div>
          </div>
        </div>
        <div style={{ width: "30%" }}>
          <div style={{ width: "100%", display: "flex", flexDirection: "column", gap: "10px" }}>
            <div style={{ width: "100%", display: "flex", justifyContent: "space-between" }}>
              <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "20px", lineHeight: "100%", letterSpacing: "0px", color: "#0B0C0C" }}>
                No. of Health Facilities Unassigned
              </div>
              <div style={{ fontFamily: "Roboto", fontWeight: 400, fontSize: "20px", lineHeight: "137%", letterSpacing: "0px", color: "#0B0C0C" }}>
                406
              </div>
            </div>
            <div style={{ width: "100%", display: "flex", justifyContent: "space-between" }}>
              <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "20px", lineHeight: "100%", letterSpacing: "0px", color: "#0B0C0C" }}>
                Total Health Facilities Assigned
              </div>
              <div style={{ fontFamily: "Roboto", fontWeight: 400, fontSize: "20px", lineHeight: "137%", letterSpacing: "0px", color: "#0B0C0C" }}>
                407
              </div>
            </div>
            <div style={{ width: "100%", display: "flex", justifyContent: "space-between" }}>
              <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "20px", lineHeight: "100%", letterSpacing: "0px", color: "#0B0C0C" }}>
                Completion Rate
              </div>
              <div>
                <div style={{ display: "flex", gap: `${value > 99 ? "10px" : "20px"}` }}>
                  <div style={{ width: "100px", height: "20px", background: "#E0E0E0", borderRadius: "5px" }}>
                    <div style={{ position: "absolute", height: "20px", width: `${value}px`, background: "#00703C", borderRadius: "5px" }}></div>
                  </div>
                  <div
                    style={{ fontFamily: "Roboto", fontWeight: 400, fontSize: "20px", lineHeight: "137%", letterSpacing: "0px", color: "#0B0C0C" }}
                  >
                    {value}%
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default InfoCard;
