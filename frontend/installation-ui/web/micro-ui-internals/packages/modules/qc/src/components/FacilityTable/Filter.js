import React, { useEffect, useMemo, useState } from "react";
import {
  Dropdown,
  RemoveableTag,
  FilterIcon,
  CheckBox,
  Loader, LinkLabel
} from "@egovernments/digit-ui-react-components";

const Filter = ({ t, fieldPlan, onFilterChange, projectQueryFilter, statusesWithCount }) => {

  const [districtMenu, setDistrictMenu] = useState([]);
  const [blocksList, setBlocksList] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [currentFilter, setCurrentFilter] = useState(projectQueryFilter.facilityFilter || {
    district: [],
    block: [],
    status: []
  });

  const { isLoading, data } = Digit.Hooks.qc.useBoundary(
    fieldPlan?.address?.boundary || "India_Telangana", "State"
  );

  useEffect(() => {
    if (data) {
      setDistrictMenu(data.districts?.map(district => ({...district, name: t(`DISTRICT_${district.code.toUpperCase()}`)})));
      setBlocksList(data.blocks?.map(block => ({...block, name: t(`BLOCK_${block.code.toUpperCase()}`)})));
    }
  }, [data, t]);

  useEffect(() => {
    const facilityFilterQuery = {};

    if (currentFilter.block.length > 0) {
      facilityFilterQuery.boundary = currentFilter.block.map(block => block.code);
    } else if (currentFilter.district.length > 0) {
      const selectedDistrictCodes = currentFilter.district.map(district => district.code);

      facilityFilterQuery.boundary = blocksList
        .filter(block => selectedDistrictCodes.includes(block.districtCode))
        .map(block => block.code);
    }

    if (currentFilter.status.length > 0) {
      facilityFilterQuery.status = currentFilter.status;
    }

    onFilterChange({
      facilityFilter: {
        ...currentFilter
      },
      facilityFilterQuery
    });
  }, [currentFilter, blocksList]);

  useEffect(() => {
    const selectedDistrictCodes = currentFilter.district.map(district => district.code);
    const newBlockMenu = blocksList.filter(block => selectedDistrictCodes.includes(block.districtCode));
    setBlockMenu(newBlockMenu);
  }, [currentFilter, blocksList])

  const handleDistrictChange = (value) => {
    if (currentFilter.district.every(district => district.code !== value.code)) {

      const newSelectedDistricts = [...currentFilter.district, value];
      setCurrentFilter({
        ...currentFilter,
        district: newSelectedDistricts
      });

      const selectedDistrictCodes = newSelectedDistricts.map(district => district.code);
      const newBlockMenu = blocksList.filter(block => selectedDistrictCodes.includes(block.districtCode));
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
      const newBlockMenu = blocksList.filter(block => newSelectedDistrictCodes.includes(block.districtCode));
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

  if (isLoading) {
    return <Loader />
  }

  const GetSelectOptions = (label, options, selected = null, select, optionKey, onRemove, key) => {
    selected = selected || { [optionKey]: "", code: "" };

    return (
      <div>
        <div className="filter-label">{label}</div>
        {<Dropdown t={t} option={options} selected={selected} select={(value) => select(value, key)} optionKey={optionKey} />}

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
    const statusesChanged = option.code === "PENDING_INSTALLATION" ? ["ASSIGNED_TO_FIELD_SUPERVISOR", "ASSIGNED_TO_FIELD_STAFF"] : [option.code];
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

  const onClearAll = () => {
    setCurrentFilter({
      district: [],
      block: [],
      status: [],
    });
    setBlockMenu([]);
  }

  const checkStatusFilterPresence = (status) => {
    if (status === "PENDING_INSTALLATION") {
      return currentFilter.status.includes("ASSIGNED_TO_FIELD_SUPERVISOR") || currentFilter.status.includes("ASSIGNED_TO_FIELD_STAFF");
    }
    return currentFilter.status.includes(status);
  }

  return (
    <React.Fragment>
      <div className="filter">
        <div className="filter-card" style={{ padding: "10px" }}>
          <div className="filter-header" style={{ display: "flex", justifyContent: "space-between" }}>
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
            <LinkLabel
              style={{ fontSize: "18px" }}
              onClick={onClearAll}
            >
              Clear
            </LinkLabel>
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
              <div style={{ marginTop: "-30px" }}>
                <CheckBox
                  key={index}
                  onChange={(e) => {handleStatusChange(option, e.target.checked)}}
                  checked={checkStatusFilterPresence(option.code)}
                  label={`${option.name}${ option.count > 0 ? ` (${option.count})` : ""}`}
                />
              </div>
            );
          })}
        </div>
      </div>
    </React.Fragment>
  );
};

export default Filter;
