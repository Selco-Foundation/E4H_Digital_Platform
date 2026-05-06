import { CardSubHeader, FormComposer,CardText} from "@egovernments/digit-ui-react-components";
import { BackLink,Toast} from "@egovernments/digit-ui-components";
import PropTypes from "prop-types";
import React, { useEffect, useState } from "react";
import { useHistory } from "react-router-dom";
import Background from "../../../components/Background";
import Header from "../../../components/Header";
import SelectOtp from "../../citizen/Login/SelectOtp";
import ImageComponent from "../../../components/ImageComponent";
import {useLoginConfig} from "../../../hooks/useLoginConfig";

const ChangePasswordComponent = ({ config: propsConfig, t }) => {
  const [user, setUser] = useState(null);
  const { mobile_number: mobileNumber, tenantId } = Digit.Hooks.useQueryParams();
  const history = useHistory();
  const [otp, setOtp] = useState("");
  const [isOtpValid, setIsOtpValid] = useState(true);
  const [showToast, setShowToast] = useState(null);
  const getUserType = () => Digit.UserService.getType();
  const stateCode = window?.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") || "in";

  const { data : mdmsData } = useLoginConfig(stateCode);

  if(mdmsData?.config){
    propsConfig.header = mdmsData?.config[0]?.header;
    propsConfig.bannerImages = mdmsData?.config[0]?.bannerImages;
  }

  useEffect(() => {
    if (!user) {
      Digit.UserService.setType("employee");
      return;
    }
    Digit.UserService.setUser(user);
    const redirectPath = location.state?.from || `/${window?.contextPath}/employee`;
    history.replace(redirectPath);
  }, [user]);

  const closeToast = () => {
    setShowToast(null);
  };

  const onResendOTP = async () => {
    const requestData = {
      otp: {
        mobileNumber,
        userType: getUserType().toUpperCase(),
        type: "passwordreset",
        tenantId,
      },
    };

    try {
      await Digit.UserService.sendOtp(requestData, tenantId);
      setShowToast({
        type: "success",
        label: "ES_OTP_RESEND",
      });
    } catch (err) {
      setShowToast({
        type: "error",
        label: err?.response?.data?.error?.fields?.[0]?.message || "ES_SOMETHING_WRONG",
      });
    }
    setTimeout(closeToast, 5000);
  };

  const onChangePassword = async (data) => {
    try {
      if (data.newPassword !== data.confirmPassword) {
        return setShowToast({
          type: "error",
          label: "CORE_COMMON_PROFILE_PASSWORD_MISMATCH",
        });
      }
      const requestData = {
        ...data,
        otpReference: otp,
        tenantId,
        type: getUserType().toUpperCase(),
      };

      const response = await Digit.UserService.changePassword(requestData, tenantId);
      navigateToLogin();
    } catch (err) {
      setShowToast({
        type: "error",
        label: err?.response?.data?.error?.fields?.[0]?.message || "ES_SOMETHING_WRONG",
      });
      setTimeout(closeToast, 5000);
    }
  };

  const navigateToLogin = () => {
    history.replace(`/${window?.contextPath}/employee/user/login`);
  };

  const [username, password, confirmPassword] = propsConfig.inputs;
  const config = [
    {
      body: [
        {
          label: t(username.label),
          type: username.type,
          populators: {
            name: username.name,
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
          label: t(confirmPassword.label),
          type: confirmPassword.type,
          populators: {
            name: confirmPassword.name,
          },
          isMandatory: true,
        },
      ],
    },
  ];

  return (
    <Background>
      <div className="employeeBackbuttonAlign">
        <BackLink variant="primary" style={{ borderBottom: "none" }} />
      </div>
      <FormComposer
        onSubmit={onChangePassword}
        noBoxShadow
        inline
        submitInForm
        config={config}
        label={propsConfig.texts.submitButtonLabel}
        cardStyle={{ maxWidth: "408px", margin: "auto" }}
        className="employeeChangePassword"
      >
        {propsConfig?.header ? <Header loginHeader={propsConfig?.header} /> : <Header />}
        <CardSubHeader style={{ textAlign: "center" }}> {propsConfig.texts.header} </CardSubHeader>
        <CardText>
          {`${t(`CS_LOGIN_OTP_TEXT`)} `}
          <b>
            {" "}
            {`${t(`+ 91 - `)}`} {mobileNumber}
          </b>
        </CardText>
        <SelectOtp t={t} userType="employee" otp={otp} onOtpChange={setOtp} error={isOtpValid} onResend={onResendOTP} />
        {/* <div>
          <CardLabel style={{ marginBottom: "8px" }}>{t("CORE_OTP_SENT_MESSAGE")}</CardLabel>
          <CardLabelDesc style={{ marginBottom: "0px" }}> {mobileNumber} </CardLabelDesc>
          <CardLabelDesc style={{ marginBottom: "8px" }}> {t("CORE_EMPLOYEE_OTP_CHECK_MESSAGE")}</CardLabelDesc>
        </div>
        <CardLabel style={{ marginBottom: "8px" }}>{t("CORE_OTP_OTP")} *</CardLabel>
        <TextInput className="field" name={otpReference} isRequired={true} onChange={updateOtp} type={"text"} style={{ marginBottom: "10px" }} />
        <div className="flex-right">
          <div className="primary-label-btn" onClick={onResendOTP}>
            {t("CORE_OTP_RESEND")}
          </div>
        </div> */}
      </FormComposer>
      {showToast && <Toast type={showToast?.type} label={t(showToast?.label)} onClose={closeToast} />}
      <div className="EmployeeLoginFooter">
        <ImageComponent
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

ChangePasswordComponent.propTypes = {
  loginParams: PropTypes.any,
};

ChangePasswordComponent.defaultProps = {
  loginParams: null,
};

export default ChangePasswordComponent;
