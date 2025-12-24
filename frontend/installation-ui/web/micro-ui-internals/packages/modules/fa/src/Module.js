import React from "react";
import getRootReducer from "./redux/reducers";
import EmployeeApp from "./App";
import { Loader } from "@egovernments/digit-ui-react-components";
import FACard from "./components/FACard";

export const FAReducers = getRootReducer;

const FAModule = ({ stateCode, userType, tenants }) => {
  const moduleCode = "FA";
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
  FAModule,
  FACard,
};

export const initFAComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
