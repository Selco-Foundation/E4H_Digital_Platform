/**
 * The above code initializes various Digit UI modules and components, sets up customizations, and
 * renders the DigitUI component based on the enabled modules and state code.
 * @returns The `App` component is being returned, which renders the `DigitUI` component with the
 * specified props such as `stateCode`, `enabledModules`, `moduleReducers`, and `defaultLanding`. The
 * `DigitUI` component is responsible for rendering the UI based on the provided configuration and
 * modules.
 */
import React, { Suspense } from "react";
import { initLibraries } from "@egovernments/digit-ui-libraries";
import { UICustomizations } from "./Customisations/UICustomizations";
import { initUtilitiesComponents } from "@egovernments/digit-ui-module-utilities";
import { Loader } from "@egovernments/digit-ui-components";
import { QCReducers, initQCComponents } from "@selco/digit-ui-module-qc";
import { PMReducers, initPMComponents } from "@selco/digit-ui-module-pm";
import { AMCReducers, initAMCComponents } from "@selco/digit-ui-module-amc";
import { FAReducers, initFAComponents } from "@selco/digit-ui-module-fa";
import { ORGReducers, initORGComponents } from "@selco/digit-ui-module-org";

window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");

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
  "FA",
  "ORG",
  "Utilities",
];

initLibraries().then(() => {
  initDigitUI();
});

const moduleReducers = (initData) => ({
  qc: QCReducers(initData),
  pm: PMReducers(initData),
  amc: AMCReducers(initData),
  fa: FAReducers(initData),
  org: ORGReducers(initData),
});

const initDigitUI = () => {
  window.Digit.ComponentRegistryService.setupRegistry({});
  window.Digit.Customizations = {
    QC: {},
    commonUiConfig: UICustomizations,
  };

  initUtilitiesComponents();
  initQCComponents();
  initPMComponents();
  initAMCComponents();
  initFAComponents();
  initORGComponents();
};

function App() {
  window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");
  const stateCode =
    window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") ||
    process.env.REACT_APP_STATE_LEVEL_TENANT_ID;
  if (!stateCode) {
    return <h1>stateCode is not defined</h1>;
  }
  return (
    <Suspense fallback={<Loader page={true} variant={"PageLoader"} />}>
      <DigitUI
        stateCode={stateCode}
        enabledModules={enabledModules}
        moduleReducers={moduleReducers}
        defaultLanding="employee"
        allowedUserTypes={["employee"]}
      />
    </Suspense>
  );
}

export default App;