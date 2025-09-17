import React, { useEffect, useRef, useState, useMemo, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { Loader, Table, SubmitBar } from "@egovernments/digit-ui-react-components";
import { Link, useHistory, useLocation } from "react-router-dom";
import useProject from "../../hooks/useProject";

const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${month}/${day}/${year}`;
};

const ProjectTable = () => {
    const { t } = useTranslation();
    const history = useHistory();
    const location = useLocation();
    const queryParams = new URLSearchParams(window.location.search);

    const initialFilter = useMemo(() => {
        try {
            const filterParam = queryParams.get("filter");
            return filterParam ? JSON.parse(filterParam) : {
                subProjectTypeId: "PROJECT"
            };
        } catch (error) {
            return {
                subProjectTypeId: "PROJECT"
            };
        }
    }, []);

    const [queryFilter, setQueryFilter] = useState(initialFilter);
    const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
    const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
    const [searchText, setSearchText] = useState(queryFilter.name || "");

    const prevSearchParamsRef = useRef(JSON.stringify(queryFilter));
    const prevPageSizeRef = useRef(pageSize);

    const { isLoading, isError, error, data } = useProject(
        queryFilter,
        pageSize,
        pageOffset
    );

    useEffect(() => {
        history.replace({
            pathname: location.pathname,
            search: `filter=${JSON.stringify(queryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
        });
    }, [queryFilter, pageSize, pageOffset]);

    useEffect(() => {
        const prevSearchParams = prevSearchParamsRef.current;
        const currentSearchParams = JSON.stringify(queryFilter);

        if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
            setPageOffset(0);
            prevSearchParamsRef.current = currentSearchParams;
            prevPageSizeRef.current = pageSize;
        }
    }, [queryFilter, pageSize]);

    const handleSearch = () => {
        setQueryFilter({
            name: searchText,
            subProjectTypeId: "PROJECT"
        });
    };

    const handleClear = () => {
        setSearchText("");
        setQueryFilter({
            subProjectTypeId: "PROJECT"
        });
    };

    const onNextPage = () => {
        setPageOffset(pageOffset + pageSize);
    };

    const onPrevPage = () => {
        setPageOffset(pageOffset - pageSize);
    };

    const onPageSizeChange = (e) => {
        setPageSize(parseInt(e.target.value, 10));
    };

    const DefaultHeader = ({ label }) => (
        <span style={{ color: '#0B0C0C', fontSize: "16px" }}>
            {label}
        </span>
    );

    const GetCell = (value) => (
        <span className="cell-text" style={{ color: "#0B0C0C" }}>
            {value}
        </span>
    );

    const columns = [
        {
            Header: <DefaultHeader label={t("PM_LABEL_PROJECT_NAME")} />,
            accessor: "projectName",
            Cell: ({ row }) => (
                <div>
                    <Link
                        to={`/${window.contextPath}/employee/pm/project/${row.original.id}/details`}
                        className="link"
                        style={{ color: "#C84C0E", textDecoration: "none" }}
                    >
                        {row.original.name || row.original.projectNumber}
                    </Link>
                </div>
            )
        },
        {
            Header: <DefaultHeader label={t("PM_PROJECT_INFO_STATE")} />,
            accessor: "state",
            Cell: ({ row }) => GetCell(
                row.original.additionalDetails?.geographyDetails?.state?.code !== "-" ?
                    t(`STATE_${row.original.additionalDetails?.geographyDetails?.state?.code.toUpperCase()}`) :
                    "-"
            ),
        },
        {
            Header: <DefaultHeader label={t("PM_LABEL_PROJECT_TYPE")} />,
            accessor: "projectType",
            Cell: ({ row }) => GetCell(row.original.projectType || "-"),
        },
        {
            Header: <DefaultHeader label={t("CORE_COMMON_START_DATE")} />,
            accessor: "startDate",
            Cell: ({ row }) => GetCell(row.original.startDate ? formatDate(row.original.startDate) : "-"),
        },
        {
            Header: <DefaultHeader label={t("CS_END_DATE")} />,
            accessor: "endDate",
            Cell: ({ row }) => GetCell(row.original.endDate ? formatDate(row.original.endDate) : "-"),
        },
        {
            Header: <DefaultHeader label={t("CORE_COMMON_STATUS")} />,
            accessor: "status",
            Cell: ({ row }) => GetCell(t(`PM_PROJECT_STATUS_${row.original.status ? row.original.status.toUpperCase() : "DRAFT"}`)),
        },
        {
            Header: <DefaultHeader label={t("PM_LABEL_PROJECT_NO_OF_HFS")} />,
            accessor: "numberOfHealthFacilities",
            Cell: ({ row }) => GetCell(row.original.additionalDetails?.countProjectFacilities || 0),
        },
    ];

    return (
        <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
            {isLoading && (
                <div
                    style={{
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        height: "100%",
                        width: "100%",
                        zIndex: 5,
                        backgroundColor: "rgba(255, 255, 255, 0.7)",
                        position: "fixed",
                        top: 0,
                        left: 0,
                    }}
                >
                    <Loader />
                </div>
            )}

            <div
                style={{
                    fontSize: "40px",
                    fontWeight: "bold",
                    fontFamily: "Roboto Condensed",
                    marginBottom: "20px",
                    color: "#0B0C0C",
                }}
            >
                {t("PM_LABEL_MY_PROJECTS")}
            </div>

            <div
                style={{
                    backgroundColor: "white",
                    padding: "24px",
                    marginBottom: "16px",
                    borderRadius: "2px",
                }}>
                <div style={{ marginBottom: "8px", color: "#0B0C0C", fontWeight: 600 }}>
                    {t("PM_LABEL_SEARCH_PROJECT_ID")}
                </div>
                <div
                    style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        width: "100%",
                    }}>
                    <input
                        type="text"
                        value={searchText}
                        onChange={(e) => setSearchText(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        style={{
                            width: "420px",
                            maxWidth: "100%",
                            height: "40px",
                            border: "1px solid #D6D5D4",
                            borderRadius: "2px",
                            padding: "0 12px",
                            fontSize: "16px",
                        }}
                    />
                    <div
                        style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "12px"
                        }}>
                        <button
                            type="button"
                            onClick={handleClear}
                            style={{
                                border: "none",
                                background: "transparent",
                                color: "#C84C0E",
                                fontWeight: 600,
                                cursor: "pointer",
                            }}>
                            {t("CORE_COMMON_CLEAR_SEARCH")}
                        </button>
                        <SubmitBar
                            label={t("CORE_COMMON_SEARCH")}
                            onSubmit={handleSearch}
                            style={{ width: 180 }}
                        />
                    </div>
                </div>
            </div>

            <div style={{ backgroundColor: "white" }}>
                <div style={{ padding: "20px" }}>
                    {data?.projects && data.projects.length > 0 ? (
                        <div style={{ margin: "0 0px", overflow: "auto" }}>
                            <Table
                                t={t}
                                data={data.projects}
                                columns={columns}
                                getCellProps={() => ({
                                    style: {
                                        maxWidth: "100%",
                                        padding: "16px",
                                        fontSize: "16px"
                                    },
                                })}
                                onNextPage={onNextPage}
                                onPrevPage={onPrevPage}
                                currentPage={Math.floor(pageOffset / pageSize)}
                                totalRecords={data.totalCount || 0}
                                onPageSizeChange={onPageSizeChange}
                                pageSizeLimit={pageSize}
                            />
                        </div>
                    ) : (
                        <div style={{
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                            height: "200px",
                            fontSize: "18px",
                            color: "#666"
                        }}>
                            {!isLoading && t("CS_NO_FACILITIES_FOUND")}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProjectTable;