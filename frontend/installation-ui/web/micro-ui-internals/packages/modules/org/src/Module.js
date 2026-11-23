import React from "react";
import getRootReducer from "./redux/reducers";
import { ORG_PLATFORM_ADMIN } from "./constants/Roles";
import { isSingleOrgAdmin } from "./utilities/orgAdminRoles";
import EmployeeApp from "./App";
import { Loader } from "@egovernments/digit-ui-react-components";
import ORGCard from "./components/ORGCard";
import StateSelector from "./components/FormComposer/StateSelector";
import DistrictSelector from "./components/FormComposer/DistrictSelector";
import BlockSelector from "./components/FormComposer/BlockSelector";
import CountrySelector from "./components/FormComposer/CountrySelector";
import FacilitySelector from "./components/FormComposer/FacilitySelector";

export const ORGReducers = getRootReducer;

const ORGModule = ({ stateCode, userType, tenants }) => {
  const moduleCode = "ORG";
  const language = Digit.StoreData.getCurrentLanguage();
  const { isLoading, data: store } = Digit.Services.useStore({ stateCode, moduleCode, language });
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles || [];
  const currentUserRoleCodes = currentUserRoles.map((role) => role?.code);
  // Matched on code, not on name: two MDMS roles are both named "Vendor Administrator" -
  // VENDOR_ADMIN, which is current, and AMC_VENDOR_ADMIN, which is not. Matching the name
  // admitted the dead one as well.
  // VENDOR_ADMIN or ORG_ADMIN - one shared check, see utilities/orgAdminRoles.js.
  const isSingleOrgAdminUser = isSingleOrgAdmin(currentUserRoles);

  if (!currentUserRoleCodes?.includes(ORG_PLATFORM_ADMIN) && !isSingleOrgAdminUser) {
    return null;
  }

  if (isLoading) {
    return <Loader />;
  }
  return <EmployeeApp />;
};

const componentsToRegister = {
  ORGModule,
  ORGCard,
  ORGCountrySelector: CountrySelector,
  ORGStateSelector: StateSelector,
  ORGDistrictSelector: DistrictSelector,
  ORGBlockSelector: BlockSelector,
  ORGFacilitySelector: FacilitySelector,
};

export const initORGComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
