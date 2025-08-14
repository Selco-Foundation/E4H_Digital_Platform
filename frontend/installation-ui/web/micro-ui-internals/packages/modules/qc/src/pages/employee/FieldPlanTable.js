import React, { useEffect, useRef, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import SearchCentre from "../../components/FieldPlanTable/Search";
import { setSelectedFieldPlan } from "../../redux/actions";
import { Link, useHistory, useLocation, useRouteMatch } from "react-router-dom";
import { useDispatch } from "react-redux";

const FieldPlanTable = ({ t }) => {

  const [fetchedData, setData] = useState([]);
  const dispatch = useDispatch();
  const { path } = useRouteMatch();
  const history = useHistory();
  const location = useLocation();
  const queryParams = new URLSearchParams(window.location.search);
  const [queryFilter, setQueryFilter] = useState((() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : null;
    } catch (error) {
      console.error("Failed to parse filter parameter:", error);
      return null;
    }
  })() || {
    Project : {
      projectTypeId: "FieldPlan",
      name: ""
    }
  });
  const prevSearchParamsRef = useRef(JSON.stringify(queryFilter));
  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const prevPageSizeRef = useRef(pageSize);

  const { isLoading, data } = Digit.Hooks.qc.useFieldPlan(queryFilter, pageSize, pageOffset);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(queryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
    });
  }, [queryFilter, pageSize, pageOffset])

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(queryFilter);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [queryFilter, pageSize]);

  const onSearch = (textToSearch) => {
    setQueryFilter({
      Project : {
        projectTypeId: "FieldPlan",
        name: textToSearch
      }
    });
  }

  const onClear = () => {
    setQueryFilter({
      Project : {
        projectTypeId: "FieldPlan"
      }
    });
  }

  useEffect(() => {
    if (data) {
      setData(data.fieldPlans);
    }
  }, [data]);

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

  const GetCell = (value) => <span className="cell-text">{value}</span>;

  const GetProgress = (value) => {
    return (
      <div style={{ display: "flex", gap:"10px" }}>
        <div style={{ width: "100px", height: "20px", background: "#E0E0E0", borderRadius: "5px" }}>
          <div style={{ height: "20px", width: `${value}px`, maxWidth: "100px", background: "#00703C", borderRadius: "5px" }}></div>
        </div>
        <div>{value}%</div>
      </div>
    );
  };

  const GetPendingApprovalCount = (projectFacilityInfo) => {
    const value = projectFacilityInfo["SUBMITTED_BY_SUPERVISOR"];
    if (value) {
      return (
        <div
          style={{
            minWidth: "30px",
            width: "fit-content",
            height: "20px",
            padding: "0 5px",
            background: "red",
            borderRadius: "10px",
            color: "white",
            textAlign: "center"
          }}
        >
          <div>{value}</div>
        </div>
      )
    }
  }

  const columnsList = [
    {
      Header: t("CS_FIELD_PLAN_CODE"),
      Cell: ({ row }) => {
        return (
          <div style={{display: "flex", alignItems: "center", gap: "10px"}}>
            <span className="link" onClick={() => dispatch(setSelectedFieldPlan(row.original))}>
              <Link to={`${path}/${row.original["id"]}/facilities`} style={{ color: "#C84C0E" }}>
                {row.original["name"]}
              </Link>
            </span>
            {GetPendingApprovalCount(row.original["projectFacilityInfo"])}
          </div>
        );
      },
    },
    {
      Header: t("CS_ACTIVITY_TYPE"),
      Cell: ({ row }) => {
        return GetCell(`${row.original["projectType"]}`);
      },
    },
    {
      Header: t("CS_HEALTH_FACILITIES"),
      Cell: ({ row }) => {
        return GetCell(`${row.original["facilitiesCount"]}`);
      },
    },
    {
      Header: t("CS_START_DATE"),
      Cell: ({ row }) => {
        return GetCell(`${row.original["startDate"]}`);
      },
    },
    {
      Header: t("CS_END_DATE"),
      Cell: ({ row }) => {
        return GetCell(`${row.original["endDate"]}`);
      },
    },
    {
      Header: t("CS_COMPLETION_RATE"),
      Cell: ({ row }) => {
        return GetProgress(`${row.original["completionRate"]}`);
      },
    },
  ];

  const renderFieldPlans = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "300px", backgroundColor: "white" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("CS_NO_FIELD_PLANS_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div style={{
        backgroundColor: "white",
        padding: "15px 0px 0px 0px",
      }}>
        <div style={{
          margin: "0px 20px",
          overflow: "auto",
        }}>
          <Table
            t={t}
            data={fetchedData}
            columns={columnsList}
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
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        Inbox
      </div>
      <SearchCentre queryFilter={queryFilter} onSearch={onSearch} onClear={onClear} />
      {renderFieldPlans()}
    </div>
  );
};

export default FieldPlanTable;
