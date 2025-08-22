import React, { useEffect } from "react";
import getRootReducer from "./redux/reducers";
import EmployeeApp from "./App";
import { useTranslation } from "react-i18next";
import { LOCALE } from "./constants/Localization";
import { ComplaintIcon, Loader, CitizenHomeCard } from "@egovernments/digit-ui-react-components";
import FieldPlanTable from "./pages/employee/FieldPlanTable";
import FacilityTable from "./pages/employee/FacilityTable";
import FacilityDetails from "./pages/employee/FacilityDetails";
import QCCard from "./components/QCCard";
import { useSelector } from "react-redux";

export const QCReducers = getRootReducer;

const QCModule = ({ stateCode, userType, tenants }) => {
  const moduleCode = "QC";
  const language = Digit.StoreData.getCurrentLanguage();
  const { isLoading, data: store } = Digit.Services.useStore({ stateCode, moduleCode, language });

  if (isLoading) {
    return <Loader />;
  }

  Digit.SessionStorage.set("IM_TENANTS", tenants);
  return <EmployeeApp />;
};

const QCLinks = ({ matchPath }) => {
  const { t } = useTranslation();
  const [params, setParams, clearParams] = Digit.Hooks.useSessionStorage("PGR_CITIZEN_CREATE_COMPLAINT", {});

  useEffect(() => {
    clearParams();
  }, []);

  const links = [
    {
      link: `${matchPath}/create-complaint/complaint-type`,
      i18nKey: t("CS_COMMON_FILE_A_COMPLAINT"),
    },
    {
      link: `${matchPath}/complaints`,
      i18nKey: t(LOCALE.MY_COMPLAINTS),
    },
  ];

  return <CitizenHomeCard header={t("CS_COMMON_HOME_COMPLAINTS")} links={links} Icon={ComplaintIcon} />;
};

const componentsToRegister = {
  QCModule,
  QCLinks,
  QCCard,
  QCFieldPlanTable : FieldPlanTable,
  QCFacilityTable : FacilityTable,
  QCFacilityDetails : FacilityDetails
};

export const initQCComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
