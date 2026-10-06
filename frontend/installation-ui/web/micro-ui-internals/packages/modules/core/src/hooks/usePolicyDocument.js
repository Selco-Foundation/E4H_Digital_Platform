const POLICY_MASTER_NAMES = {
  privacy: "PrivacyPolicy",
  terms: "TermsOfUse",
};

const usePolicyDocument = ({ type = "privacy", module = "HCM", moduleName, tenantId } = {}) => {
  const masterName = POLICY_MASTER_NAMES[type] || POLICY_MASTER_NAMES.privacy;
  const selectedModuleName = moduleName || Digit.Utils.getConfigModuleName();
  const selectedTenantId = tenantId || Digit.ULBService.getCurrentTenantId() || Digit.ULBService.getStateId();

  return Digit.Hooks.useCustomMDMS(selectedTenantId, selectedModuleName, [{ name: masterName }], {
    select: (data) => {
      const policies = data?.[selectedModuleName]?.[masterName] || [];
      return policies.find((policy) => policy.module === module) || policies.find((policy) => policy.active) || policies[0];
    },
  });
};

export default usePolicyDocument;
