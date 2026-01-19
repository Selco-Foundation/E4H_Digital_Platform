import React from "react";
import getRootReducer from "./redux/reducers";
import EmployeeApp from "./App";
import { Loader } from "@egovernments/digit-ui-react-components";
import ORGCard from "./components/ORGCard";

export const ORGReducers = getRootReducer;

const ORGModule = ({ stateCode, userType, tenants }) => {
  const moduleCode = "ORG";
  const language = Digit.StoreData.getCurrentLanguage();
  const { isLoading, data: store } = Digit.Services.useStore({ stateCode, moduleCode, language });
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles?.map(role => role.code);

  // if(!currentUserRoles?.includes("FACILITY_ADMIN")) {
  //   return null;
  // }

  if (isLoading) {
    return <Loader />;
  }
  return <EmployeeApp />;
};

const componentsToRegister = {
  ORGModule,
  ORGCard,
};

export const initORGComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};