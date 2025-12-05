import React from "react";
import getRootReducer from "./redux/reducers";
import EmployeeApp from "./App";
import { Loader } from "@egovernments/digit-ui-react-components";
import VisitTable from "./pages/employee/VisitTable";
import VisitDetails from "./pages/employee/VisitDetails";
import AMCCard from "./components/AMCCard";
import ProjectTable from "./pages/employee/ProjectTable";

export const AMCReducers = getRootReducer;

const AMCModule = ({ stateCode, userType, tenants }) => {
  const moduleCode = "AMC";
  const language = Digit.StoreData.getCurrentLanguage();
  const { isLoading, data: store } = Digit.Services.useStore({ stateCode, moduleCode, language });
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles?.map(role => role.code);

  // if(!currentUserRoles?.includes("AMC_REVIEWER")) {
  //   return null;
  // }

  if (isLoading) {
    return <Loader />;
  }
  return <EmployeeApp />;
};

const componentsToRegister = {
  AMCModule,
  AMCCard,
  AMCProjectTable : ProjectTable,
  AMCFacilityTable : VisitTable,
  AMCFacilityDetails : VisitDetails
};

export const initAMCComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
