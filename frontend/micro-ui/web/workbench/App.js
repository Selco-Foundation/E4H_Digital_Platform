import React from "react";
import { initLibraries } from "@egovernments/digit-ui-libraries";
import { DigitUI } from "@egovernments/digit-ui-module-core";
// import { initHRMSComponents } from "@egovernments/digit-ui-module-hrms";
import { UICustomizations } from "./Customisations/UICustomizations";
import { initWorkbenchComponents } from "@egovernments/digit-ui-module-workbench";
import { initUtilitiesComponents } from "@egovernments/digit-ui-module-utilities";

window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");

const enabledModules = [
  "DSS",
  "NDSS",
  "Utilities",
  // "HRMS",
  "Engagement",
  "Workbench"
];

const moduleReducers = (initData) => ({
  initData,
});

const initDigitUI = () => {
  window.Digit.ComponentRegistryService.setupRegistry({});
  window.Digit.Customizations = {
    PGR: {},
    commonUiConfig: UICustomizations,
  };
  initUtilitiesComponents();
  initWorkbenchComponents();
};

initLibraries().then(() => {
  initDigitUI();
});

// DIGIT core builds the language dropdown from common-masters.StateInfo[0] (hasLocalisation + languages). In
// Livelihood that master is a list of Indian states, so only English was offered. Read common-masters.Languages
// instead, the same master livelihood-ui uses. Never rejects: on any failure the default dropdown is kept.
const fetchLanguages = () => {
  const tenantId = window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID");
  const mdmsContext = window.globalConfigs?.getConfig("MDMS_CONTEXT_PATH") || "egov-mdms-service";
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 5000);
  return fetch(`/${mdmsContext}/v1/_search`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      RequestInfo: { apiId: "Rainmaker" },
      MdmsCriteria: {
        tenantId,
        moduleDetails: [{ moduleName: "common-masters", masterDetails: [{ name: "Languages" }] }],
      },
    }),
    signal: controller.signal,
  })
    .then((res) => (res.ok ? res.json() : null))
    .then((data) => {
      const languages = (data?.MdmsRes?.["common-masters"]?.Languages || [])
        .filter((language) => language?.code && language?.label)
        .map((language) => ({ label: language.nativeLabel || language.label, value: language.code }));
      return languages.length ? languages : null;
    })
    .catch(() => null)
    .finally(() => clearTimeout(timer));
};

const languagesLoaded = fetchLanguages();

// Fix up what Digit.Hooks.useStore.getInitData() returns. Done once, before DigitUI mounts, and the patched copy is
// memoised so consumers keep a stable reference between renders. Besides the languages, stateInfo.code also comes from
// StateInfo[0] (Lakshadweep), and the dropdown loads the new language's messages for that tenant, so use the state
// tenant instead.
const overrideInitData = (languages, tenantId) => {
  const useStore = window.Digit.Hooks.useStore;
  const getInitData = useStore.getInitData;
  let source;
  let patched;
  useStore.getInitData = () => {
    const result = getInitData();
    if (!result?.data) return result;
    if (result.data !== source) {
      source = result.data;
      patched = {
        ...result.data,
        ...(languages && { languages }),
        ...(tenantId && { stateInfo: { ...result.data.stateInfo, code: tenantId } }),
      };
    }
    return { ...result, data: patched };
  };
};

function App() {
  const [languagesReady, setLanguagesReady] = React.useState(false);
  React.useEffect(() => {
    languagesLoaded.then((languages) => {
      overrideInitData(languages, window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID"));
      setLanguagesReady(true);
    });
  }, []);

  window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");
  const stateCode =
    window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") ||
    process.env.REACT_APP_STATE_LEVEL_TENANT_ID;
  if (!stateCode) {
    return <h1>stateCode is not defined</h1>;
  }
  // DigitUI reads the init data on mount, so wait until the language override is in place
  if (!languagesReady) return null;
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
