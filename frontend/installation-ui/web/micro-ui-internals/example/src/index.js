import React, {Suspense} from "react";
import ReactDOM from "react-dom";
import { initLibraries } from "@egovernments/digit-ui-libraries";
import { initUtilitiesComponents } from "@egovernments/digit-ui-module-utilities";
import { QCReducers, initQCComponents } from "@selco/digit-ui-module-qc";
import { PMReducers, initPMComponents } from "@selco/digit-ui-module-pm";
import { AMCReducers, initAMCComponents } from "@selco/digit-ui-module-amc";
import { ORGReducers, initORGComponents } from "@selco/digit-ui-module-org";
import { Loader } from "@egovernments/digit-ui-components";

import "@egovernments/digit-ui-components-css/dist/index.css";
import "@egovernments/digit-ui-css/dist/index.css";
import "@selco/installation-ui-css/example/index.css";

import { pgrCustomizations, overrideComponents } from "./pgr";
import { UICustomizations } from "./UICustomizations";

var Digit = window.Digit || {};

// Lazy load DigitUI
const DigitUI = React.lazy(() =>
  import("@egovernments/digit-ui-module-core").then((mod) => ({
    default: mod.DigitUI,
  }))
);

const enabledModules = [
  "QC",
  "PM",
  "AMC",
  "ORG",
  "Utilities",
];

const initTokens = (stateCode) => {
  const userType = window.sessionStorage.getItem("userType") || process.env.REACT_APP_USER_TYPE || "CITIZEN";
  const token = window.localStorage.getItem("token") || process.env[`REACT_APP_${userType}_TOKEN`];

  const citizenInfo = window.localStorage.getItem("Citizen.user-info");

  const citizenTenantId = window.localStorage.getItem("Citizen.tenant-id") || stateCode;

  const employeeInfo = window.localStorage.getItem("Employee.user-info");
  const employeeTenantId = window.localStorage.getItem("Employee.tenant-id");

  const userTypeInfo = userType === "CITIZEN" || userType === "QACT" ? "citizen" : "employee";
  window.Digit.SessionStorage.set("user_type", userTypeInfo);
  window.Digit.SessionStorage.set("userType", userTypeInfo);

  if (userType !== "CITIZEN") {
    window.Digit.SessionStorage.set("User", { access_token: token, info: userType !== "CITIZEN" ? JSON.parse(employeeInfo) : citizenInfo });
  } else {
    // if (!window.Digit.SessionStorage.get("User")?.extraRoleInfo) window.Digit.SessionStorage.set("User", { access_token: token, info: citizenInfo });
  }

  window.Digit.SessionStorage.set("Citizen.tenantId", citizenTenantId);

  if (employeeTenantId && employeeTenantId.length) window.Digit.SessionStorage.set("Employee.tenantId", employeeTenantId);
};

const initDigitUI = () => {
  const isMultiRootTenant = window?.globalConfigs?.getConfig("MULTI_ROOT_TENANT") || false;

  if (isMultiRootTenant) {
    const pathname = window.location.pathname;
    const context = window?.globalConfigs?.getConfig("CONTEXT_PATH");
    const start = pathname.indexOf(context) + context.length + 1;
    const employeeIndex = pathname.indexOf("employee");
    const citizenIndex = pathname.indexOf("citizen");
    const end = (employeeIndex !== -1) ? employeeIndex : (citizenIndex !== -1) ? citizenIndex : -1;
    const tenant = end > start ? pathname.substring(start, end).replace(/\/$/, "") : "";
    window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH") + `${tenant ? `/${tenant}` : ""}` || "digit-ui";
    window.globalPath = window?.globalConfigs?.getConfig("CONTEXT_PATH") || "digit-ui";
  } else {
    window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH") || "digit-ui";
  }

  window.Digit.Customizations = {
    QC: {},
    commonUiConfig: UICustomizations,
  };

  window?.Digit.ComponentRegistryService.setupRegistry({
    ...overrideComponents,
  });
  initUtilitiesComponents();
  initQCComponents();
  initPMComponents();
  initAMCComponents();
  initORGComponents();

  const moduleReducers = (initData) => ({
    qc: QCReducers(initData),
    pm: PMReducers(initData),
    amc: AMCReducers(initData),
    org: ORGReducers(initData)
  });

  // const stateCode = window?.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") || "pb";
  const stateCode = Digit.ULBService.getStateId();
  initTokens(stateCode);

  ReactDOM.render(
    <Suspense fallback={<Loader page={true} variant={"PageLoader"} />}>
      <DigitUI
        stateCode={stateCode}
        enabledModules={enabledModules}
        defaultLanding="employee"
        allowedUserTypes={["employee"]}
        moduleReducers={moduleReducers}
      />
    </Suspense>,
    document.getElementById("root")
  );
};

initLibraries().then(() => {
  initDigitUI();
});
