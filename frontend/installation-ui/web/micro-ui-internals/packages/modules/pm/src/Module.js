import React, { useEffect } from "react";
import getRootReducer from "./redux/reducers";
import EmployeeApp from "./App";
import { useTranslation } from "react-i18next";
import { LOCALE } from "./constants/Localization";
import { ComplaintIcon, Loader, CitizenHomeCard } from "@egovernments/digit-ui-react-components";
import PMCard from "./components/PMCard";
import DateRangeInput from "./components/CreateProject/DateRangeInput";
import StateSelector from "./components/CreateProject/StateSelector";
import DistrictSelector from "./components/CreateProject/DistrictSelector";
import BlockSelector from "./components/CreateProject/BlockSelector";
import DownloadTemplate from "./components/CreateProject/DownloadTemplate";
import CustomUploadFile from "./components/CreateProject/CustomUploadFile";

export const PMReducers = getRootReducer;

const PMModule = ({ stateCode, userType, tenants }) => {
  const moduleCode = "PM";
  const language = Digit.StoreData.getCurrentLanguage();
  const { isLoading, data: store } = Digit.Services.useStore({ stateCode, moduleCode, language });
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles?.map(role => role.code);

  if(!currentUserRoles?.includes("PROJECT_MANAGER")) {
    return null;
  }

  if (isLoading) {
    return <Loader />;
  }

  Digit.SessionStorage.set("PM_TENANTS", tenants);
  return <EmployeeApp />;
};

const PMLinks = ({ matchPath }) => {
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
  PMModule,
  PMLinks,
  PMCard,
  PMDateRange: DateRangeInput,
  PMStateSelector: StateSelector,
  PMDistrictSelector: DistrictSelector,
  PMBlockSelector: BlockSelector,
  PMDownloadTemplate: DownloadTemplate,
  PMUploadFacilityData: CustomUploadFile
};

export const initPMComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
