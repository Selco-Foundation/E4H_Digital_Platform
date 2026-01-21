import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "react-query";
import { Button, Toast, Loader } from "@egovernments/digit-ui-react-components";

import OrganizationModal from "../OrganizationModal/index";
import { OrganizationService } from "../../services/Organization";

const getApiErrorMessage = (e) => {
  return (e?.response?.data?.Errors?.[0]?.message)
    ? e.response.data.Errors[0].message
    : (e?.message ? e.message : "");
};

const OrganizationAdminActions = ({ orgType }) => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const queryClient = useQueryClient();
  const [showModal, setShowModal] = useState(false);
  const [toast, setToast] = useState(null);
  const [formToast, setFormToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);

  useEffect(() => {
    if (toast) {
      const id = setTimeout(() => setToast(null), 2500);
      return () => clearTimeout(id);
    }
  }, [toast]);

  useEffect(() => {
    if (formToast) {
      const id = setTimeout(() => setFormToast(null), 2500);
      return () => clearTimeout(id);
    }
  }, [formToast]);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const handleSubmit = async (formData) => {
    try {
      setBlockUI(true);
      setFormToast(null);

      const payload = {
        organisations: [
          {
            tenantId,
            name: formData.orgName,
            code: formData.orgCode,
            orgType: orgType,
            orgSubType: formData.orgSubType?.code,
            orgStatus: formData.orgStatus?.code,
            orgPocName: formData.orgPocName,
            orgPocPhone: formData.orgPocPhone,
            orgPocEmail: formData.orgPocEmail,
            orgPocUsername: formData.orgPocUsername,
            isActive: true,
            orgAddress: [],
          },
        ],
      };

      await OrganizationService.createOrganization(payload);
      await queryClient.invalidateQueries(["ORGANIZATIONS"]);

      setBlockUI(false);
      setShowModal(false);
      setToast({ key: "success", label: t("ORG_CREATE_SUCCESS") });
    } catch (e) {
      setBlockUI(false);
      setFormToast({
        key: "error",
        label: getApiErrorMessage(e) || t("ORG_CREATE_FAILED"),
      });
    }
  };

  const openModal = () => {
    setFormToast(null);
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
  };

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
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
      <Button
        variation={"secondary"}
        label={ orgType === "PLATFORM" ? t("ADD_PLATFORM_ORG") : t("ADD_VENDOR_ORG") }
        onButtonClick={openModal}
      />
      {showModal && (
        <OrganizationModal
          t={t}
          orgType={orgType}
          onSubmit={handleSubmit}
          onClose={closeModal}
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

export default OrganizationAdminActions;