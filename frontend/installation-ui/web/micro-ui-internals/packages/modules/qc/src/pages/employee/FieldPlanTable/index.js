import React, { useEffect, useState } from "react";
import { Table } from "@egovernments/digit-ui-react-components";
import SearchCentre from "./component/search";
import { QCService } from "../Service/QCService";

const FieldPlanTable = ({ t, columns, data, getCellProps, onNextPage, onPrevPage, currentPage, totalRecords, pageSizeLimit, onPageSizeChange }) => {
  const [centreNameToSearch, setCentreNameToSearch] = useState("");
  const [fetchedData, setData] = useState([]);
  const [filteredData, setFilteredData] = useState(data);
  const userInfo = Digit.UserService.getUser();

  const submitFunc = () => {
    console.log(data);
    setFilteredData(data.filter((row) => row.code.includes(centreNameToSearch)));
  };

  const clearFunc = () => {
    setCentreNameToSearch("");
    setFilteredData(data);
  };

  useEffect(async () => {
    const fetchData = async () => {
      const response = await QCService.fetchFieldPlans(userInfo, userInfo?.access_token);
      setData(response);
    }

    // await fetchData().then(() => {
    //   console.log(fetchedData);
    // });
  }, []);

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        Inbox
      </div>
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

export default FieldPlanTable;
