import React, { useEffect, useState } from "react";
import { DownloadIcon, Toast, PopUp, Button } from "@egovernments/digit-ui-react-components";
import CustomUploadIcon from "../Custom/CustomUploadIcon";
import { FacilityService } from "../../services/Facility";
import FacilityForm from "../FacilityForm";
import FacilityModal from "../FacilityModal";
import { useHistory } from "react-router-dom";

const FacilityAdminActions = ({ t }) => {

  const [toast, setToast] = useState(null);
  const [showAddFacilityModal, setShowAddFacilityModal] = useState(false);
  const history = useHistory();

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
            label={toast.message}
            onClose={() => setToast(null)}
            style={{ maxWidth: "670px" }}
            isDleteBtn={true}
          />
        )}
        {showAddFacilityModal && (
          <FacilityModal
            t={t}
            title={"ADD_FACILITY"}
            onSubmit={handleAddFacilitySubmit}
            onClose={() => setShowAddFacilityModal(false)}
          />
        )}
      </div>
    </React.Fragment>
  );
};

export default FacilityAdminActions;
