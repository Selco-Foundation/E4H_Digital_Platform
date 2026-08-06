import React, { useMemo, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import useAMCConfigurationList from "../../hooks/useAMCConfigurationList";
import { populateWorkingAMCConfiguration } from "../../redux/actions";

const AMCConfigurations = () => {

  const { t } = useTranslation();
  const dispatch = useDispatch();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);

  const { isLoading, data } = useAMCConfigurationList({}, pageSize, pageOffset);

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = date.toLocaleString("en-US", { month: "long" });
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${day} ${month} ${year}`;
  };

  const GetHead = (value) => (
    <div style={{ height: "38px", width: "100%", display: "flex", alignItems: "center" }}>
      <span>{value}</span>
    </div>
  );

  const GetCell = (value) => (
    <span style={{ fontSize: "16px", fontWeight: "400", fontFamily: "Roboto", color: "#363636" }}>
      {value}
    </span>
  );

  const GetConfiguration = (durationMonths, visitFrequencyMonths) => (
    <div>
      <span>{t("AMC_DURATION_MONTHS")}: {durationMonths ?? "-"}</span>
      <br/>
      <span>{t("AMC_VISIT_FREQUENCY_MONTHS")}: {visitFrequencyMonths ?? "-"}</span>
    </div>
  );

  const GetAssetTypeList = (assetTypes) => (
    <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
      {assetTypes?.map((assetType, index) => (
        <span
          key={assetType.code || index}
          style={{
            backgroundColor: "#F1FFF8",
            color: "#00703C",
            width: "fit-content",
            padding: "5px 10px",
          }}
        >
          {assetType.name}
        </span>
      ))}
    </div>
  );

  const columns = useMemo(
    () => [
      {
        id: "facilityName",
        Header: () => GetHead(t("CS_HEALTH_FACILITY")),
        Cell: ({ row }) => (
          <Link
            to={`/${window.contextPath}/employee/pm/amc-configurations/${row.original["id"]}/visits`}
            style={{ color: "#C84C0E" }}
            onClick={() => dispatch(populateWorkingAMCConfiguration(row.original))}
          >
            {row.original["facilityName"]}
          </Link>
        ),
      },
      {
        id: "facilityId",
        Header: () => GetHead(t("CS_HEALTH_FACILITY_ID")),
        Cell: ({ row }) => GetCell(row.original["facilityId"] || "-"),
      },
      {
        id: "projectName",
        Header: () => GetHead(t("PM_PROJECT_NAME")),
        Cell: ({ row }) => GetCell(row.original["projectName"] || "-"),
      },
      {
        id: "assetTypes",
        Header: () => GetHead(t("AMC_ASSET_TYPES")),
        Cell: ({ row }) => GetAssetTypeList(row.original["assetTypes"]),
      },
      {
        id: "configurationStartDate",
        Header: () => GetHead(t("AMC_CONFIGURATION_START_DATE")),
        Cell: ({ row }) => GetCell(row.original["configurationStartDate"] ? formatDate(row.original["configurationStartDate"]) : "-"),
      },
      {
        id: "configuration",
        Header: () => GetHead(t("AMC_CONFIGURATION")),
        Cell: ({ row }) => GetConfiguration(row.original["durationMonths"], row.original["visitFrequencyMonths"]),
      },
      {
        id: "status",
        Header: () => GetHead(t("AMC_STATUS")),
        Cell: ({ row }) => GetCell(row.original["status"] ? t(`AMC_STATUS_${row.original["status"]}`) : "-"),
      },
    ],
    [t, dispatch]
  );

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  }

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  }

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  }

  const renderConfigurations = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (!data?.amcConfigurations?.length) {
      return (
        <div style={{ display: "flex", minWidth: "700px", justifyContent: "center", alignItems: "center", height: "300px", backgroundColor: "white" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("PM_NO_AMC_CONFIGURATIONS_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div style={{
        backgroundColor: "white",
        padding: "15px 0px 0px 0px",
        minWidth: "700px",
      }}>
        <div
          className={"amc-configuration-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            data={data.amcConfigurations}
            columns={columns}
            customTableWrapperClassName={"amc-configuration-table"}
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
            totalRecords={data.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    )
  }

  return (
    <div style={{marginTop: "20px", padding: mobileView ? "15px" : "0px 10px", overflow: "auto"}}>
      <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
        {t("PM_LABEL_AMCS")}
      </div>
      {renderConfigurations()}
    </div>
  );
};

export default AMCConfigurations;
