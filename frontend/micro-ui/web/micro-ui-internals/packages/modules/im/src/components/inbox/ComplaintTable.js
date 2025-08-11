import React from "react";
import { Table } from "@selco/digit-ui-react-components";

const ComplaintTable = ({ t, columns, data, getCellProps, onNextPage, onPrevPage, currentPage, totalRecords, pageSizeLimit, onPageSizeChange }) => (
  
 <div>
   <style>
        {`
          .table thead th:first-child {
            min-width: 0px;
          }
        
          .table thead th:nth-child(2) {
            min-width: 0px;
          }
        `}
      </style>

  <Table
    t={t}
    data={data}
    columns={columns}
    getCellProps={getCellProps}
    onNextPage={onNextPage}
    onPrevPage={onPrevPage}
    currentPage={currentPage}
    totalRecords={totalRecords}
    onPageSizeChange={onPageSizeChange}
    pageSizeLimit={pageSizeLimit}
  />
  </div>
   
);
console.log("jhisss", ComplaintTable)
export default ComplaintTable;
