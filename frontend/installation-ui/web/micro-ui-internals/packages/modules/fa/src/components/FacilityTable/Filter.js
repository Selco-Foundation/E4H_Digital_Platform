import React, { useEffect, useState } from "react";
import {
  Dropdown,
  RemoveableTag,
  Loader
} from "@egovernments/digit-ui-react-components";
import RefreshButton from "../RefreshButton";
import useBoundary from "../../hooks/useBoundary";
import CustomFilterIcon from "../Custom/CustomFilterIcon";
import CustomDropdown from "../Custom/CustomDropdown";

const Filter = ({ t, onFilterChange, projectQueryFilter }) => {

  const [stateMenu, setStateMenu] = useState([]);
  const [districtOptions, setDistrictOptions] = useState([]);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [blockOptions, setBlockOptions] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [facilityOptions, setFacilityOptions] = useState([]);
  const [facilityMenu, setFacilityMenu] = useState([]);
  const [currentFilter, setCurrentFilter] = useState(projectQueryFilter.facilityFilter || {
    state: [],
    district: [],
    block: [],
    facility: [],
  });

  const { isLoading, data } = useBoundary("");

  useEffect(() => {
    if (data) {
      setStateMenu(
        data.states?.map(state => ({...state, name: t(`Boundary_${state.code}`)}))
        .sort((a, b) => a?.name?.localeCompare(b?.name))
      );
      setDistrictOptions(data.districts?.map(district => ({...district, name: t(`Boundary_${district.code}`)})));
      setBlockOptions(data.blocks?.map(block => ({...block, name: t(`Boundary_${block.code}`)})));
      setFacilityOptions(data.facilities?.map(facility => ({...facility, name: t(`Boundary_${facility.code}`)})));
    }
  }, [data, t]);

  useEffect(() => {
    const facilityFilterQuery = {};

    if (currentFilter.facility.length > 0) {
      facilityFilterQuery.facility = currentFilter.facility.map((facility) => facility.code);
    }
    if (currentFilter.block.length > 0) {
      facilityFilterQuery.block = currentFilter.block.map((block) => block.code);
    }
    if (currentFilter.district.length > 0) {
      facilityFilterQuery.district = currentFilter.district.map((district) => district.code);
    }
    if (currentFilter.state.length > 0) {
      facilityFilterQuery.state = currentFilter.state.map((state) => state.code);
    }

    onFilterChange({
      facilityFilter: {
        ...currentFilter
      },
      facilityFilterQuery
    });
  }, [currentFilter, blockOptions]);

  useEffect(() => {
    if (!data) return
    const selectedStateCodes = currentFilter.state.map((state) => state.code);
    if (selectedStateCodes?.length) {
      const newDistrictMenu = districtOptions
        .filter((district) => selectedStateCodes.includes(district?.parentCode))
        .sort((a, b) => a?.name?.localeCompare(b?.name));

      setDistrictMenu(newDistrictMenu);
    }

    const selectedDistrictCodes = currentFilter.district.map((district) => district.code);
    if (selectedDistrictCodes?.length) {
      const newBlockMenu = blockOptions
        .filter((block) => selectedDistrictCodes.includes(block?.parentCode))
        .sort((a, b) => a?.name?.localeCompare(b?.name));

      setBlockMenu(newBlockMenu);
    }

    const newSelectedBlockCodes = currentFilter.block.map((block) => block.code);
    if (newSelectedBlockCodes?.length) {
      const newFacilityMenu = facilityOptions
        .filter((facility) => newSelectedBlockCodes.includes(facility?.parentCode))
        .sort((a, b) => a?.name?.localeCompare(b?.name));

      setFacilityMenu(newFacilityMenu);
    }
  }, [currentFilter, data, districtOptions, blockOptions, facilityOptions, t]);

  const handleStateChange = (selectedState) => {
    if (currentFilter.state.every(state => state.code !== selectedState.code)) {
      setCurrentFilter({
        ...currentFilter,
        state: [...currentFilter.state, selectedState],
      });
    }
  }

  const handleDistrictChange = (selectedDistrict) => {
    if (currentFilter.district.every(district => district.code !== selectedDistrict.code)) {
      setCurrentFilter({
        ...currentFilter,
        district: [...currentFilter.district, selectedDistrict]
      });
    }
  }

  const handleBlockChange = (selectedBlock) => {
    if (currentFilter.block.every(block => block.code !== selectedBlock.code)) {
      setCurrentFilter({
        ...currentFilter,
        block: [...currentFilter.block, selectedBlock],
      });
    }
  };

  const handleFacilityChange = (selectedFacility) => {
    if (currentFilter.facility.every(facility => facility.code !== selectedFacility.code)) {
      setCurrentFilter({
        ...currentFilter,
        facility: [...currentFilter.facility, selectedFacility]
      });
    }
  };

  const onRemove = (index, key) => {
    let afterRemove = currentFilter[key].filter((value, i) => i !== index);

    if (key === "state") {
      const newSelectedStateCodes = afterRemove.map((state) => state.code);
      const newDistrictMenu = districtOptions.filter((district) => newSelectedStateCodes.includes(district.parentCode));
      const newSelectedDistricts = currentFilter.district.filter((district) => newSelectedStateCodes.includes(district.parentCode));
      const newSelectedDistrictCodes = newSelectedDistricts.map((district) => district.code);
      const newBlockMenu = blockOptions.filter((block) => newSelectedDistrictCodes.includes(block.parentCode));
      const newSelectedBlocks = currentFilter.block.filter((block) => newSelectedDistrictCodes.includes(block.parentCode));
      const newSelectedBlockCodes = newSelectedBlocks.map((block) => block.code);
      const newFacilityMenu = facilityOptions.filter((facility) => newSelectedBlockCodes.includes(facility.parentCode));
      const newSelectedFacilities = currentFilter.facility.filter((facility) => newSelectedBlockCodes.includes(facility.parentCode));

      setDistrictMenu(newDistrictMenu);
      setBlockMenu(newBlockMenu);
      setFacilityMenu(newFacilityMenu);
      setCurrentFilter({
        ...currentFilter,
        state: afterRemove,
        district: newSelectedDistricts,
        block: newSelectedBlocks,
        facility: newSelectedFacilities,
      });

    } else if (key === "district") {
      const newSelectedDistrictCodes = afterRemove.map((district) => district.code);
      const newBlockMenu = blockOptions.filter((block) => newSelectedDistrictCodes.includes(block.parentCode));
      const newSelectedBlocks = currentFilter.block.filter((block) => newSelectedDistrictCodes.includes(block.parentCode));
      const newSelectedBlockCodes = newSelectedBlocks.map((block) => block.code);
      const newFacilityMenu = facilityOptions.filter((facility) => newSelectedBlockCodes.includes(facility.parentCode));
      const newSelectedFacilities = currentFilter.facility.filter((facility) => newSelectedBlockCodes.includes(facility.parentCode));

      setBlockMenu(newBlockMenu);
      setFacilityMenu(newFacilityMenu);
      setCurrentFilter({
        ...currentFilter,
        district: afterRemove,
        block: newSelectedBlocks,
        facility: newSelectedFacilities,
      });

    } else if (key === "block") {
      const newSelectedBlockCodes = afterRemove.map((block) => block.code);
      const newFacilityMenu = facilityOptions.filter((facility) => newSelectedBlockCodes.includes(facility.parentCode));
      const newSelectedFacilities = currentFilter.facility.filter((facility) => newSelectedBlockCodes.includes(facility.parentCode));

      setFacilityMenu(newFacilityMenu);
      setCurrentFilter({
        ...currentFilter,
        block: afterRemove,
        facility: newSelectedFacilities,
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
        {<CustomDropdown t={t} option={options} selected={selected} select={(value) => select(value, key)} optionKey={optionKey} />}

        <div className="tag-container">
          {currentFilter[key].length > 0 &&
            currentFilter[key].map((value, index) => {
              return <RemoveableTag key={index} text={`${value[optionKey]} ...`} onClick={() => onRemove(index, key)} />;
            })}
        </div>
      </div>
    );
  };

  const onClearAll = () => {
    setCurrentFilter({
      state: [],
      district: [],
      block: [],
      facility: [],
    });
    setDistrictMenu([]);
    setBlockMenu([]);
    setFacilityMenu([]);
  }

  return (
    <React.Fragment>
      <div className="filter" style={{ width: "100%" }}>
        <div className="filter-card" style={{ padding: "20px" }}>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <div
              style={{
                fontFamily: "Roboto",
                fontWeight: 700,
                fontSize: "24px",
                lineHeight: "2rem",
                letterSpacing: "0px",
                color: "#0B0C0C",
                display: "flex",
                gap: "15px",
                alignItems: "center",
                marginBottom: "20px",
              }}
            >
              <CustomFilterIcon fill={"#0B4B66"} />
              {t("CORE_COMMON_FILTER")}
            </div>
            <button
              type="button"
              style={{
                cursor: "pointer",
                border: "1px solid #C84C0E",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontWeight: 500,
                height: "2rem",
                width: "2rem",
                fontSize: "24px",
                background: "transparent"
              }}
              onClick={onClearAll}
            >
              <RefreshButton fill={"#C84C0E"} />
            </button>
          </div>
          <div>
            {
              GetSelectOptions(
                t("CS_STATE"),
                stateMenu,
                null,
                handleStateChange,
                "name",
                onRemove,
                "state"
              )
            }
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
          <div>
            {
              GetSelectOptions(
                t("CS_FACILITY"),
                facilityMenu,
                null,
                handleFacilityChange,
                "name",
                onRemove,
                "facility"
              )
            }
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Filter;
