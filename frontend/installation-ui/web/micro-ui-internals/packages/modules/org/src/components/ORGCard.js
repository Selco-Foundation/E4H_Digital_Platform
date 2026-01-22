import React, {useEffect, useState} from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { ListAlt, AdminPanelSettings } from "@egovernments/digit-ui-svg-components";
import { Loader, Toast } from "@egovernments/digit-ui-react-components";
import { OrganizationService } from "../services/Organization";


const ORGCard = () => {

  const history = useHistory();
  const { t } = useTranslation();
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles?.map(role => role.code);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);

  useEffect(() => {
    if (toast) {
      const id = setTimeout(() => setToast(null), 2500);
      return () => clearTimeout(id);
    }
  }, [toast]);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  // if (!currentUserRoles?.includes("FACILITY_ADMIN")) {
  //   return null;
  // }

  const isVendorAdminUser = currentUserRoles?.includes("VENDOR_ADMIN");
  const userType = "employee";
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const userUuid = info?.uuid;

  const handleManageVendorsRedirection = () => {
    history.push(`/${window?.contextPath}/employee/org/vendors`);
  };

  const handleManagePlatformsRedirection = () => {
    history.push(`/${window?.contextPath}/employee/org/platforms`);
  };

  const handleOrganizationDetailsPageRedirection = async () => {
    try {
      setBlockUI(true);
      const res = await OrganizationService.searchOrgUsers(tenantId, userUuid ? [userUuid] : [], 0,1);

      setBlockUI(false);
      const orgId = res?.OrgUsers?.[0]?.organizationId;

      if (!orgId) {
        setToast({ key: "error", label: t("ORG_EDIT_ORG_NOT_FOUND") });
        return;
      }

      history.push(`/${window?.contextPath}/employee/org/organizations/${orgId}/details`);
    } catch (e) {
      setBlockUI(false);
      setToast({ key: "error", label: t("ORG_EDIT_ORG_FETCH_FAILED") });
    }
  };

  return (
    <div
      style={{
        marginLeft: "0px",
        margin: userType === "citizen" ? "8px" : "0px",
        gap: userType === "citizen" ? "" : "0 24px",
        boxShadow: "1px 1px 4px 0px rgba(0,0,0,0.2)",
        backgroundColor: "white",
        borderRadius: "4px",
        width: "320px",
        maxWidth: "95%",
        minHeight: "297px",
        position: "relative",
        padding: "24px",
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
      <div
        style={{
          marginBottom: "20px",
          padding: "8px 0px 27px 0px",
          display: "flex",
          gap: "16px",
          alignItems: "center",
          lineHeight: "35px",
          borderBottom: "1px solid #D6D5D4",
        }}
      >
        <AdminPanelSettings height={"28px"} width={"28px"} />
        <div
          style={{
            fontFamily: "Roboto",
            fontWeight: "700",
            fontSize: "24px",
            lineHeight: "100%",
            letterSpacing: "0px",
            color: "#0B4B66",
            width: "70%",
          }}
        >
          {t("ORG_CARD_HEADING")}
        </div>
      </div>
      {isVendorAdminUser ? (
        <div>
          <button
            style={{
              display: "flex",
              gap: "8px",
              alignItems: "center",
              color: "#C84C0E",
              fontSize: "16px",
              fontWeight: "500",
              fontFamily: "Roboto",
              background: "transparent",
              border: "none",
            }}
            onClick={handleOrganizationDetailsPageRedirection}
          >
            <ListAlt />
            <span>
              {t("ORG_ACTION_EDIT_ORGANIZATION")}
            </span>
          </button>
        </div>
      ) : (
        <div>
          <button
            style={{
              display: "flex",
              gap: "8px",
              alignItems: "center",
              color: "#C84C0E",
              cursor: "pointer",
              marginBottom: "15px",
              fontSize: "16px",
              fontWeight: "500",
              fontFamily: "Roboto",
              background: "transparent",
              border: "none",
            }}
            onClick={handleManageVendorsRedirection}
          >
            <ListAlt />
            <span>{t("ORG_ACTION_MANAGE_VENDOR_ORGANIZATION")}</span>
          </button>
          <button
            style={{
              display: "flex",
              gap: "8px",
              alignItems: "center",
              color: "#C84C0E",
              cursor: "pointer",
              fontSize: "16px",
              fontWeight: "500",
              fontFamily: "Roboto",
              background: "transparent",
              border: "none",
            }}
            onClick={handleManagePlatformsRedirection}
          >
            <ListAlt />
            <span>{t("ORG_ACTION_MANAGE_PLATFORM_ORGANIZATION")}</span>
          </button>
        </div>
      )}
      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          style={{
            zIndex: 100000000,
            ...(toast.key === "error" ? { backgroundColor: "#B91900" } : {}),
            ...(mobileView ? { bottom: "120px" } : {}),
          }}
          label={toast.label}
          isDleteBtn={true}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
}

export default ORGCard;