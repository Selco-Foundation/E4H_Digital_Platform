const POLICY_MASTER_NAMES = {
  privacy: "PrivacyPolicy",
  terms: "TermsOfUse",
};

const usePolicyDocument = ({ type = "privacy", module = "E4H", moduleName = "commonUiConfig", tenantId } = {}) => {
  const masterName = POLICY_MASTER_NAMES[type] || POLICY_MASTER_NAMES.privacy;
  const selectedTenantId = tenantId || Digit.ULBService.getCurrentTenantId() || Digit.ULBService.getStateId();

  return Digit.Hooks.useCustomMDMS(selectedTenantId, moduleName, [{ name: masterName }], {
    select: (data) => {
      const policies = data?.[moduleName]?.[masterName] || [];
      return policies.find((policy) => policy.module === module) || policies.find((policy) => policy.active) || policies[0];
    },
  });
};

export default usePolicyDocument;
