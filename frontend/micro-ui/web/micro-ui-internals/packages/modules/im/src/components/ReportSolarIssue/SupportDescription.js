import React from "react";
import { TextArea } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const SupportDescription = ({ value, onChange, required = true }) => {
  const { t } = useTranslation();
  return <div style={{ margin: "16px 0" }}>
    <label htmlFor="reportSupportDescription">
      {t("IM_REPORT_DESCRIPTION_QUESTION", { defaultValue: "Please explain the issue in your own words" })}
      {required ? " *" : ` (${t("IM_REPORT_OPTIONAL", { defaultValue: "Optional" })})`}
    </label>
    <TextArea id="reportSupportDescription" name="comments" value={value} maxlength={500}
      onChange={(event) => onChange(event.target.value)}
      hintText={t("IM_REPORT_DESCRIPTION_LIMIT", { defaultValue: "{{count}}/500 characters", count: value.length })} />
  </div>;
};

export default SupportDescription;
