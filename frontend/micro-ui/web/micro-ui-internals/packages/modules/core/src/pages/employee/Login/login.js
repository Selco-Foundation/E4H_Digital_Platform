import { BackButton, Dropdown, FormComposer, Loader, Toast } from "@selco/digit-ui-react-components";
import PropTypes from "prop-types";
import React, { useEffect, useState } from "react";
import { useHistory } from "react-router-dom";
import Background from "../../../components/Background";
import Header from "../../../components/Header";
import ForgotPassword from "../ForgotPasswordPopup/ForgotPassword";

/* set employee details to enable backward compatiable */
const setEmployeeDetail = (userObject, token) => {
  //console.log("userObject1", userObject)
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

const Login = ({ config: propsConfig, t, isDisabled }) => {
  const { data: cities, isLoading } = Digit.Hooks.useTenants();
  let sortedCities = [];
  if (cities !== null && cities !== undefined) {
    sortedCities = cities.sort((a, b) => a.i18nKey.localeCompare(b.i18nKey));
  }
  const { data: storeData, isLoading: isStoreLoading } = Digit.Hooks.useStore.getInitData();
  // console.log("storeData", storeData)
  const { stateInfo } = storeData || {};
  const [user, setUser] = useState(null);
  const [showToast, setShowToast] = useState(null);
  const [popup, setPopup] = useState(false);
  const [disable, setDisable] = useState(false);

  const history = useHistory();
  // const getUserType = () => "EMPLOYEE" || Digit.UserService.getType();
  const isMobile = window.Digit.Utils.browser.isMobile();
  useEffect(() => {
    if (!user) {
      return;
    }
    Digit.SessionStorage.set("citizen.userRequestObject", user);
    const filteredRoles = user?.info?.roles?.filter((role) => role.tenantId === Digit.SessionStorage.get("Employee.tenantId"));
    if (user?.info?.roles?.length > 0) user.info.roles = filteredRoles;
    Digit.UserService.setUser(user);
    setEmployeeDetail(user?.info, user?.access_token);
    let redirectPath = "/digit-ui/employee";

    /* logic to redirect back to same screen where we left off  */
    if (window?.location?.href?.includes("from=")) {
      redirectPath = decodeURIComponent(window?.location?.href?.split("from=")?.[1]) || "/digit-ui/employee";
    }

    /*  RAIN-6489 Logic to navigate to National DSS home incase user has only one role [NATADMIN]*/
    if (user?.info?.roles && user?.info?.roles?.length > 0 && user?.info?.roles?.every((e) => e.code === "NATADMIN")) {
      redirectPath = "/digit-ui/employee/dss/landing/NURT_DASHBOARD";
    }
    /*  RAIN-6489 Logic to navigate to National DSS home incase user has only one role [NATADMIN]*/
    if (user?.info?.roles && user?.info?.roles?.length > 0 && user?.info?.roles?.every((e) => e.code === "STADMIN")) {
      redirectPath = "/digit-ui/employee/dss/landing/home";
    }

    history.replace(redirectPath);
  }, [user]);

  const onLogin = async (data) => {
    if (!data.city) {
      alert(t("ES_SELECT_HEALTH_CARE"));
      return;
    }
    setDisable(true);

    const requestData = {
      ...data,
      userType: "EMPLOYEE",
    };
    requestData.tenantId = data.city.code;
    delete requestData.city;
    try {
      const { UserRequest: info, ...tokens } = await Digit.UserService.authenticate(requestData);
      Digit.SessionStorage.set("Employee.tenantId", info?.tenantId);
      setUser({ info, ...tokens });
    } catch (err) {
      setShowToast(err?.response?.data?.error_description || "Invalid login credentials!");
      setTimeout(closeToast, 5000);
    }
    setDisable(false);
  };

  const closeToast = () => {
    setShowToast(null);
  };

  const onForgotPassword = () => {
    sessionStorage.getItem("User") && sessionStorage.removeItem("User");
    history.push("/digit-ui/employee/user/forgot-password");
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
          },
          isMandatory: true,
        },
        {
          label: t(password.label),
          type: password.type,
          populators: {
            name: password.name,
          },
          isMandatory: true,
        },
        {
          label: t(city.label),
          type: city.type,
          populators: {
            name: city.name,
            customProps: {},
            component: (props, customProps) => (
              <Dropdown
                option={sortedCities}
                className="login-city-dd"
                optionKey="i18nKey"
                select={(d) => {
                  props.onChange(d);
                }}
                t={t}
                {...customProps}
              />
            ),
          },
          isMandatory: true,
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
          isDisabled={isDisabled || disable}
          noBoxShadow
          inline
          submitInForm
          config={config}
          label={propsConfig.texts.submitButtonLabel}
          secondaryActionLabel={propsConfig.texts.secondaryButtonLabel}
          onSecondayActionClick={onForgotPassword}
          heading={propsConfig.texts.header}
          headingStyle={{ textAlign: "center" }}
          cardStyle={isMobile ? { margin: "auto", minWidth: "300px" } : { margin: "auto", minWidth: "400px" }}
          className="loginFormStyleEmployee"
          buttonStyle={{ maxWidth: "100%", width: "100%", backgroundColor: "#7a2829" }}
        >
          <Header />
        </FormComposer>
        <div style={{ textAlign: "center", marginTop: "1rem" }}>
          <button
            onClick={() => setPopup(true)}
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
          <img
            className="bannerLogo"
            src={"https://selco-assets.s3.ap-south-1.amazonaws.com/powered-by-nhm-ka.png"}
            alt="Selco Foundation"
            style={{ border: "0px", marginLeft: "15px" }}
          />
          <img
            className="bannerLogo"
            src={"https://selco-assets.s3.ap-south-1.amazonaws.com/powered-by-ka_govt.svg"}
            alt="Selco Foundation"
            style={{ border: "0px" }}
          />
          <img
            className="bannerLogo"
            src={"https://selco-assets.s3.ap-south-1.amazonaws.com/logo.png"}
            alt="Selco Foundation"
            style={{ border: "0px" }}
          />
        </div>
      </div>
      {showToast && <Toast error={true} label={t(showToast)} onClose={closeToast} />}
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
