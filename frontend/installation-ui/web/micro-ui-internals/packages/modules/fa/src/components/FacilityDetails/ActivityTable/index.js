import React, { useEffect, useState } from "react";
import useActivity from "../../../hooks/useActivity";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import Filter from "./Filter";

const ActivityTable = ({ t, facilityId }) => {

  const [fetchedData, setFetchedData] = useState([]);

  const [projectQueryFilter, setProjectQueryFilter] = useState({
    project: {
      facilityId: [facilityId],
    },
    facilityFilter: {
      activityCode: [],
    },
  });
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);

  const { isLoading, data: facilityData } = useActivity(projectQueryFilter, pageSize, pageOffset);

  useEffect(() => {
    if (facilityData) {
      setFetchedData(facilityData.facilities);
    }
  }, [facilityData]);

  const GetCell = (value) => (
    <span className="cell-text" style={{ color: "#000000" }}>
      {value}
    </span>
  );

  const columns = [
    {
      Header: t("CS_HEALTH_FACILITY"),
      Cell: ({ row }) => {
        return GetCell(row.original["facilityName"] ? row.original["facilityName"] : "-");
      },
    },
    {
      Header: t("CS_BLOCK"),
      Cell: ({ row }) => {
        return GetCell(row.original["block"] ? t(`Boundary_${row.original["block"]}`) : "-");
      },
    },
    {
      Header: t("CS_DISTRICT"),
      Cell: ({ row }) => {
        return GetCell(row.original["district"] ? t(`Boundary_${row.original["district"]}`) : "-");
      },
    },
    {
      Header: t("CS_ASSIGNED_TO"),
      Cell: ({ row }) => {
        return GetCell(row.original["assigned"] ? `${row.original["assigned"]}` : "-");
      },
    },
    {
      Header: t("CS_STATUS"),
      Cell: ({ row }) => {
        return GetCell(row.original["status"] ? t(`CS_${row.original["status"]}`) : "-");
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

  const handleFilterChange = (filters) => {
    setProjectQueryFilter({
      ...projectQueryFilter,
      ...filters,
    });
  };

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>{t("CS_NO_FACILITIES_FOUND")}</div>
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
            customTableWrapperClassName={"health-facility-table"}
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
            totalRecords={facilityData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    );
  };

  return (
    <div style={{ width: "100%", display: "flex", gap: "15px" }}>
      <div style={{ minWidth: "300px" }}>
        <Filter
          t={t}
          type="desktop"
          projectQueryFilter={projectQueryFilter}
          onFilterChange={handleFilterChange}
        />
      </div>
      <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
        {renderFacilities()}
      </div>
    </div>
  );
};

export default ActivityTable;