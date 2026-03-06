import React, { useEffect, useState } from "react";
import { Loader, Table, Button } from "@egovernments/digit-ui-react-components";
import Filter from "./Filter";
import useAsset from "../../../hooks/useAsset";
import AssetSpecsModal from "./AssetSpecsModal";

const AssetTable = ({ t, facilityId }) => {

  const [fetchedData, setFetchedData] = useState([]);
  const [assetSpecs, setAssetSpecs] = useState(null);

  const [assetQueryFilter, setAssetQueryFilter] = useState({
    facility: {
      facilityId: facilityId,
    },
    assetFilter: {
      assetType: [],
      isOperational: [],
      serialNumber: [],
    },
  });

  const { isLoading, data: assetData } = useAsset(assetQueryFilter);

  useEffect(() => {
    if (assetData) {
      setFetchedData(assetData);
    }
  }, [assetData]);

  const GetCell = (value) => (
    <span className="cell-text" style={{ color: "#000000" }}>
      {value}
    </span>
  );

  const columns = [
    {
      Header: t("ASSET_TYPE"),
      Cell: ({ row }) => {
        return GetCell(row.original["assetName"] ? row.original["assetName"] : "-");
      },
    },
    {
      Header: t("ASSET_SERIAL_NO"),
      Cell: ({ row }) => {
        return GetCell(row.original["serialNumber"] ? row.original["serialNumber"] : "-");
      },
    },
    {
      Header: t("ASSET_INSTALLATION_DATE"),
      Cell: ({ row }) => {
        return GetCell(row.original["warrantyStartDate"] ? row.original["warrantyStartDate"] : "-");
      },
    },
    {
      Header: t("ASSET_STATUS"),
      Cell: ({ row }) => {
        return GetCell(row.original["isOperational"] ? t("OPERATIONAL") : t("NOT_OPERATIONAL"));
      },
    },
    {
      Header: t("ASSET_SPECS"),
      Cell: ({ row }) => {
        return (
          <Button
            variation={"secondary"}
            onButtonClick={() =>
              setAssetSpecs({
                BRAND: row.original["brand"],
                MODEL_NUMBER: row.original["modelNumber"],
                CAPACITY: row.original["capacity"],
                ...(row.original["assetType"] === "BATTERY" && { VOLTAGE: row.original["voltage"] }),
              })
            }
            label={t("VIEW_SPECS")}
            style={{
              backgroundColor: "white",
              border: "1px solid #d35400",
              color: "#d35400",
              padding: "8px 20px",
              cursor: "pointer",
              fontWeight: "bold",
              fontSize: "16px",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              gap: "5px",
              height: "40px",
            }}
          ></Button>
        );
      },
    },
  ];

  const handleFilterChange = (filters) => {
    setAssetQueryFilter({
      ...assetQueryFilter,
      ...filters,
    });
  };

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%", minHeight: "300px" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>{t("CS_NO_ASSETS_FOUND")}</div>
        </div>
      );
    }

    return (
      <div
        style={{
          backgroundColor: "white",
          padding: "15px 0px 0px 0px",
        }}
      >
        <div
          className={"health-facility-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            customTableWrapperClassName={"facility-details-table"}
            data={fetchedData}
            columns={columns}
            getCellProps={() => {
              return {
                style: {
                  maxWidth: "100%",
                  padding: "17.24px 18px",
                  fontSize: "15px",
                },
              };
            }}
            isPaginationRequired={false}
          />
        </div>
      </div>
    );
  };

  const serialNumberList = fetchedData.map((item) => ({
    code: item.serialNumber,
    name: item.serialNumber,
  }));

  return (
    <div style={{ width: "100%", display: "flex", gap: "15px" }}>
      <div style={{ minWidth: "300px" }}>
        <Filter t={t} type="desktop" serialNumberList={serialNumberList} assetQueryFilter={assetQueryFilter} onFilterChange={handleFilterChange} />
      </div>
      <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
        {renderFacilities()}
      </div>
      {!!assetSpecs && (
        <AssetSpecsModal t={t} assetSpecs={assetSpecs} onClose={() => setAssetSpecs(null)} />
      )}
    </div>
  );
};

export default AssetTable;
