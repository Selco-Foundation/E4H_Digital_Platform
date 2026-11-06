import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardText, TextInput } from "@selco/digit-ui-react-components";
import ReportingSelection from "./ReportingSelection";

const FacilitySelection = ({ facilities, selectedCode, onSelect, showSearch }) => {
  const { t } = useTranslation();
  const [search, setSearch] = useState("");
  const searchTerm = search.trim().toLocaleLowerCase();
  const matches = facilities.filter(({ name }) => name.toLocaleLowerCase().includes(searchTerm));

  return (
    <React.Fragment>
      {showSearch && (
        <div>
          <label htmlFor="report-facility-search">
            {t("IM_REPORT_FACILITY_SEARCH", { defaultValue: "Search facilities" })}
          </label>
          <TextInput id="report-facility-search" name="facilitySearch" type="search" value={search}
            onChange={(event) => setSearch(event.target.value)} />
        </div>
      )}
      <ReportingSelection name="reportFacility" options={matches} optionKey="code" selectedKey={selectedCode} onSelect={onSelect}
        question={t("IM_REPORT_FACILITY_QUESTION", { defaultValue: "Which facility is this for?" })} />
      {!matches.length && <CardText>{t("IM_REPORT_FACILITY_NO_MATCH", { defaultValue: "No facilities match your search." })}</CardText>}
    </React.Fragment>
  );
};

export default FacilitySelection;
