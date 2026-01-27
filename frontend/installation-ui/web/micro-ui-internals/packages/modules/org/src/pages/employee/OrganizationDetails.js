import React, {useCallback, useEffect, useState} from 'react';
import { Loader, Button, Toast } from "@egovernments/digit-ui-react-components";
import useOrganizationDetails from "../../hooks/useOrganizationDetails";
import {useTranslation} from "react-i18next";
import OrganizationUserTable from "../../components/OrganizationDetails/OrganizationUserTable";
import {populateWorkingOrganization} from "../../redux/actions";
import {useDispatch} from "react-redux";
import OrganizationModal from "../../components/OrganizationModal";
import {useQueryClient} from "react-query";
import {OrganizationService} from "../../services/Organization";
import CommonUtils from "../../utilities/CommonUtils";

const OrganizationDetails = () => {

  const { t } = useTranslation();
  const url = window.location.href;
  const organizationId = url.split("organizations/")[1].split("/")[0];
  const dispatch = useDispatch();
  const [toast, setToast] = useState(null);
  const [formToast, setFormToast] =  useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [blockUI, setBlockUI] = useState(null);
  const queryClient = useQueryClient();

  const { isLoading: organizationDataLoading, data: organizationData } = useOrganizationDetails({ id: organizationId });

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

  useEffect(() => {
    if (organizationData) {
      dispatch(populateWorkingOrganization(organizationData));
    }
  }, [organizationData]);

  const InfoItem = ({ title, value }) => (
    <div
      style={{
        display: "flex",
        marginBottom: "10px",
        gap: "15px",
      }}
    >
      <div
        style={{
          fontWeight: "bold",
          width: "50%",
        }}
      >
        {title}
      </div>
      <div>{value || t("CORE_COMMON_NOT_APPLICABLE")}</div>
    </div>
  );

  const handleOrganizationEdit = useCallback(async (formData) => {
    try {
      setBlockUI(true);
      setFormToast(null);

      const payload = {
        organisations: [
          {
            ...organizationData,
            name: formData.orgName,
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

      await OrganizationService.updateOrganization(payload);
      await queryClient.invalidateQueries(["ORGANIZATIONS"]);
      await queryClient.invalidateQueries(["ORGANISATION_DETAILS"]);

      setBlockUI(false);
      setShowEditModal(false);
      setToast({ key: "success", label: t("ORG_UPDATE_SUCCESS") });

    } catch (e) {
      setBlockUI(false);
      setFormToast({
        key: "error",
        label: CommonUtils.getApiErrorMessage(e) || t("ORG_UPDATE_FAILED"),
      });
    }
  }, [t, organizationData])

  if (organizationDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{ marginTop: "20px", padding: "16px", overflow: "auto", backgroundColor: "white" }}>
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
      <div style={{ display: "flex", justifyContent: "space-between", padding: "20px" }}>
        <h1
          style={{
            fontSize: "40px",
            fontWeight: "bold",
            fontFamily: "Roboto Condensed",
            margin: "0",
            color: "#0B0C0C",
          }}
        >
          {organizationData?.applicationNumber} {t("DETAILS")}
        </h1>
        <Button
          variation="secondary"
          label={t("CORE_COMMON_EDIT")}
          onButtonClick={() => setShowEditModal(true)}
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
        />
      </div>
      <div style={{padding: "20px"}}>
        <InfoItem title={t("ORGANIZATION_NAME")} value={organizationData?.name} />
        <InfoItem title={t("ORGANIZATION_TYPE")} value={t(`ORGANIZATION_TYPE_${organizationData?.orgType}`)} />
        <InfoItem title={t("ORGANIZATION_SUB_TYPE")} value={t(`ORGANIZATION_SUB_TYPE_${organizationData?.orgSubType}`)} />
        <InfoItem title={t("ORGANIZATION_CODE")} value={organizationData?.code} />
        <InfoItem title={t("ORGANIZATION_STATUS")} value={t(`ORGANIZATION_STATUS_${organizationData?.orgStatus}`)} />
        <InfoItem title={t("ORGANIZATION_POC_NAME")} value={organizationData?.orgPocName} />
        <InfoItem title={t("ORGANIZATION_POC_PHONE")} value={organizationData?.orgPocPhone} />
        <InfoItem title={t("ORGANIZATION_POC_EMAIL")} value={organizationData?.orgPocEmail} />
        <InfoItem title={t("ORGANIZATION_POC_USERNAME")} value={organizationData?.orgPocUsername} />
      </div>
      <OrganizationUserTable t={t} organizationId={organizationId} organizationType={organizationData?.orgType} organizationSubType={organizationData?.orgSubType} />
      {showEditModal && (
        <OrganizationModal
          t={t}
          onSubmit={handleOrganizationEdit}
          heading={t("EDIT_ORGANIZATION")}
          onClose={() => setShowEditModal(false)}
          createdOrganization={organizationData}
          orgType={organizationData?.orgType}
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

export default OrganizationDetails;