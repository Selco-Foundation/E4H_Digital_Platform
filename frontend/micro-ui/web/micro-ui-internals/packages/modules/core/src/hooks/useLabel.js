import { useTranslation } from "react-i18next";
import DEFAULT_LABELS from "../components/MobileChangeRequest/defaultLabels";

/**
 * Returns a translate function for this feature's labels.
 * Uses the localisation service's text when the key exists; otherwise the English default.
 * (i18next returns the key itself for a missing key, which is what we detect.)
 *
 * `defaults` (optional) lets another feature pass its own English fallbacks; without it the
 * mobile change request labels are used, exactly as before.
 */
export const useLabel = (defaults = DEFAULT_LABELS) => {
  const { t } = useTranslation();
  return (key) => {
    const translated = t(key, { defaultValue: defaults[key] });
    return translated && translated !== key ? translated : defaults[key] || key;
  };
};
