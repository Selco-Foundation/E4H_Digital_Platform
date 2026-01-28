import React, {useEffect, useState} from 'react';
import UserModal from "./UserModal";
import { Toast, Loader } from "@egovernments/digit-ui-react-components";
import {VendorService} from "../../../services/Vendor";
import {useQueryClient} from "react-query";

const OrganizationUserAdminActions = ({ t, organizationId, organizationType, organizationSubType }) => {

  const [toast, setToast] = useState(null);
  const [formToast, setFormToast] =  useState(null);
  const [showUserModal, setShowUserModal] = useState(false);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [blockUI, setBlockUI] = useState(null);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (toast) {
      setTimeout(() => {
        setToast(null);
      }, 2500);
    }
  }, [toast]);

  useEffect(() => {
    if (formToast) {
      setTimeout(() => {
        setFormToast(null);
      }, 2500);
    }
  }, [formToast]);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const handleOrganizationUserCreate = async (userFormData, jurisdictions) => {
    try {
      setBlockUI(true);
      const organizationUser = {
        organizationId: organizationId,
        user: {
          name: userFormData.name,
          userName: userFormData.userName,
          mobileNumber: userFormData.contact,
          emailId: userFormData.email,
          tenantId: Digit.ULBService.getCurrentTenantId(),
          roles: userFormData.roles.map(role => ({...role, tenantId: Digit.ULBService.getCurrentTenantId()})),
          jurisdiction: jurisdictions
        }
      }
      await VendorService.createOrganizationUser(organizationUser)
      await queryClient.invalidateQueries(["ORGANISATION_USER"]);

      setBlockUI(false);
      setShowUserModal(false);
      setToast({
        key: "success",
        label: t("ORGANIZATION_USER_CREATION_SUCCESS"),
      })
    } catch (e) {
      console.error("Failed to edit organization user", e);
      setBlockUI(false);
      setFormToast({
        key: "error",
        label: t("ORGANIZATION_USER_CREATION_FAILED"),
      });
    }
  }

  return (
    <div>
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
      <div style={{ display: "flex", justifyContent: "space-between", margin: "0px 20px" }}>
        <h1 style={{ fontSize: "24px", fontWeight: "bold" }}>{t("ORG_USER_LIST")}</h1>
        <button
          id={"orgAddUserBtn"}
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
          onClick={() => setShowUserModal(true)}
        >
          <span>{t("ADD_USER")}</span>
        </button>
      </div>
      {showUserModal && (
        <UserModal
          t={t}
          title={"ADD_USER"}
          onClose={() => setShowUserModal(false)}
          onSubmit={handleOrganizationUserCreate}
          organizationType={organizationType}
          organizationSubType={organizationSubType}
          formToast={formToast}
          setFormToast={setFormToast}
        />
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
};

export default OrganizationUserAdminActions;