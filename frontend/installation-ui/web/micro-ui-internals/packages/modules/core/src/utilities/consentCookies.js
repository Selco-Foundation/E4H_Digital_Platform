export const CONSENT_COOKIE_KEYS = {
  privacy: "e4h_privacy_policy_accepted",
  terms: "e4h_terms_of_use_accepted",
};

const CONSENT_COOKIE_MAX_AGE = 60 * 60 * 24 * 365;

export const getConsentCookie = (key) => {
  if (typeof document === "undefined") {
    return false;
  }

  return document.cookie
    .split(";")
    .map((cookie) => cookie.trim())
    .some((cookie) => cookie === `${key}=true`);
};

export const setConsentCookie = (key) => {
  if (typeof document === "undefined") {
    return;
  }

  const secureAttribute = window.location.protocol === "https:" ? "; Secure" : "";
  document.cookie = `${key}=true; Max-Age=${CONSENT_COOKIE_MAX_AGE}; Path=/; SameSite=Lax${secureAttribute}`;
};

export const rememberRequiredConsents = () => {
  setConsentCookie(CONSENT_COOKIE_KEYS.privacy);
  setConsentCookie(CONSENT_COOKIE_KEYS.terms);
};
