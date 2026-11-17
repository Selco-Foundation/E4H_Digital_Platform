import { useTranslation } from "react-i18next";
import DEFAULT_LABELS from "../components/MobileChangeRequest/defaultLabels";

/**
 * Returns a translate function for this feature's labels.
 * Uses the localisation service's text when the key exists; otherwise the English default.
 * (i18next returns the key itself for a missing key, which is what we detect.)
 */
export const useLabel = () => {
  const { t } = useTranslation();
  return (key) => {
    const translated = t(key, { defaultValue: DEFAULT_LABELS[key] });
    return translated && translated !== key ? translated : DEFAULT_LABELS[key] || key;
  };
};
