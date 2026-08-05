import { BackButton, Dropdown, FormComposer, Loader, Toast } from "@selco/digit-ui-react-components";
import PropTypes from "prop-types";
import React, { useEffect, useState } from "react";
import { useHistory, useLocation } from "react-router-dom";
import Background from "../../../components/Background";
import Header from "../../../components/Header";
import PolicyConsentModal from "../../../components/PolicyConsentModal";
import { hasAcceptedRequiredConsents, rememberRequiredConsents } from "../../../utilities/consentCookies";
import ForgotPassword from "../ForgotPasswordPopup/ForgotPassword";

const setEmployeeDetail = (userObject, token) => {
  let locale = JSON.parse(sessionStorage.getItem("Digit.locale"))?.value || "en_IN";
  localStorage.setItem("Employee.tenant-id", userObject?.tenantId);
  localStorage.setItem("tenant-id", userObject?.tenantId);
  localStorage.setItem("citizen.userRequestObject", JSON.stringify(userObject));
  localStorage.setItem("locale", locale);
  localStorage.setItem("Employee.locale", locale);
  localStorage.setItem("token", token);
  localStorage.setItem("Employee.token", token);
  localStorage.setItem("user-info", JSON.stringify(userObject));
  localStorage.setItem("Employee.user-info", JSON.stringify(userObject));
};

const ConsentCheckbox = ({ id, checked, onChange, modalType, linkText, translateWithFallback, openPolicyModal }) => (
  <label
    htmlFor={id}
    style={{
      display: "flex",
      alignItems: "center",
      gap: "12px",
      marginBottom: "18px",
      cursor: "pointer",
    }}
  >
    <input
      id={id}
      type="checkbox"
      checked={checked}
      onChange={(event) => onChange(event.target.checked)}
      style={{
        cursor: "pointer",
        height: "18px",
        margin: 0,
        width: "18px",
      }}
    />
    <span>
      {translateWithFallback("CORE_ACCEPT_TEXT", "By clicking, I accept the")}{" "}
      <button
        type="button"
        onClick={(event) => {
          event.preventDefault();
          event.stopPropagation();
          openPolicyModal(modalType);
        }}
        style={{
          background: "transparent",
          border: "none",
          color: "#d4351c",
          cursor: "pointer",
          padding: 0,
          textDecoration: "underline",
        }}
      >
        {translateWithFallback(linkText, linkText === "CORE_PRIVACY_POLICY" ? "Privacy Policy" : "Terms of Use")}
      </button>
    </span>
  </label>
);

const ConsentAcceptance = ({ config }) => {
  const {
    shouldShowConsent,
    privacyAccepted,
    termsAccepted,
    setPrivacyAccepted,
    setTermsAccepted,
    translateWithFallback,
    openPolicyModal,
  } = config?.consentProps || {};

  return shouldShowConsent ? (
    <div style={{ marginBottom: "6px" }}>
      <ConsentCheckbox
        id="privacy-policy-consent"
        checked={privacyAccepted}
        onChange={setPrivacyAccepted}
        modalType="privacy"
        linkText="CORE_PRIVACY_POLICY"
        translateWithFallback={translateWithFallback}
        openPolicyModal={openPolicyModal}
      />
      <ConsentCheckbox
        id="terms-of-use-consent"
        checked={termsAccepted}
        onChange={setTermsAccepted}
        modalType="terms"
        linkText="CORE_TERMS_OF_USE"
        translateWithFallback={translateWithFallback}
        openPolicyModal={openPolicyModal}
      />
    </div>
  ) : null;
};

