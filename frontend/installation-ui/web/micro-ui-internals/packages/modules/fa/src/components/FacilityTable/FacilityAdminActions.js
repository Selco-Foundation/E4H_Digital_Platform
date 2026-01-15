import React, { useEffect, useState } from "react";
import { Toast, Loader } from "@egovernments/digit-ui-react-components";
import { FacilityService } from "../../services/Facility";
import FacilityModal from "../FacilityModal";
import { useHistory } from "react-router-dom";

const FacilityAdminActions = ({ t }) => {

  const [toast, setToast] = useState(null);
  const [showAddFacilityModal, setShowAddFacilityModal] = useState(false);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [blockUI, setBlockUI] = useState(null);
  const history = useHistory();

  const tenantId = Digit.ULBService.getCurrentTenantId();

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (toast) {
      setTimeout(() => {
        setToast(null);
      }, 2500);
    }
  }, [toast]);

  const handleAddFacilitySubmit = async (formData) => {
    try {
      setBlockUI(true);
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
            isActive: formData?.isActive?.code === "YES",
            isOnmReady: formData?.isOnmReady?.code === "YES",
            blockBoundaryCode: block?.code,
            address: {
              tenantId: tenantId,
              ...(formData?.latitude ? { latitude: formData.latitude } : {}),
              ...(formData?.longitude ? { longitude: formData.longitude } : {}),
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

      setBlockUI(false);
      setShowAddFacilityModal(false);
      setToast({
        key: "success",
        label: t("FACILITY_CREATION_SUCCESS"),
      });
    } catch (e) {
      console.error("Failed to create facility", e);
      setBlockUI(false);
      setToast({
        key: "error",
        label: t("FACILITY_CREATION_FAILED"),
      });
    }
  };

  const handleAddFacility = () => {
    setShowAddFacilityModal(true);
  };

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
        {blockUI && (
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              height: "100%",
              width: "100%",
              zIndex: 10000005,
              backgroundColor: "gray",
              opacity: 0.5,
              position: "fixed",
              top: 0,
              left: 0,
            }}
          >
            <Loader />
          </div>
        )}
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
            onClick={() => history.push(`/${window?.contextPath}/employee/fa/facilities/bulk-add`)}
          >
            <span>{t("BULK_ADD")}</span>
          </button>
        </div>
        {toast && (
          <Toast
            error={toast.key === "error"}
            warning={toast.key === "warning"}
            style={{
              ...(toast.key === "error" ? { backgroundColor: "#B91900" } : {}),
              ...(mobileView ? { bottom: "120px" } : {}),
            }}
            label={toast.label}
            isDleteBtn={true}
            onClose={() => setToast(null)}
          />
        )}
        {showAddFacilityModal && (
          <FacilityModal t={t} title={"ADD_FACILITY"} onSubmit={handleAddFacilitySubmit} onClose={() => setShowAddFacilityModal(false)} />
        )}
      </div>
    </React.Fragment>
  );
};

export default FacilityAdminActions;
