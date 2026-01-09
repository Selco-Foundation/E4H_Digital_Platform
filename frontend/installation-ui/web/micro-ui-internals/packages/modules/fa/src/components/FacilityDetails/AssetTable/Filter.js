import React, { useEffect, useState } from "react";
import {
  Dropdown,
  RemoveableTag,
  Loader
} from "@egovernments/digit-ui-react-components";
import RefreshButton from "../../RefreshButton";
import CustomFilterIcon from "../../Custom/CustomFilterIcon";
import CustomDropdown from "../../Custom/CustomDropdown";

const Filter = ({ t, onFilterChange, assetQueryFilter, serialNumberList }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [currentFilter, setCurrentFilter] = useState(assetQueryFilter.assetFilter || {
      assetType: [],
      isOperational: [],
      serialNumber: [],
  });

  const { isLoading, data: assetTypeData } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "asset-registry",
    [
      {
        name: "AssetTypeSchema",
      },
    ],
    {
      select: (data) => {
        return (data?.["asset-registry"]?.["AssetTypeSchema"]?.[0]?.["AssetType"] || []).map((assetType) => ({
          code: assetType.code,
          name: assetType.name,
        }));
      },
      enabled: !!tenantId,
    }
  );

  const assetStatusMenu = [
    { code: "OPERATIONAL", name: t("OPERATIONAL") },
    { code: "NON_OPERATIONAL", name: t("NOT_OPERATIONAL") },
  ]

  useEffect(() => {
    const facilityFilterQuery = {};

    if (currentFilter.assetType.length > 0) {
      facilityFilterQuery.assetType = currentFilter.assetType.map((assetType) => assetType.code);
    }

    if (currentFilter.isOperational.length > 0) {
      facilityFilterQuery.isOperational = currentFilter.isOperational.map((assetStatus) => assetStatus.code === "OPERATIONAL");
    }

    if (currentFilter.serialNumber.length > 0) {
      facilityFilterQuery.serialNumber = currentFilter.serialNumber.map((serialNumber) => serialNumber.code);
    }

    onFilterChange({
      facilityFilter: {
        ...currentFilter
      },
      facilityFilterQuery
    });
  }, [currentFilter]);

  const onRemove = (index, key) => {
    let afterRemove = currentFilter[key].filter((value, i) => i !== index);
    setCurrentFilter({ ...currentFilter, [key]: afterRemove });
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

  const handleAssetTypeChange = (selectedAssetType) => {
    if (currentFilter.assetType.every((assetType) => assetType.code !== selectedAssetType.code)) {
      setCurrentFilter({
        ...currentFilter,
        assetType: [...currentFilter.assetType, selectedAssetType],
      });
    }
  };

  const handleAssetStatusChange = (selectedAssetStatus) => {
    if (currentFilter.assetType.every((assetType) => assetType.code !== selectedAssetStatus.code)) {
      setCurrentFilter({
        ...currentFilter,
        isOperational: [selectedAssetStatus],
      });
    }
  };

  const handleAssetSerialNumberChange = (selectedSerialNumber) => {
    if (currentFilter.serialNumber.every((serialNumber) => serialNumber.code !== selectedSerialNumber.code)) {
      setCurrentFilter({
        ...currentFilter,
        serialNumber: [...currentFilter.serialNumber, selectedSerialNumber],
      });
    }
  };

  const onClearAll = () => {
    setCurrentFilter({
      assetType: [],
      isOperational: [],
      serialNumber: [],
    });
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
                t("ASSET_TYPE"),
                assetTypeData,
                null,
                handleAssetTypeChange,
                "name",
                onRemove,
                "assetType"
              )
            }
            {
              GetSelectOptions(
                t("ASSET_STATUS"),
                assetStatusMenu,
                null,
                handleAssetStatusChange,
                "name",
                onRemove,
                "isOperational"
              )
            }
            {
              GetSelectOptions(
                t("ASSET_SERIAL_NO"),
                serialNumberList,
                null,
                handleAssetSerialNumberChange,
                "name",
                onRemove,
                "serialNumber"
              )
            }
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Filter;
