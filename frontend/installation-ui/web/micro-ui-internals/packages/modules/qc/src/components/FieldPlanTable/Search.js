import React from "react";
import { TextInput, Label, SubmitBar } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const SearchCentre = ({ centreName, setCentreName, onSubmit, onClear }) => {
  const { t } = useTranslation();

  return (
    <React.Fragment>
      <div style={{ width: "100%", background: "white", height: "fit-content", marginBottom: "15px", padding: "10px" }}>
        <div>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span>
              <Label>Search Field Plan Code</Label>
              <TextInput
                name="serviceRequestId"
                value={centreName}
                onChange={(e) => {
                  setCentreName(e.target.value);
                }}
                style={{ width: "300px" }}
              ></TextInput>
            </span>
            <div style={{ display: "flex", gap: "30px" }}>
              <span
                onClick={onClear}
                className="clear-search"
                style={{ color: "#7a2829", marginLeft: "15px", marginTop: "10px" }}
              >
                Clear Search
              </span>
              <SubmitBar onSubmit={onSubmit} style={{ marginLeft: "10px" }} label={"Search"} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
    // </form>
  );
};

export default SearchCentre;
