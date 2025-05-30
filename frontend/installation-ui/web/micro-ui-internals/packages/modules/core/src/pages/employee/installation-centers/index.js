import React, { useState } from "react";
import { Table } from "@egovernments/digit-ui-react-components";
import SearchCentre from "./component/search";

const ComplaintTable = ({ t, columns, data, getCellProps, onNextPage, onPrevPage, currentPage, totalRecords, pageSizeLimit, onPageSizeChange }) => {
  const [centreNameToSearch, setCentreNameToSearch] = useState("");
  const [filteredData, setFilteredData] = useState(data);

  const submitFunc = () => {
    console.log(data);
    setFilteredData(data.filter((row) => row.code.includes(centreNameToSearch)));
  };

  const clearFunc = () => {
    setCentreNameToSearch("");
    setFilteredData(data);
  };

  return (
    <div style={{ marginTop: "75px", width: "95%", marginLeft: "auto", padding: "20px" }}>
      <SearchCentre centreName={centreNameToSearch} setCentreName={setCentreNameToSearch} onSubmit={submitFunc} onClear={clearFunc} />
      <Table
        t={t}
        data={filteredData}
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
};
export default ComplaintTable;
