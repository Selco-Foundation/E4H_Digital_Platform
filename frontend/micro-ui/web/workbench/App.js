import React from "react";
import { useTranslation } from "react-i18next";
import { Button, CheckBox, PopUp } from "@egovernments/digit-ui-components";
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

// The sidebar's "Home" is a shared access-control action (ACCESSCONTROL-ACTIONS-TEST id 170) whose navigationURL is
// /digit-ui/employee, the main DIGIT app, so clicking it left the workbench. Point top-level Home actions that belong to
// another app at the workbench's own home instead.
const keepHomeInWorkbench = () => {
  const useAccessControl = window.Digit.Hooks.useAccessControl;
  const home = `/${window.contextPath}/employee`;
  const patchedData = new WeakMap();
  const patch = (data) => ({
    ...data,
    actions: data.actions.map((action) =>
      action.path === "Home" && !action.navigationURL?.startsWith(`/${window.contextPath}/`)
        ? { ...action, navigationURL: home }
        : action
    ),
  });
  function useWorkbenchAccessControl(...args) {
    const result = useAccessControl(...args);
    const data = result?.data;
    if (!data?.actions) return result;
    if (!patchedData.has(data)) patchedData.set(data, patch(data));
    return { ...result, data: patchedData.get(data) };
  }
  window.Digit.Hooks.useAccessControl = useWorkbenchAccessControl;
};

// E4H's login config (commonUiConfig.LoginConfig, shared with the main E4H app) has a "TOUComponent" terms-of-use field.
// E4H's own core registers it, DIGIT's core used here doesn't, and the login page crashed on the missing component.
// Same as DIGIT's PrivacyComponent, but for commonUiConfig.TermsOfUse and the "touCheck" form value.
const policyText = (t, items = []) =>
  items.map((item, index) => (
    <div key={index} style={{ marginBottom: "0.5rem", fontWeight: item?.isBold ? 700 : 400 }}>
      {item?.type === "points" && "• "}
      {item?.type === "step" && `${index + 1}. `}
      {t(item?.text)}
      {item?.subDescriptions?.length > 0 && <div style={{ paddingLeft: "1rem" }}>{policyText(t, item.subDescriptions)}</div>}
    </div>
  ));

function TOUComponent({ onSelect, ...props }) {
  const { t } = useTranslation();
  const tenantId = window.Digit.ULBService.getCurrentTenantId();
  const moduleName = window.Digit.Utils.getConfigModuleName();
  const [checked, setChecked] = React.useState(false);
  const [showPopUp, setShowPopUp] = React.useState(false);
  const { data: terms } = window.Digit.Hooks.useCustomMDMS(tenantId, moduleName, [{ name: "TermsOfUse" }], {
    select: (data) => {
      const docs = data?.[moduleName]?.TermsOfUse || [];
      return docs.find((doc) => doc.module === props?.props?.module) || docs[0];
    },
  });
  React.useEffect(() => {
    onSelect("touCheck", checked);
  }, [checked]);
  const decide = (accepted) => () => {
    setChecked(accepted);
    setShowPopUp(false);
  };
  return (
    <React.Fragment>
      <div className="digit-privacy-checkbox digit-privacy-checkbox-align">
        <CheckBox label={t("ES_BY_CLICKING")} checked={checked} onChange={(e) => setChecked(e.target.checked)} id="tou-component-check" />
        <Button label={t("ES_TERMS_OF_USE")} variation="link" size="small" onClick={() => setShowPopUp(true)} style={{ marginBottom: "1rem", paddingLeft: "0.2rem" }} />
      </div>
      {showPopUp && (
        <PopUp
          type="default"
          className="privacy-popUpClass"
          footerclassName="popUpFooter"
          heading={t(terms?.header)}
          onOverlayClick={() => setShowPopUp(false)}
          onClose={() => setShowPopUp(false)}
          sortFooterChildren={true}
          footerChildren={[
            <Button key="reject" type="button" size="large" variation="secondary" label={t("DIGIT_I_DO_NOT_ACCEPT")} onClick={decide(false)} />,
            <Button key="accept" type="button" size="large" variation="primary" label={t("DIGIT_I_ACCEPT")} className="accept-class" onClick={decide(true)} />,
          ]}
        >
          {terms?.contents?.map((section, index) => (
            <div key={index}>
              <div style={{ fontWeight: "bold" }}>{t(section?.header)}</div>
              {policyText(t, section?.descriptions)}
            </div>
          ))}
        </PopUp>
      )}
    </React.Fragment>
  );
}

const initDigitUI = () => {
  keepHomeInWorkbench();
  window.Digit.ComponentRegistryService.setupRegistry({});
  if (!window.Digit.ComponentRegistryService.getComponent("TOUComponent")) {
    window.Digit.ComponentRegistryService.setComponent("TOUComponent", TOUComponent);
  }
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
// the Selco tenants that master is a list of Indian states, so only English was offered. Read common-masters.Languages
// instead. Never rejects: on any failure the default dropdown is kept.
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
// tenant instead. logoUrl is the login page's header logo; the StateInfo rows have none, so take it from the global
// config's LOGO_COLORED.
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
        ...(tenantId && {
          stateInfo: { ...result.data.stateInfo, code: tenantId, logoUrl: window.globalConfigs?.getConfig("LOGO_COLORED") },
        }),
      };
    }
    return { ...result, data: patched };
  };
};

// The header logo after login is the current tenant's logoId (MDMS tenant.tenants), and DIGIT shows its eGov logo when
// that is empty. The Selco state tenant has none and the schema only allows null there, so replace DIGIT's fallback image
// with LOGO_COLORED from the global config whenever the header renders it.
const DIGIT_FALLBACK_LOGO = "egov-logo-2025.png";
const replaceFallbackHeaderLogo = () => {
  const logo = window.globalConfigs?.getConfig("LOGO_COLORED");
  if (!logo) return;
  const replace = () =>
    document.querySelectorAll(".digit-header-logo-img").forEach((img) => {
      if (img.getAttribute("src")?.endsWith(DIGIT_FALLBACK_LOGO)) img.setAttribute("src", logo);
    });
  new MutationObserver(replace).observe(document.body, { childList: true, subtree: true, attributeFilter: ["src"] });
};

replaceFallbackHeaderLogo();

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
