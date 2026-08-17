const SYSTEM_TYPE_ALIASES = {
  DC_OFF_GRID: "DC",
};

const normalizeCode = (value) => {
  const normalizedValue = value?.toString().trim().toUpperCase();
  return SYSTEM_TYPE_ALIASES[normalizedValue] || normalizedValue;
};

const getSystemTypeConfig = (criterion = {}, systemType) => {
  const selectedSystemType = normalizeCode(systemType);
  if (!selectedSystemType) return null;

  return criterion.system_types?.find((systemTypeConfig) => (
    normalizeCode(systemTypeConfig?.code) === selectedSystemType
  ));
};

export const getInstallationImageCriteriaBySystemType = (criteria = [], systemType) => (
  criteria
    .map((criterion) => {
      const systemTypeConfig = getSystemTypeConfig(criterion, systemType);
      if (!systemTypeConfig) return null;

      return {
        ...criterion,
        order: Number(systemTypeConfig.order),
      };
    })
    .filter(Boolean)
    .sort((left, right) => left.order - right.order)
);
