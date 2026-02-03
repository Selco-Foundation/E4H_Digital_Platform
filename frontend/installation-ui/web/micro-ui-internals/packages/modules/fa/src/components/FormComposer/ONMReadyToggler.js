import React from 'react';
import { Toggle } from '@egovernments/digit-ui-components'

const OnmReadyToggler = ({ props = {} }) => {

  const { t, isOnmReady, setIsOnmReady } = props;
  const booleanChoiceMenu = [
    { code: "YES", name: "TL_COMMON_YES" },
    { code: "NO", name: "TL_COMMON_NO" },
  ];

  return (
    <div
      style={{
        fontFamily: "Roboto",
        backgroundColor: "#fff",
      }}
    >
      <div
        className={"onm-ready-toggler"}
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "30px",
        }}
      >
        <h2 style={{ margin: 0, fontSize: "32px", fontWeight: "700" }}>
          {t("FACILITY_BULK_ADD_SELECT_ONM_STATE")}
        </h2>
        <Toggle
          numberOfToggleItems={2}
          onSelect={(option) => {
            setIsOnmReady(option === "YES");
          }}
          innerStyles={{width: "100px"}}
          options={booleanChoiceMenu}
          optionsKey="name"
          selectedOption={isOnmReady ? "YES" : "NO"}
          style={{width: "fit-content"}}
          t={t}
          type="toggle"
          value=""
        />
      </div>
      <p
        style={{
          margin: 0,
          fontSize: "14px",
          lineHeight: "1.4",
          color: "#B91900"
        }}
      >
        {`${t("FACILITY_BULK_ADD_SELECT_ONM_STATE_WARNING")} ${isOnmReady ? t("TL_COMMON_YES") : t("TL_COMMON_NO")}`}
      </p>
    </div>
  );
};

export default OnmReadyToggler;