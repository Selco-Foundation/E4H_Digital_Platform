import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "react-query";
import { Button, Toast } from "@egovernments/digit-ui-react-components";

import OrganizationModal from "../OrganizationModal/index";
import { OrganizationService } from "../../services/Organization";

const getApiErrorMessage = (e) => {
  return (
    (e &&
      e.response &&
      e.response.data &&
      e.response.data.Errors &&
      e.response.data.Errors[0] &&
      e.response.data.Errors[0].message) ||
    (e && e.message) ||
    "Unknown error"
  );
};

const OrganizationAdminActions = ({ orgType }) => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const [showModal, setShowModal] = useState(false);

  const [toast, setToast] = useState(null);
  const [formToast, setFormToast] = useState(null);

  const [isSubmitting, setIsSubmitting] = useState(false);
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
      setIsSubmitting(true);
      setFormToast(null);

      const tenantId = Digit.ULBService.getCurrentTenantId();

      const payload = {
        organisations: [
          {
            tenantId,
            name: formData.orgName,
            code: formData.orgCode,
            orgType: orgType || "VENDOR",
            orgStatus: formData.orgStatus ? formData.orgStatus.code : "ACTIVE",

            orgPocName: formData.orgPocName || null,
            orgPocPhone: formData.orgPocPhone || null,
            orgPocEmail: formData.orgPocEmail || null,
            orgPocUsername: formData.orgPocPhone || null,

            isActive: true,

            orgAddress: [
              {
                tenantId,
                boundaryType: null,
                boundaryCode: null,
                hqAddress: formData.hqAddress,
                geoLocation: {
                  latitude: Number(formData.latitude),
                  longitude: Number(formData.longitude),
                },
              },
            ],
          },
        ],
      };

      await OrganizationService.createOrganization(payload);

      await queryClient.invalidateQueries(["ORGANIZATIONS"]);

      setShowModal(false);
      setToast({ key: "success", label: t("ORG_CREATE_SUCCESS") });
    } catch (e) {
      setFormToast({
        key: "error",
        label: getApiErrorMessage(e) || t("ORG_CREATE_FAILED"),
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const openModal = () => {
    setFormToast(null);
    setShowModal(true);
  };

  const closeModal = () => {
    if (isSubmitting) return;
    setShowModal(false);
  };

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
      <Button
        variation={"secondary"}
        label={ orgType === "PLATFORM" ? t("ADD_PLATFORM_ORG") : t("ADD_VENDOR_ORG") }
        onButtonClick={openModal}
      />

      {showModal ? (
        <OrganizationModal
          t={t}
          orgType={orgType}
          onSubmit={handleSubmit}
          onClose={closeModal}
          isLoading={isSubmitting}
          formToast={formToast}
          setFormToast={setFormToast}
        />
      ) : null}

      {toast ? (
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
      ) : null}
    </div>
  );
};

export default OrganizationAdminActions;