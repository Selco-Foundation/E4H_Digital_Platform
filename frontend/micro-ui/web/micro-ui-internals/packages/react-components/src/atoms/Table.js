import React, { useEffect, useState, useRef, forwardRef } from "react";
import { useGlobalFilter, usePagination, useRowSelect, useSortBy, useTable } from "react-table";
import { ArrowBack, ArrowForward, ArrowToFirst, ArrowToLast, SortDown, SortUp, DoubleTickIcon } from "./svgindex";
import CheckBox from "./CheckBox";
import ActionBar from "./ActionBar";
import SubmitBar from "./SubmitBar";
import Toast from "./Toast";

const noop = () => {};

const IndeterminateCheckbox = forwardRef(({ indeterminate, ...rest }, ref) => {
  const defaultRef = useRef();
  const resolvedRef = ref || defaultRef;

  useEffect(() => {
    resolvedRef.current.indeterminate = indeterminate;
  }, [resolvedRef, indeterminate]);

  return (
    <React.Fragment>
      <CheckBox inputRef={resolvedRef} {...rest} />
    </React.Fragment>
  );
});
const getNoColumnBorder = (noColumnBorder) => (noColumnBorder ? { cellSpacing: "0", cellPadding: "0" } : {});
const Table = ({
  className = "table",
  t,
  data,
  columns,
  getCellProps,
  currentPage = 0,
  pageSizeLimit = 10,
  disableSort = true,
  autoSort = false,
  initSortId = "",
  onSearch = false,
  manualPagination = true,
  totalRecords,
  onNextPage,
  onPrevPage,
  globalSearch,
  onSort = noop,
  onPageSizeChange,
  onLastPage,
  onFirstPage,
  isPaginationRequired = true,
  sortParams = [],
  showAutoSerialNo = false,
  customTableWrapperClassName = "",
  styles = {},
  tableTopComponent,
  tableRef,
  isReportTable = false,
  showCheckBox = false,
  actionLabel = "CS_COMMON_DOWNLOAD",
  tableSelectionHandler = () => {},
  onClickRow = () => {},
  rowClassName = "",
  noColumnBorder = false,
  // customPageSizesArray = null
}) => {
  const {
    getTableProps,
    getTableBodyProps,
    headerGroups,
    rows,
    prepareRow,
    page,
    canPreviousPage,
    canNextPage,
    pageOptions,
    pageCount,
    gotoPage,
    nextPage,
    previousPage,
    setPageSize,
    setGlobalFilter,
    state: { pageIndex, pageSize, sortBy, globalFilter, selectedRowIds },
  } = useTable(
    {
      columns,
      data,
      initialState: { pageIndex: currentPage, pageSize: pageSizeLimit, sortBy: autoSort ? [{ id: initSortId, desc: false }] : sortParams },
      pageCount: totalRecords > 0 ? Math.ceil(totalRecords / pageSizeLimit) : -1,
      manualPagination: manualPagination,
      disableMultiSort: false,
      disableSortBy: disableSort,
      manualSortBy: autoSort ? false : true,
      autoResetPage: false,
      autoResetSortBy: false,
      disableSortRemove: true,
      disableGlobalFilter: onSearch === false ? true : false,
      globalFilter: globalSearch || "text",
      useControlledState: (state) => {
        return React.useMemo(() => ({
          ...state,
          pageIndex: manualPagination ? currentPage : state.pageIndex,
        }));
      },
    },
    useGlobalFilter,
    useSortBy,
    usePagination,
    useRowSelect,
    (hooks) => {
      if (showCheckBox) {
        hooks.visibleColumns.push((columns) => [
          {
            id: "selection",
            Header: ({ getToggleAllPageRowsSelectedProps }) => (
              <div>
                <IndeterminateCheckbox {...getToggleAllPageRowsSelectedProps()} />
              </div>
            ),
            Cell: ({ row }) => (
              <div>
                <IndeterminateCheckbox {...row.getToggleRowSelectedProps()} />
              </div>
            ),
          },
          ...columns,
        ]);
      }
    }
  );
  let isTotalColSpanRendered = false;
  const [toast, setToast] = useState({ show: false, label: "", error: false });

  const iPadMaxWidth = 1024;
  const iPadMinWidth = 768;
  const tableWrapperRef = useRef(null); // Ref for the table wrapper
  const paginationRef = useRef(null); // Ref for the pagination container
  const [isIpadView, setIsIpadView] = React.useState(window.innerWidth <= iPadMaxWidth && window.innerWidth >= iPadMinWidth);

  const onResize = () => {
    if (window.innerWidth >= iPadMinWidth && window.innerWidth <= iPadMaxWidth) {
      if (!isIpadView) {
        setIsIpadView(true);
      }
    } else {
      if (isIpadView) {
        setIsIpadView(false);
      }
    }
  };

  React.useEffect(() => {
    window.addEventListener("resize", () => {
      onResize();
    });
    return () => {
      window.addEventListener("resize", () => {
        onResize();
      });
    };
  });

  useEffect(() => {
    onSort(sortBy);
  }, [onSort, sortBy]);

  useEffect(() => setGlobalFilter(onSearch), [onSearch, setGlobalFilter, data]);

  const handleSelection = async () => {
    const selectedRows = rows?.filter((ele) => Object.keys(selectedRowIds)?.includes(ele?.id));
    const response = await tableSelectionHandler(selectedRows, t);
    setToast({ show: true, label: t(response?.label), error: !response?.isSuccess });
  };

  const handleToastClose = () => {
    setToast({ show: false, label: "", error: false });
  };

  useEffect(() => {
    if (toast?.show) {
      const timer = setTimeout(handleToastClose, 3000);
      return () => clearTimeout(timer);
    }
  }, [toast?.show]);

  //note -> adding data prop in dependency array to trigger filter whenever state of the table changes
  //use case -> without this if we enter string to search and then click on it's attendence checkbox or skill selector for that matter then the global filtering resets and whole table is shown
  return (
    <React.Fragment>
      <div
        style={{
          marginTop: isIpadView ? "210px" : 0,
          marginLeft: isIpadView ? -20 : 0,
        }}
      >
        <div style={{ overflowX: "auto" }} ref={tableWrapperRef}>
          {/* Table */}
          <div style={{ display: "inline-block", minWidth: "100%" }}>
            <table className={className} {...getTableProps()} style={styles} ref={tableRef} {...getNoColumnBorder(noColumnBorder)}>
              <thead>
                {headerGroups.map((headerGroup) => (
                  <tr {...headerGroup.getHeaderGroupProps()}>
                    {showAutoSerialNo && (
                      <th style={{ verticalAlign: "top" }}>
                        {showAutoSerialNo && typeof showAutoSerialNo == "string" ? t(showAutoSerialNo) : t("TB_SNO")}
                      </th>
                    )}
                    {headerGroup.headers.map((column) => (
                      <th
                        key={column.id}
                        {...column.getHeaderProps(column.getSortByToggleProps())}
                        style={
                          column?.id === "selection"
                            ? { minWidth: "100px" }
                            : { verticalAlign: "top", textAlign: `${column?.headerAlign ? column?.headerAlign : "left"}` }
                        }
                      >
                        {column.render("Header")}
                        <span>{column.isSorted ? column.isSortedDesc ? <SortDown /> : <SortUp /> : ""}</span>
                      </th>
                    ))}
                  </tr>
                ))}
              </thead>
              <tbody {...getTableBodyProps()}>
                {page.map((row, i) => {
                  prepareRow(row);
                  return (
                    <tr {...row.getRowProps()} onClick={() => onClickRow(row)} className={rowClassName}>
                      {showAutoSerialNo && <td>{i + 1}</td>}
                      {row.cells.map((cell) => (
                        <td key={cell.column.id} {...cell.getCellProps([getCellProps(cell)])}>
                          {cell.attachment_link ? (
                            <a style={{ color: "#1D70B8" }} href={cell.attachment_link}>
                              {cell.render("Cell")}
                            </a>
                          ) : (
                            <React.Fragment> {cell.render("Cell")} </React.Fragment>
                          )}
                        </td>
                      ))}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
        {isPaginationRequired && (
          <div
            className="pagination dss-white-pre"
            ref={paginationRef}
            style={{
              marginTop: "8px",
              overflow: "hidden",
              width: "0px !important",
            }}
          >
            {`${t("CS_COMMON_ROWS_PER_PAGE")} :`}
            <select
              className="cp"
              value={manualPagination ? pageSizeLimit : pageSize}
              style={{ marginRight: "15px" }}
              onChange={manualPagination ? onPageSizeChange : (e) => setPageSize(Number(e.target.value))}
            >
              {[10, 20, 30, 40, 50].map((pageSize) => (
                <option key={pageSize} value={pageSize}>
                  {pageSize}
                </option>
              ))}
            </select>
            <span>
              <span>
                {pageIndex * pageSize + 1}
                {"-"}
                {manualPagination
                  ? (currentPage + 1) * pageSizeLimit > totalRecords
                    ? totalRecords
                    : (currentPage + 1) * pageSizeLimit
                  : pageIndex * pageSize + page?.length}{" "}
                {/* {(pageIndex + 1) * pageSizeLimit > rows.length ? rows.length : (pageIndex + 1) * pageSizeLimit}{" "} */}
                {totalRecords ? `of ${manualPagination ? totalRecords : rows.length}` : ""}
              </span>{" "}
            </span>
            {/* to go to first and last page we need to do a manual pagination , it can be updated later*/}
            {!manualPagination && pageIndex != 0 && <ArrowToFirst onClick={() => gotoPage(0)} className={"cp"} />}
            {canPreviousPage && manualPagination && onFirstPage && (
              <ArrowToFirst onClick={() => manualPagination && onFirstPage()} className={"cp"} />
            )}
            {canPreviousPage && <ArrowBack onClick={() => (manualPagination ? onPrevPage() : previousPage())} className={"cp"} />}
            {canNextPage && <ArrowForward onClick={() => (manualPagination ? onNextPage() : nextPage())} className={"cp"} />}
            {!manualPagination && pageIndex != pageCount - 1 && <ArrowToLast onClick={() => gotoPage(pageCount - 1)} className={"cp"} />}
            {rows.length == pageSizeLimit && canNextPage && manualPagination && onLastPage && (
              <ArrowToLast onClick={() => manualPagination && onLastPage()} className={"cp"} />
            )}
            {/* to go to first and last page we need to do a manual pagination , it can be updated later*/}
          </div>
        )}
      </div>
      {Object.keys(selectedRowIds)?.length > 0 && (
        <ActionBar className="actionBarWrapper">
          <span style={{ display: "flex" }}>
            <DoubleTickIcon style={{ marginRight: "8px" }} />
            <p className="search-instruction-header" style={{ marginBottom: 0 }}>{`${Object.keys(selectedRowIds)?.length} ${t(
              "COMMON_SELECTED"
            )}`}</p>
          </span>
          <SubmitBar label={t(actionLabel)} onSubmit={handleSelection} />
        </ActionBar>
      )}
      {toast?.show && <Toast label={toast?.label} error={toast?.error} isDleteBtn={true} onClose={handleToastClose}></Toast>}
    </React.Fragment>
  );
};

export default Table;
