import React, { useEffect, useState } from "react";
import { DownloadIcon, Toast, PopUp, Button } from "@egovernments/digit-ui-react-components";
import CustomUploadIcon from "../Custom/CustomUploadIcon";
import { FacilityService } from "../../services/Facility";
import FacilityForm from "../FacilityForm";

const FacilityAdminActions = ({ t }) => {

  const [toast, setToast] = useState(null);
  const [showAddFacilityModal, setShowAddFacilityModal] = useState(false);

  const tenantId = Digit.ULBService.getCurrentTenantId();

  useEffect(() => {
    if (toast) {
      setTimeout(() => {
        setToast(null);
      }, 2500);
    }
  }, [toast]);

  const handleAddFacilitySubmit = async (formData) => {
    try {
      const facilityTypeValue = formData?.facilityType;
      const solarDesignValue = formData?.solarSolutionDesignType;
      const block = formData?.block;

      const facilityTypeCode = facilityTypeValue?.code;
      const solarDesignCode = solarDesignValue?.code;

      const payload = {
        facilities: [
          {
            tenant_id: tenantId,
            facility_name: formData?.facilityName,
            facility_type: facilityTypeCode,
            isActive: true,
            blockBoundaryCode: block?.code,
            address: {
              tenantId: tenantId,
              ...(formData?.pincode ? { pincode: formData.pincode } : {}),
            },
            facility_poc_name: formData?.facilityPocName,
            facility_poc_phone: formData?.facilityPocPhone,
            facility_poc_email: formData?.facilityPocEmail,
            hfr_id: formData?.hfrId,
            nin_id: formData?.ninId,
            facility_details: {
              solar_solution_design_type: solarDesignCode,
            },
          },
        ],
      };

      await FacilityService.createFacility(payload);

      setShowAddFacilityModal(false);
      setToast({
        key: "success",
        message: t("FACILITY_SUCCESS"),
      });
    } catch (e) {
      setToast({
        key: "error",
        message: t("FACILITY_FAILED"),
      });
    }
  };

  const handleAddFacility = () => {
    setShowAddFacilityModal(true);
  };

  const handleBulkAddTemplateDownload = () => {

  }

  const handleBulkAddUpload = () => {

  }

  return (
    <React.Fragment>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          minWidth: "fit-content",
        }}
      >
        <h1
          style={{
            fontSize: "40px",
            fontWeight: "bold",
            fontFamily: "Roboto Condensed",
            margin: "0",
            color: "#0B0C0C",
          }}
        >
          {t("FACILITIES")}
        </h1>
        <div style={{ display: "flex", gap: "16px" }}>
          <button
            id={"faAddFacilityBtn"}
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
            onClick={handleAddFacility}
          >
            <span>{t("ADD_FACILITY")}</span>
          </button>
          <button
            id={"faBulkAddTemplateDownloadBtn"}
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
            onClick={handleBulkAddTemplateDownload}
          >
            <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
              <DownloadIcon fill={"#d35400"} />
            </div>
            <span>{t("BULK_ADD_TEMPLATE")}</span>
          </button>
          <button
            id={"faBulkAddUploadBtn"}
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
            onClick={handleBulkAddUpload}
          >
            <CustomUploadIcon fill={"#C84C0E"} height={"20"} width={"20"} />
            <span>{t("BULK_ADD")}</span>
          </button>
        </div>
        {toast && (
          <Toast
            error={toast.key === "error"}
            warning={toast.key === "warning"}
            label={`${toast.message} ${toast.failedCount ? `(${toast.failedCount} ${t("QC_BULK_APPROVE_FAILED_COUNT")})` : ""}`}
            onClose={() => setToast(null)}
            style={{ maxWidth: "670px" }}
            isDleteBtn={true}
          />
        )}
        {showAddFacilityModal && (
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
                maxHeight: "90vh",
                overflowY: "auto",
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
                  {t("ADD_FACILITY")}
                </div>
                <Button variation="secondary" label={t("CORE_COMMON_CLOSE")} onButtonClick={() => setShowAddFacilityModal(false)} />
              </div>
              <FacilityForm t={t} handleFormSubmit={handleAddFacilitySubmit} wrapperStyle={{ paddingTop: "0px" }} />
            </div>
          </PopUp>
        )}
      </div>
    </React.Fragment>
  );
};

export default FacilityAdminActions;
