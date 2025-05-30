import React, { useEffect, useMemo, useState } from "react";
import { Dropdown, RadioButtons, ActionBar, RemoveableTag, RoundedLabel, FilterIcon, CheckBox } from "@egovernments/digit-ui-react-components";
import { ApplyFilterBar, CloseSvg } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
// import Status from "./Status";

const Filter = (props) => {
  const { data, onFilter, onFilterClear, filter, setFilter } = props;

  const districts = data.map((row) => row.district);

  let uniqueDistrictsOptions = [...new Set(districts)].map((district) => {
    return { name: district };
  });

  const blocks = data.map((row) => row.block);

  let uniqueBlocksOptions = [...new Set(blocks)].map((block) => {
    return { name: block };
  });

  return (
    <React.Fragment>
      <div className="filter">
        <div className="filter-card" style={{ padding: "10px" }}>
          <div>
            <div
              style={{
                fontFamily: "Roboto",
                fontWeight: 700,
                fontSize: "24px",
                lineHeight: "114%",
                letterSpacing: "0px",
                color: "#0B0C0C",
                display: "flex",
                gap: "15px",
                alignItems: "center",
                marginBottom: "20px",
              }}
            >
              <FilterIcon />
              Filter
            </div>
            <div style={{ marginBottom: "35px" }}>
              <div className="filter-label" style={{ marginBottom: "10px" }}>
                District
              </div>
              <Dropdown
                option={uniqueDistrictsOptions}
                selected={filter.district === null ? { name: "District" } : { name: filter.district }}
                select={(value) => {
                  setFilter({
                    district: value.name,
                    block: filter.block,
                    status: filter.status,
                  });
                }}
                optionKey={"name"}
              />
            </div>
            <div style={{ marginBottom: "65px" }}>
              <div className="filter-label" style={{ marginBottom: "10px" }}>
                Block
              </div>
              <Dropdown
                option={uniqueBlocksOptions}
                selected={filter.block === null ? { name: "Block" } : { name: filter.block }}
                select={(value) => {
                  setFilter({
                    district: filter.district,
                    block: value.name,
                    status: filter.status,
                  });
                }}
                optionKey={"name"}
              />
            </div>
            {props.installationsWithCount.map((option, index) => {
              return (
                <div style={{ marginTop: "-35px" }}>
                  <CheckBox
                    key={index}
                    onChange={(e) => {
                      setFilter({
                        district: filter.district,
                        block: filter.block,
                        status: filter.status.includes(e.target.value)
                          ? filter.status.filter((status) => status !== e.target.value)
                          : [...filter.status, e.target.value],
                      });
                    }}
                    defaultValue={false}
                    label={`${option.name}`}
                  />
                </div>
              );
            })}
            <div style={{ marginTop: "15px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div
                style={{
                  fontFamily: "Roboto",
                  fontWeight: 400,
                  fontSize: "16px",
                  lineHeight: "24px",
                  letterSpacing: "0px",
                  textTransform: "capitalize",
                  color: "#C84C0E",
                  cursor: "pointer",
                }}
                onClick={onFilterClear}
              >
                Clear
              </div>

              <div
                style={{
                  width: 116,
                  height: 32,
                  display: "flex",
                  gap: "8px",
                  justifyContent: "center",
                  alignItems: "center",
                  paddingTop: "8px",
                  paddingRight: "20px",
                  paddingBottom: "8px",
                  paddingLeft: "20px",
                  background: "#C84C0E",
                  color: "white",
                  cursor: "pointer",
                  fontFamily: "Roboto",
                  fontWeight: 500,
                  fontSize: "19px",
                  lineHeight: "24px",
                  letterSpacing: "0px",
                  textAlign: "center",
                  textTransform: "capitalize",
                  boxShadow: "0px -2px 0px 0px #0B0C0C inset",
                }}
                onClick={onFilter}
              >
                Apply
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Filter;
