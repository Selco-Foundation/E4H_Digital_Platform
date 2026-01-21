import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "react-query";
import { Button } from "@egovernments/digit-ui-react-components";

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
  const [showModal, setShowModal] = useState(false);

  // ✅ toast shown inside modal (FA style)
  const [formToast, setFormToast] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const queryClient = useQueryClient();

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

      queryClient.invalidateQueries(["ORGANIZATIONS"]);

      setShowModal(false);
      setFormToast({ key: "success", label: t("ORG_CREATE_SUCCESS") });
    } catch (e) {
      setFormToast({
        key: "error",
        label: getApiErrorMessage(e) || t("ORG_CREATE_FAILED"),
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
      <Button
        variation={"secondary"}
        label={orgType === "PLATFORM" ? t("ADD_PLATFORM_ORG") || t("ADD_ORGANIZATION") : t("ADD_VENDOR_ORG") || t("ADD_ORGANIZATION")}
        onButtonClick={() => setShowModal(true)}
      />

      {showModal ? (
        <OrganizationModal
          t={t}
          orgType={orgType}
          onSubmit={handleSubmit}
          onClose={() => setShowModal(false)}
          isLoading={isSubmitting}
          formToast={formToast}
          setFormToast={setFormToast}
        />
      ) : null}
    </div>
  );
};

export default OrganizationAdminActions;