const Login = ({ config: propsConfig, t, isDisabled }) => {
  const { data: cities, isLoading } = Digit.Hooks.useTenants();
  let sortedCities = [];
  if (cities !== null && cities !== undefined) {
    sortedCities = cities.sort((a, b) => a.i18nKey.localeCompare(b.i18nKey));
  }
  const { data: storeData, isLoading: isStoreLoading } = Digit.Hooks.useStore.getInitData();
  const { stateInfo } = storeData || {};
  const [user, setUser] = useState(null);
  const [showToast, setShowToast] = useState(null);
  const [popup, setPopup] = useState(false);
  const [disable, setDisable] = useState(false);
  const [hasStoredConsent, setHasStoredConsent] = useState(hasAcceptedRequiredConsents());
  const [privacyAccepted, setPrivacyAccepted] = useState(false);
  const [termsAccepted, setTermsAccepted] = useState(false);
  const [activePolicyModal, setActivePolicyModal] = useState(null);

  const history = useHistory();
  const location = useLocation();
  const isMobile = window.Digit.Utils.browser.isMobile();
  const logos = window?.globalConfigs?.getConfig("LOGO_LIST") || [];
  const shouldShowConsent = !hasStoredConsent;
  const isConsentAccepted = privacyAccepted && termsAccepted;
  const translateWithFallback = (key, fallback) => {
    const translated = t(key);
    return translated === key ? fallback : translated;
  };

  const getSelectedLanguage = () => {
    const fromPrelogin = sessionStorage.getItem("prelogin_language");
    if (fromPrelogin) return fromPrelogin;
    const fromStore = window?.Digit?.StoreData?.getCurrentLanguage?.();
    if (fromStore) return fromStore;

    try {
      const raw = sessionStorage.getItem("Digit.locale");
      const parsed = raw ? JSON.parse(raw) : null;
      if (parsed?.value) return parsed.value;
    } catch {}

    return navigator.language || "en_IN";
  };
  useEffect(() => {
    try {
      Digit?.Utils?.analytics?.trackPageView?.("login_page", {
        page_path: window.location?.pathname || "/employee/user/login",
        page_title: "Login",
        selected_language: getSelectedLanguage(),
      });
    } catch (e) {
      console.warn("analytics: page_view login failed", e);
    }
  }, []);

  useEffect(() => {
    if (!user) return;
    let cancelled = false;

    (async () => {
      Digit.SessionStorage.set("citizen.userRequestObject", user);

      const filteredRoles =
        user?.info?.roles?.filter((role) => role.tenantId === Digit.SessionStorage.get("Employee.tenantId")) || [];
      if (user?.info?.roles?.length > 0) user.info.roles = filteredRoles;

      Digit.UserService.setUser(user);
      setEmployeeDetail(user?.info, user?.access_token);

      let redirectPath = `/${window.contextPath}/employee`;

      try {
        await Digit.UserService.userLoginReport({
          User: user.info,
          application: window.contextPath === "installation-qc" ? "MANAGEMENT_HUB" : "SAURA_EMITRA",
        });
      } catch (err) {
        console.error("Login report failed", err);
      }

      const hrmsResponse = await Digit.HRMSService.search("in", null, { codes: user.info.userName });
      const hrmsUser = hrmsResponse?.Employees?.[0];
      if (!hrmsUser) {
        throw new Error("Could not find HRMS employee");
      }
      if (!hrmsUser.jurisdictions || !Array.isArray(hrmsUser.jurisdictions)) {
        throw new Error("Could not find HRMS employee Jurisdictions");
      }
      Digit.SessionStorage.set("HRMS.User", hrmsUser);

      let jurisdictionBoundaries = {};
      for (let jurisdiction of hrmsUser?.jurisdictions) {
        if (jurisdiction?.boundaryType) {
          const key = jurisdiction.boundaryType.toLowerCase();
          jurisdictionBoundaries = {
            ...jurisdictionBoundaries,
            [key]: [...(jurisdictionBoundaries[key] || []), jurisdiction.boundary],
          }
        }
      }

      Digit.SessionStorage.set("Jurisdiction.Boundaries", jurisdictionBoundaries);
      Digit.SessionStorage.set("Jurisdiction.CurrentBoundary", jurisdictionBoundaries);
      localStorage.setItem("Jurisdiction.Boundaries", JSON.stringify(jurisdictionBoundaries));
      localStorage.setItem("Jurisdiction.CurrentBoundary", JSON.stringify(jurisdictionBoundaries));

      const fromParam = new URLSearchParams(location.search).get("from");
      if (fromParam) {
        redirectPath = decodeURIComponent(fromParam) || `/${window.contextPath}/employee`;
      }

      if (user?.info?.roles && user?.info?.roles?.length > 0 && user?.info?.roles?.every((e) => e.code === "NATADMIN")) {
        redirectPath = `/${window.contextPath}/employee/dss/landing/NURT_DASHBOARD`;
      }
      if (user?.info?.roles && user?.info?.roles?.length > 0 && user?.info?.roles?.every((e) => e.code === "STADMIN")) {
        redirectPath = `/${window.contextPath}/employee/dss/landing/home`;
      }

      if (!cancelled) history.replace(redirectPath);
    })().catch((e) => console.error("login effect failed", e));

    return () => {
      cancelled = true;
    };
  }, [user, history, location.search]);

  const onLogin = async (data) => {
    if (shouldShowConsent && !isConsentAccepted) {
      setShowToast("CORE_ACCEPT_PRIVACY_TERMS");
      setTimeout(closeToast, 5000);
      return;
    }

    setDisable(true);
    const requestData = {
      ...data,
      userType: "EMPLOYEE",
      tenantId: "in",
    };
    delete requestData.city;
    try {
      const { UserRequest: info, ...tokens } = await Digit.UserService.authenticate(requestData);
      if (shouldShowConsent) {
        rememberRequiredConsents();
        setHasStoredConsent(true);
      }
      Digit.SessionStorage.set("Employee.tenantId", info?.tenantId);
      setUser({ info, ...tokens });

      try {
        const tenantIdForLabel = info?.tenantId || requestData.tenantId;
        const facilityKey = tenantIdForLabel ? `TENANT_TENANTS_${tenantIdForLabel.replace(".", "_").toUpperCase()}` : null;
        const facilityFromUser = info?.facilityName?.trim?.();
        const facilityTranslated = facilityKey ? t(facilityKey) : "";
        const facility = facilityFromUser || facilityTranslated || "unknown";
        Digit?.Utils?.analytics?.setFacilityName(facility);
        const rolesCsv = (info?.roles || []).map(r => r.code).join(",") || "unknown";
        Digit?.Utils?.analytics?.trackLogin(rolesCsv, getSelectedLanguage());
      } catch (e) {
        console.warn("analytics: user_login failed", e);
      }

      try {
        sessionStorage.removeItem("prelogin_language");
      } catch {}
    } catch (err) {
      setShowToast(err?.response?.data?.error_description || "Invalid login credentials!");
      setTimeout(closeToast, 5000);
    }
    setDisable(false);
  };

  useEffect(() => {
    const queryParams = new URLSearchParams(window.location.search);
    const username = queryParams.get("username");
    const password = queryParams.get("passwd");

    if (username && password) {
      onLogin({
        username,
        password,
      });
    }
  }, []);

  const closeToast = () => {
    setShowToast(null);
  };

  const onForgotPassword = () => {
    sessionStorage.getItem("User") && sessionStorage.removeItem("User");
    history.push(`/${window.contextPath}/employee/user/forgot-password`);
  };

  const handlePolicyModalAccept = () => {
    if (activePolicyModal === "privacy") {
      setPrivacyAccepted(true);
    }
    if (activePolicyModal === "terms") {
      setTermsAccepted(true);
    }
    setActivePolicyModal(null);
  };

  const handlePolicyModalReject = () => {
    if (activePolicyModal === "privacy") {
      setPrivacyAccepted(false);
    }
    if (activePolicyModal === "terms") {
      setTermsAccepted(false);
    }
    setActivePolicyModal(null);
  };

  const [userId, password, city] = propsConfig.inputs;
  const config = [
    {
      body: [
        {
          label: t(userId.label),
          type: userId.type,
          populators: {
            name: userId.name,
            style: {
              marginBottom: "5px"
            },
          },
          isMandatory: true,
        },
        {
          label: t(password.label),
          type: password.type,
          populators: {
            name: password.name,
            style: {
              marginBottom: "25px"
            },
          },
          isMandatory: true,
        },
        {
          type: "component",
          component: ConsentAcceptance,
          withoutLabel: true,
          key: "consentAcceptance",
          consentProps: {
            shouldShowConsent,
            privacyAccepted,
            termsAccepted,
            setPrivacyAccepted,
            setTermsAccepted,
            translateWithFallback,
            openPolicyModal: setActivePolicyModal,
          },
        },
      ],
    },
  ];

  return isLoading || isStoreLoading ? (
    <Loader />
  ) : (
    <Background>
      <div className="employeeBackbuttonAlign">
        <BackButton variant="white" style={{ borderBottom: "none" }} />
      </div>
      <div style={{ backgroundColor: "white" }}>
        <FormComposer
          onSubmit={onLogin}
          isDisabled={isDisabled || disable || (shouldShowConsent && !isConsentAccepted)}
          noBoxShadow
          inline
          submitInForm
          config={config}
          label={propsConfig.texts.submitButtonLabel}
          secondaryActionLabel={propsConfig.texts.secondaryButtonLabel}
          heading={propsConfig.texts.header}
          onSecondayActionClick={onForgotPassword}
          headingStyle={{ textAlign: "center" }}
          cardStyle={isMobile ? { margin: "auto", minWidth: "300px" } : { margin: "auto", minWidth: "400px" }}
          className="loginFormStyleEmployee"
          buttonStyle={{ maxWidth: "100%", width: "100%", backgroundColor: "#7a2829" }}
        >
          <Header />
        </FormComposer>
        <div style={{ textAlign: "center", marginTop: "1rem" }}>
          <button
            onClick={onForgotPassword}
            style={{
              color: "blue",
              textDecoration: "underline",
              cursor: "pointer",
            }}
          >
            {t("CORE_COMMON_FORGOT_PASSWORD")}
          </button>
          {popup && <ForgotPassword setPopup={setPopup} />}
        </div>
        <div style={{ display: "flex", justifyContent: "center", margin: "1rem auto" }}>
          {logos.map((logo, index) => (
            <img
              key={index}
              className="bannerLogo"
              src={logo.url}
              alt={logo.alt}
              style={{
                border: "0px",
                marginRight: "unset",
                paddingRight: "unset",
              }}
            />
          ))}
        </div>
      </div>
      {showToast && <Toast error={true} label={t(showToast)} onClose={closeToast} />}
      {activePolicyModal && (
        <PolicyConsentModal
          type={activePolicyModal}
          module={"E4H"}
          tenantId={Digit.ULBService.getCurrentTenantId() || Digit.ULBService.getStateId()}
          onClose={() => setActivePolicyModal(null)}
          onAccept={handlePolicyModalAccept}
          onReject={handlePolicyModalReject}
        />
      )}
      <div className="employee-login-home-footer" style={{ backgroundColor: "unset" }}>
        <img
          alt="Powered by DIGIT"
          src={window?.globalConfigs?.getConfig?.("DIGIT_FOOTER_BW")}
          style={{ cursor: "pointer" }}
          onClick={() => {
            window.open(window?.globalConfigs?.getConfig?.("DIGIT_HOME_URL"), "_blank").focus();
          }}
        />{" "}
      </div>
    </Background>
  );
};

Login.propTypes = {
  loginParams: PropTypes.any,
};

Login.defaultProps = {
  loginParams: null,
};

export default Login;
