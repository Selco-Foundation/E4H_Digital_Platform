import React, {useEffect} from 'react';
import { Loader, Button, Toast } from "@egovernments/digit-ui-react-components";
import useOrganizationDetails from "../../hooks/useOrganizationDetails";
import {useTranslation} from "react-i18next";
import OrganizationUserTable from "../../components/OrganizationDetails/OrganizationUserTable";
import {populateWorkingOrganization} from "../../redux/actions";
import {useDispatch} from "react-redux";

const OrganizationDetails = () => {

  const { t } = useTranslation();
  const url = window.location.href;
  const organizationId = url.split("organizations/")[1].split("/")[0];
  const dispatch = useDispatch();

  const { isLoading: organizationDataLoading, data: organizationData } = useOrganizationDetails({ id: organizationId });

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

  if (organizationDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{ marginTop: "20px", padding: "16px", overflow: "auto", backgroundColor: "white" }}>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "20px" }}>
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
          onButtonClick={() => {}}
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
      <div>
        <InfoItem title={t("ORGANIZATION_NAME")} value={organizationData?.name} />
        <InfoItem title={t("ORGANIZATION_TYPE")} value={organizationData?.orgType} />
        <InfoItem title={t("ORGANIZATION_SUB_TYPE")} value={organizationData?.orgSubType} />
        <InfoItem title={t("ORGANIZATION_POC_NAME")} value={organizationData?.orgPocName} />
        <InfoItem title={t("ORGANIZATION_POC_PHONE")} value={organizationData?.orgPocPhone} />
        <InfoItem title={t("ORGANIZATION_POC_EMAIL")} value={organizationData?.orgPocEmail} />
        <InfoItem title={t("ORGANIZATION_POC_USERNAME")} value={organizationData?.orgPocUsername} />
        <InfoItem title={t("ORGANIZATION_STATUS")} value={organizationData?.orgStatus} />
      </div>
      <OrganizationUserTable t={t} organizationId={organizationId} />
    </div>
  );
};

export default OrganizationDetails;