import React from "react";
import { Table } from "@selco/digit-ui-react-components";

const RMSPausedTable = ({ t, columns, data, onNextPage, onPrevPage, currentPage, totalRecords, pageSizeLimit, onPageSizeChange }) => {
  return (
    <Table
      t={t}
      data={data}
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
      currentPage={currentPage}
      totalRecords={totalRecords}
      onPageSizeChange={onPageSizeChange}
      pageSizeLimit={pageSizeLimit}
    />
  );
};

export default RMSPausedTable;
