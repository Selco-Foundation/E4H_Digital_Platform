import React, { useEffect, useMemo, useState } from "react";
import { Dropdown, RadioButtons, ActionBar, RemoveableTag, RoundedLabel, FilterIcon, CheckBox } from "@egovernments/digit-ui-react-components";
import { ApplyFilterBar, CloseSvg } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
// import Status from "./Status";

const Filter = ({ onFilterApply, filter, statusesWithCount }) => {

  const { t } = useTranslation();
  const [districtMenu, setDistrictMenu] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [currentFilter, setCurrentFilter] = useState({ district: [], block: [], status: [] });

  const districts = [
    { name: "Greenfield", code: "D001" },
    { name: "Riverdale", code: "D002" },
    { name: "Hilltown", code: "D003" },
    { name: "Lakeside", code: "D004" },
    { name: "Sunset Valley", code: "D005" },
  ];

  const blocks = [
    // Greenfield (D001)
    { name: "Central Block", code: "B001", districtCode: "D001" },
    { name: "North Block", code: "B002", districtCode: "D001" },
    { name: "East Block", code: "B003", districtCode: "D001" },

    // Riverdale (D002)
    { name: "West Block", code: "B004", districtCode: "D002" },
    { name: "South Block", code: "B005", districtCode: "D002" },
    { name: "Riverbank Block", code: "B006", districtCode: "D002" },

    // Hilltown (D003)
    { name: "Hilltop Block", code: "B007", districtCode: "D003" },
    { name: "Valley Block", code: "B008", districtCode: "D003" },
    { name: "Pinewood Block", code: "B009", districtCode: "D003" },

    // Lakeside (D004)
    { name: "Lakeshore Block", code: "B010", districtCode: "D004" },
    { name: "Harbor Block", code: "B011", districtCode: "D004" },
    { name: "Island Block", code: "B012", districtCode: "D004" },

    // Sunset Valley (D005)
    { name: "Sunrise Block", code: "B013", districtCode: "D005" },
    { name: "Golden Block", code: "B014", districtCode: "D005" },
    { name: "Twilight Block", code: "B015", districtCode: "D005" },
  ];

  useEffect(() => {
    setDistrictMenu(districts);
  }, []);

  const handleDistrictChange = (value) => {
    if (currentFilter.district.every(district => district.code !== value.code)) {

      const newSelectedDistricts = [...currentFilter.district, value];
      setCurrentFilter({
        ...currentFilter,
        district: newSelectedDistricts
      });

      const selectedDistrictCodes = newSelectedDistricts.map(district => district.code);
      const newBlockMenu = blocks.filter(block => selectedDistrictCodes.includes(block.districtCode));
      setBlockMenu(newBlockMenu);
    }
  }

  const handleBlockChange = (selectedBlock) => {
    if (currentFilter.block.every(block => block.code !== selectedBlock.code)) {
      setCurrentFilter({
        ...currentFilter,
        block: [...currentFilter.block, selectedBlock]
      });
    }
  };

  const onRemove = (index, key) => {
    let afterRemove = currentFilter[key].filter((value, i) => i !== index);

    if (key === "district") {
      const newSelectedDistrictCodes = afterRemove.map(district => district.code);
      const newBlockMenu = blocks.filter(block => newSelectedDistrictCodes.includes(block.districtCode));
      const newSelectedBlocks = currentFilter.block.filter(block => newSelectedDistrictCodes.includes(block.districtCode));

      setBlockMenu(newBlockMenu);
      setCurrentFilter({
        ...currentFilter,
        block: newSelectedBlocks,
        district: afterRemove
      });

    } else {
      setCurrentFilter({ ...currentFilter, [key]: afterRemove });
    }
  };

  const GetSelectOptions = (label, options, selected = null, select, optionKey, onRemove, key) => {
    selected = selected || { [optionKey]: "", code: "" };

    return (
      <div>
        <div className="filter-label">{label}</div>
        {<Dropdown option={options} selected={selected} select={(value) => select(value, key)} optionKey={optionKey} />}

        <div className="tag-container">
          {currentFilter[key].length > 0 &&
            currentFilter[key].map((value, index) => {
              return <RemoveableTag key={index} text={`${value[optionKey]} ...`} onClick={() => onRemove(index, key)} />;
            })}
        </div>
      </div>
    );
  };

  const handleStatusChange = (option, checked) => {
    const statusesChanged = option.code === "PENDING_INSTALLATION" ? ["ASSIGNED_TO_SUPERVISOR", "ASSIGNED_TO_FIELD_STAFF"] : [option.name];
    if (checked) {
      setCurrentFilter({
        ...currentFilter,
        status: [...currentFilter.status, ...statusesChanged]
      });
    } else {
      setCurrentFilter({
        ...currentFilter,
        status: currentFilter.status.filter(status => !statusesChanged.includes(status))
      });
    }
  }

  const handleFilterApply = () => {
    console.debug("handleFilterApply", currentFilter);
    // onFilterApply(currentFilter);
  }

  const onClearAll = () => {
    setCurrentFilter({
      district: [],
      block: [],
      status: [],
    });
    setDistrictMenu(districts);
    setBlockMenu([]);
  }

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
            <div>
              {
                GetSelectOptions(
                  t("CS_DISTRICT"),
                  districtMenu,
                  null,
                  handleDistrictChange,
                  "name",
                  onRemove,
                  "district"
                )
              }
            </div>
            <div>
              {
                GetSelectOptions(
                  t("CS_BLOCK"),
                  blockMenu,
                  null,
                  handleBlockChange,
                  "name",
                  onRemove,
                  "block"
                )
              }
            </div>
            <div
              style={{
                fontFamily: "Roboto",
                fontWeight: 700,
                fontSize: "18px",
                lineHeight: "114%",
                letterSpacing: "0px",
                color: "#0B0C0C",
                alignItems: "center",
                marginBottom: "30px",
              }}
            >
              Status
            </div>
            {statusesWithCount?.map((option, index) => {
              return (
                <div style={{ marginTop: "-35px" }}>
                  <CheckBox
                    key={index}
                    onChange={(e) => {handleStatusChange(option, e.target.checked)}}
                    defaultValue={false}
                    label={`${option.name}${ option.count > 0 ? ` (${option.count})` : ""}`}
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
                onClick={onClearAll}
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
                onClick={handleFilterApply}
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
