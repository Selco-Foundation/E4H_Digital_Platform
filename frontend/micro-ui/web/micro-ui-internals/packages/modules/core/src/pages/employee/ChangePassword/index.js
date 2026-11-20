import React, { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import ChangePasswordComponent from "./changePassword";
import { config } from "./config";
import ForcedChangePassword from "./forcedChangePassword";

const EmployeeChangePassword = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();

  const params = useMemo(() =>
    config.map(
      (step) => {
        const texts = {};
        for (const key in step.texts) {
          texts[key] = t(step.texts[key]);
        }
        return { ...step, texts };
      },
      [config]
    )
  );

  return (
    <Switch>
      <Route path={`${path}`} exact>
        <ChangePasswordComponent config={params[0]} t={t} />
      </Route>
      {/* MH2-10: forced change on first login (logged-in `_update`, not the OTP flow above). */}
      <Route path={`${path}/required`} exact>
        <ForcedChangePassword />
      </Route>
    </Switch>
  );
};

export default EmployeeChangePassword;
