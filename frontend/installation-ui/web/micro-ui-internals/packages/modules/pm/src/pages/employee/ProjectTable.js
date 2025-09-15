import React, {useEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Loader, Table, SubmitBar} from "@egovernments/digit-ui-react-components";
import {Link} from "react-router-dom";

const SAMPLE_PROJECTS = [
    {
        id: "MH-QC_HO-2024-200centres-ASSAM",
        projectName: "MH-QC_HO-2024-200centres",
        state: "Assam",
        projectType: "Livelihood",
        startDate: "08/05/2025",
        endDate: "08/10/2025",
        status: "Scheduled",
        noOfHFs: 564,
    },
    {
        id: "MH-QC_HO-2024-200centres-MANIPUR",
        projectName: "MH-QC_HO-2024-200centres",
        state: "Manipur",
        projectType: "Livelihood",
        startDate: "08/05/2025",
        endDate: "08/10/2025",
        status: "In Progress",
        noOfHFs: 1244,
    },
    {
        id: "MH-QC_HO-2024-200centres-KARNATAKA",
        projectName: "MH-QC_HO-2024-200centres",
        state: "Karnataka",
        projectType: "Medtech",
        startDate: "08/05/2025",
        endDate: "08/10/2025",
        status: "Completed",
        noOfHFs: 4534,
    },
];

for (let i = 4; i <= 18; i++) {
    SAMPLE_PROJECTS.push({
        id: `MH-QC_HO-2024-${200 + i}centres-${i}`,
        projectName: `MH-QC_HO-2024-${200 + i}centres`,
        state: ["Assam", "Manipur", "Karnataka", "Odisha", "Kerala"][i % 5],
        projectType: ["Livelihood", "Medtech", "Healthcare"][i % 3],
        startDate: "08/05/2025",
        endDate: "08/10/2025",
        status: ["Scheduled", "In Progress", "Completed"][i % 3],
        noOfHFs: 300 + i * 37,
    });
}

const SORT_DIR = {
    ASC: "ASC",
    DESC: "DESC",
};

const parseDate = (ddmmyyyy) => {
    const [dd, mm, yyyy] = ddmmyyyy.split("/").map((s) => parseInt(s, 10));
    return new Date(yyyy, mm - 1, dd).getTime();
};

