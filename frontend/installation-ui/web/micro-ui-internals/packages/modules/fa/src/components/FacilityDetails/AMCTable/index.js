import React, { useEffect, useState } from "react";
import { Loader, Table, Button } from "@egovernments/digit-ui-react-components";
import useAMCConfiguration from "../../../hooks/useAMCConfiguration";
import VisitsModal from "./VisitsModal";

const AMCTable = ({ t, facilityId }) => {

  const [fetchedData, setFetchedData] = useState([]);
  const [amcConfigurationDisplayed, setAMCConfigurationDisplayed] = useState(null);

  const amcConfigurationQueryFilter = {
    facility: {
      facilityId: [facilityId],
    },
  };
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);

  const { isLoading, data: amcConfigurationData } = useAMCConfiguration(amcConfigurationQueryFilter, pageSize, pageOffset);

  useEffect(() => {
    if (amcConfigurationData) {
      setFetchedData(amcConfigurationData.amcConfigurations);
    }
  }, [amcConfigurationData]);

  const GetCell = (value) => (
    <span className="cell-text" style={{ color: "#000000" }}>
      {value}
    </span>
  );

  const GetConfiguration = (frequency, duration) => (
    <div>
      <span>
        Frequency: {frequency}
      </span>
      <br/>
      <span>
        Duration: {duration}
      </span>
    </div>
  )

  const GetAssetSerialNumberList = (assetSerialNumbers) => (
    <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
      {assetSerialNumbers?.map((assetSerialNumber) => (
        <span
          key={assetSerialNumber}
          style={{
            backgroundColor: "#F1FFF8",
            color: "#00703C",
            width: "fit-content",
            padding: "5px 10px",
          }}
        >
          {assetSerialNumber}
        </span>
      ))}
    </div>
  );

  const columns = [
    {
      Header: t("PROJECT_ID"),
      Cell: ({ row }) => {
        return GetCell(row.original["projectName"] ? row.original["projectName"] : "-");
      },
    },
    {
      Header: t("AMC_START_DATE"),
      Cell: ({ row }) => {
        return GetCell(row.original["amcStartDate"] ? row.original["amcStartDate"] : "-");
      },
    },
    {
      Header: t("AMC_VENDOR"),
      Cell: ({ row }) => {
        return GetCell(row.original["vendorName"] ? row.original["vendorName"] : "-");
      },
    },
    {
      Header: t("AMC_CONFIGURATION"),
      Cell: ({ row }) => {
        return GetConfiguration(row.original["frequency"] ? row.original["frequency"] : "-", row.original["duration"] ? row.original["duration"] : "-");
      },
    },
    {
      Header: t("COMPLETED_VISITS"),
      Cell: ({ row }) => {
        return GetCell(row.original["completedVisits"] ? `${row.original["completedVisits"]}/${row.original["totalVisits"]} ${t("COMPLETED")}` : "-");
      },
    },
    {
      Header: t("AMC_STATUS"),
      Cell: ({ row }) => {
        return GetCell(row.original["status"] ? t(`AMC_STATUS_${row.original["status"]}`) : "-");
      },
    },
    {
      Header: t("LINKED_ASSETS"),
      Cell: ({ row }) => {
        return row.original["assetSerialNumbers"]?.length ? GetAssetSerialNumberList(row.original["assetSerialNumbers"]) : "-";
      },
    },
    {
      Header: t("VISIT_REPORTS"),
      Cell: ({ row }) => {
        return (
          <Button
            variation={"secondary"}
            onButtonClick={() => setAMCConfigurationDisplayed(row.original["id"])}
            label={t("VIEW_REPORTS")}
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

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  };

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  };

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  };

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%", minHeight: "300px" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>{t("NO_AMC_FOUND")}</div>
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
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={amcConfigurationData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    );
  };

  return (
    <div style={{ width: "100%", backgroundColor: "white" }}>
      {renderFacilities()}
      {amcConfigurationDisplayed && (
        <VisitsModal
          t={t}
          amcConfigurationId={amcConfigurationDisplayed}
          onClose={() => setAMCConfigurationDisplayed(null)}
        />
      )}
    </div>
  );
};

export default AMCTable;
