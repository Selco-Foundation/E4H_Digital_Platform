import { useQuery } from "react-query";
import i18next from "i18next";

const POLICY_MASTER_NAMES = {
  privacy: "PrivacyPolicy",
  terms: "TermsOfUse",
};
const POLICY_MDMS_URL = "/egov-mdms-service/v1/_search";
const POLICY_LOCALIZATION_URL = "/localization/messages/v1/_search";
const POLICY_LOCALIZATION_MODULE = "rainmaker-common";

const unique = (items = []) => items.filter((item, index) => item && items.indexOf(item) === index);

const collectPolicyArrays = (source, masterName, results = []) => {
  if (!source || typeof source !== "object") {
    return results;
  }

  Object.keys(source).forEach((key) => {
    const value = source[key];
    if (key === masterName && Array.isArray(value)) {
      results.push(value);
    } else if (value && typeof value === "object") {
      collectPolicyArrays(value, masterName, results);
    }
  });

  return results;
};

const hasContents = (policy) => Array.isArray(policy && policy.contents) && policy.contents.length > 0;

const selectPolicy = (policies = [], module) => {
  return (
    policies.find((policy) => policy.module === module && hasContents(policy)) ||
    policies.find((policy) => policy.active && hasContents(policy)) ||
    policies.find(hasContents) ||
    policies.find((policy) => policy.module === module) ||
    policies.find((policy) => policy.active) ||
    policies[0]
  );
};

const getPolicyFromResponse = (data, moduleName, masterName, module) => {
  const mdmsRes = data && (data.MdmsRes || data);
  const moduleData = mdmsRes && mdmsRes[moduleName];
  const directPolicies = (moduleData && moduleData[masterName]) || [];
  const allPolicies = directPolicies.length ? directPolicies : collectPolicyArrays(mdmsRes, masterName).flat();
  return selectPolicy(allPolicies, module);
};

const getRequestInfo = () => {
  const userInfo = Digit.UserService.getUser();
  return {
    apiId: "Rainmaker",
    ver: ".01",
    ts: "",
    action: "_search",
    did: "1",
    key: "",
    msgId: "20170310130900|en_IN",
    authToken: userInfo && userInfo.access_token,
  };
};

const getSelectedLocale = () => {
  const selectedLocale =
    Digit.StoreData.getCurrentLanguage() ||
    Digit.SessionStorage.get("locale") ||
    localStorage.getItem("Employee.locale") ||
    localStorage.getItem("Citizen.locale") ||
    Digit.Utils.getDefaultLanguage();

  return selectedLocale && selectedLocale.indexOf(Digit.Utils.getLocaleRegion()) === -1
    ? `${selectedLocale}${Digit.Utils.getLocaleRegion()}`
    : selectedLocale;
};

const addLocalizationMessages = (locale, messages = []) => {
  const translations = messages.reduce((result, item) => {
    if (item && item.code) {
      result[item.code] = item.message;
    }
    return result;
  }, {});

  i18next.addResources(locale, "translations", translations);
};

const fetchPolicyLocalization = async (locale, tenantId) => {
  const response = await fetch(
    `${POLICY_LOCALIZATION_URL}?module=${POLICY_LOCALIZATION_MODULE}&locale=${locale}&tenantId=${tenantId}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        RequestInfo: getRequestInfo(),
      }),
    }
  );

  if (!response.ok) {
    return;
  }

  const data = await response.json();
  addLocalizationMessages(locale, data && data.messages);
};

const fetchPolicyDocument = async (tenantId, moduleName, masterName) => {
  const response = await fetch(`${POLICY_MDMS_URL}?tenantId=${tenantId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      RequestInfo: getRequestInfo(),
      MdmsCriteria: {
        tenantId,
        moduleDetails: [
          {
            moduleName,
            masterDetails: [{ name: masterName }],
          },
        ],
      },
    }),
  });

  if (!response.ok) {
    throw new Error("Policy MDMS request failed");
  }

  return response.json();
};

const usePolicyDocument = ({ type = "privacy", module = "HCM", moduleName = "commonUiConfig", tenantId } = {}) => {
  const masterName = POLICY_MASTER_NAMES[type] || POLICY_MASTER_NAMES.privacy;
  const selectedTenantId = tenantId || Digit.ULBService.getCurrentTenantId() || Digit.ULBService.getStateId() || "in";
  const tenantCandidates = unique([
    "in",
    selectedTenantId,
    Digit.ULBService.getCurrentTenantId(),
    Digit.ULBService.getStateId(),
    window.globalConfigs && window.globalConfigs.getConfig && window.globalConfigs.getConfig("STATE_LEVEL_TENANT_ID"),
  ]);
  const moduleCandidates = unique([moduleName, Digit.Utils.getConfigModuleName && Digit.Utils.getConfigModuleName(), "commonUiConfig"]);

  return useQuery(["policy-document", tenantCandidates, moduleCandidates, masterName, module], async () => {
    let fallbackPolicy;
    const locale = getSelectedLocale();

    for (const tenant of tenantCandidates) {
      for (const configModuleName of moduleCandidates) {
        try {
          const response = await fetchPolicyDocument(tenant, configModuleName, masterName);
          const policy = getPolicyFromResponse(response, configModuleName, masterName, module);
          if (hasContents(policy)) {
            try {
              await fetchPolicyLocalization(locale, tenant);
            } catch (error) {}
            return policy;
          }
          fallbackPolicy = fallbackPolicy || policy;
        } catch (error) {
          fallbackPolicy = fallbackPolicy || null;
        }
      }
    }

    return fallbackPolicy;
  });
};

export default usePolicyDocument;