const ProjectTable = () => {
    const {t} = useTranslation();
    const [search, setSearch] = useState("");
    const [query, setQuery] = useState("");
    const [loading, setLoading] = useState(false);

    const [pageSize, setPageSize] = useState(5);
    const [pageOffset, setPageOffset] = useState(0);
    const prevPageSizeRef = useRef(pageSize);

    const [sortBy, setSortBy] = useState(null);
    const [sortDir, setSortDir] = useState(SORT_DIR.DESC);

    useEffect(() => {
        if (prevPageSizeRef.current !== pageSize) {
            setPageOffset(0);
            prevPageSizeRef.current = pageSize;
        }
    }, [pageSize]);

    const filtered = useMemo(() => {
        const q = (query || "").trim().toLowerCase();
        let out = SAMPLE_PROJECTS.filter((p) =>
            q ? p.id.toLowerCase().includes(q) : true
        );

        if (sortBy === "startDate") {
            out = out.sort((a, b) => {
                    const da = parseDate(a.startDate);
                    const db = parseDate(b.startDate);
                    return sortDir === SORT_DIR.ASC ? da - db : db - da;
                }
            );
        } else if (sortBy === "endDate") {
            out = out.sort((a, b) => {
                    const da = parseDate(a.endDate);
                    const db = parseDate(b.endDate);
                    return sortDir === SORT_DIR.ASC ? da - db : db - da;
                }
            );
        }
        return out;
    }, [query, sortBy, sortDir]);

    const paged = useMemo(() => {
        const start = pageOffset;
        const end = pageOffset + pageSize;
        return filtered.slice(start, end);
    }, [filtered, pageOffset, pageSize]);

    const onNextPage = () => {
        const next = pageOffset + pageSize;
        if (next < filtered.length) setPageOffset(next);
    };
    const onPrevPage = () => {
        const prev = pageOffset - pageSize;
        if (prev >= 0) setPageOffset(prev);
    };
    const onPageSizeChange = (e) => {
        setPageSize(parseInt(e.target.value, 10));
    };

    const toggleSort = (field) => {
        if (sortBy !== field) {
            setSortBy(field);
            setSortDir(SORT_DIR.DESC);
        } else {
            setSortDir((d) => (d === SORT_DIR.DESC ? SORT_DIR.ASC : SORT_DIR.DESC));
        }
    };

    const DefaultHeader = ({label}) => (
        <span style={{
            color: '#0B0C0C',
            fontSize: "16px"
        }}>
            {label}
        </span>
    );

    const SortHeader = ({label, field}) => (
        <span
            onClick={() => toggleSort(field)}
            style={{cursor: "pointer", userSelect: "none", color: '#0B0C0C', fontSize: "16px"}}
            title="Click to sort"
        >
            {label}{" "}
            {sortBy === field ? (sortDir === SORT_DIR.DESC ? "↑" : "↓") : "↑↓"}
        </span>
    );

    const GetCell = (value) => (
        <span
            className="cell-text"
            style={{color: "#0B0C0C"}}>
            {value}
        </span>);

    const columns = [
        {
            Header: <DefaultHeader label={t("PM_LABEL_PROJECT_NAME")} />,
            accessor: "projectName",
            Cell: ({row}) => (
                <div>
                    <Link
                        to="#"
                        onClick={(e) => e.preventDefault()}
                        className="link"
                        style={{color: "#C84C0E", textDecoration: "none"}}
                    >
                        {row.original.projectName}
                    </Link>
                </div>
            )
        },
        {
            Header: <DefaultHeader label={t("PM_PROJECT_INFO_STATE")} />,
            accessor: "state",
            Cell: ({row}) => GetCell(row.original.state),
        },
        {
            Header: <DefaultHeader label={t("PM_LABEL_PROJECT_TYPE")} />,
            accessor: "projectType",
            Cell: ({row}) => GetCell(row.original.projectType),
        },
        {
            Header: <SortHeader label={t("CORE_COMMON_START_DATE")} field="startDate"/>,
            accessor: "startDate",
            Cell: ({row}) => GetCell(row.original.startDate),
        },
        {
            Header: <SortHeader label={t("CS_END_DATE")} field="endDate"/>,
            accessor: "endDate",
            Cell: ({row}) => GetCell(row.original.endDate),
        },
        {
            Header: <DefaultHeader label={t("CORE_COMMON_STATUS")} />,
            accessor: "status",
            Cell: ({row}) => GetCell(row.original.status),
        },
        {
            Header: <DefaultHeader label={t("PM_LABEL_PROJECT_NO_OF_HFS")} />,
            accessor: "numberOfHealthFacilities",
            Cell: ({row}) => GetCell(row.original.noOfHFs),
        },
    ];

    return (
        <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
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
                <div style={{marginBottom: "8px", color: "#0B0C0C", fontWeight: 600}}>
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
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder={t("PM_LABEL_SEARCH_PROJECT_ID")}
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
                            onClick={() => {
                                setSearch("");
                                setQuery("");
                                setPageOffset(0);
                            }}
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
                            onSubmit={() => {
                                setQuery(search);
                                setPageOffset(0);
                            }}
                            style={{width: 180}}
                        />
                    </div>
                </div>
            </div>

            <div style={{backgroundColor: "white"}}>
                <div style={{padding: "20px"}}>

                    {loading
                        ? (<Loader/>)
                        : (
                            <div style={{margin: "0 0px", overflow: "auto"}}>
                                <Table
                                    t={t || ((k) => k)}
                                    data={paged}
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
                                    totalRecords={filtered.length}
                                    onPageSizeChange={onPageSizeChange}
                                    pageSizeLimit={pageSize}
                                />
                            </div>
                        )}
                </div>
            </div>
        </div>
    );
};

export default ProjectTable;