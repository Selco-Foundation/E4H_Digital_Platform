const normalizeCode = (value) => value?.toString().trim().toUpperCase();

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
