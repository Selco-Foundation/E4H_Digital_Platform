export const CONSENT_COOKIE_KEYS = {
  privacy: "e4h_privacy_policy_accepted",
  terms: "e4h_terms_of_use_accepted",
} as const;

const CONSENT_COOKIE_MAX_AGE = 60 * 60 * 24 * 365;

export function getConsentCookie(key: string): boolean {
  if (typeof document === "undefined") {
    return false;
  }

  return document.cookie
    .split(";")
    .map((cookie) => cookie.trim())
    .some((cookie) => cookie === `${key}=true`);
}

export function setConsentCookie(key: string): void {
  if (typeof document === "undefined") {
    return;
  }

  const secureAttribute = typeof window !== "undefined" && window.location.protocol === "https:" ? "; Secure" : "";
  document.cookie = `${key}=true; Max-Age=${CONSENT_COOKIE_MAX_AGE}; Path=/; SameSite=Lax${secureAttribute}`;
}

export function rememberRequiredConsents(): void {
  setConsentCookie(CONSENT_COOKIE_KEYS.privacy);
  setConsentCookie(CONSENT_COOKIE_KEYS.terms);
}

export function hasAcceptedRequiredConsents(): boolean {
  return getConsentCookie(CONSENT_COOKIE_KEYS.privacy) && getConsentCookie(CONSENT_COOKIE_KEYS.terms);
}
