import React from "react";
import { initLibraries } from "@egovernments/digit-ui-libraries";
import { DigitUI,initCoreComponents } from "@egovernments/digit-ui-module-core";
import { initUtilitiesComponents } from "@egovernments/digit-ui-module-utilities";
import { UICustomizations } from "./Customisations/UICustomizations";
import { QCReducers, initQCComponents } from "@selco/digit-ui-module-qc";

window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");

const enabledModules = [
  "QC",
  "Utilities",
];

const moduleReducers = (initData) => ({
  initData, qc: QCReducers(initData),
});

const initDigitUI = () => {
  window.Digit.ComponentRegistryService.setupRegistry({
    PaymentModule,
    ...paymentConfigs,
    PaymentLinks,
  });
  initCoreComponents();
  initUtilitiesComponents();
  initQCComponents();

  window.Digit.Customizations = {
    commonUiConfig: UICustomizations,
  };
};

initLibraries().then(() => {
  initDigitUI();
});

function App() {
  window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");
  const stateCode =
    window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") ||
    process.env.REACT_APP_STATE_LEVEL_TENANT_ID;
  if (!stateCode) {
    return <h1>stateCode is not defined</h1>;
  }
  return (
    <DigitUI
      stateCode={stateCode}
      enabledModules={enabledModules}
      moduleReducers={moduleReducers}
      defaultLanding="employee"
    />
  );
}

export default App;