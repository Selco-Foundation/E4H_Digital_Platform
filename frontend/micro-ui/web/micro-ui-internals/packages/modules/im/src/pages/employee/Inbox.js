import React, { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Loader, Header } from "@selco/digit-ui-react-components";

import DesktopInbox from "../../components/DesktopInbox";
import MobileInbox from "../../components/MobileInbox";
import { Link } from "react-router-dom";

const Inbox = () => {
  const { t } = useTranslation();
  let tenantId = Digit.ULBService.getCurrentTenantId();
  const { uuid } = Digit.UserService.getUser().info;
  const [pageOffset, setPageOffset] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);
  const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
  const {nearingSLA} = Digit.Hooks.useQueryParams();
  const isCodePresent = (array, codeToCheck) =>{
    return array.some(item => item.code === codeToCheck);
  }
  const [searchParams, setSearchParams] = useState({ filters: { wfFilters: { assignee: [{ code: isCodePresent(userRoles, "COMPLAINT_RESOLVER") ? uuid :"" }] } }, search: "", sort: {} });
 
  useEffect(() => {
    (async () => {
      const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
      const applicationStatus = searchParams?.filters?.pgrfilters?.applicationStatus?.map(e => e.code).join(",")
      if(searchParams?.filters?.pgrQuery?.phcType)
      {
        tenantId= searchParams?.filters?.pgrQuery?.phcType
      }
      else if (isCodePresent(userRoles, "COMPLAINT_RESOLVER") && (!searchParams?.filters?.pgrQuery || searchParams?.filters?.pgrfilters?.phcType.length ==0) && Digit.SessionStorage.get("Employee.tenantId") == "pg")
      {
        const codes = Digit.SessionStorage.get("Tenants").filter(item => item.code !== "pg")
        .map(item => item.code)
        .join(',');
        tenantId = codes
      }

      //let response = await Digit.PGRService.count(tenantId, applicationStatus?.length > 0  ? {applicationStatus} : {} );
      // if (response?.count) {
      //   setTotalRecords(response.count);
      // }
    })();
  }, [searchParams]);
  useEffect(() => {
    setPageOffset(0);
  }, [searchParams]);

  const fetchNextPage = () => {
    setPageOffset((prevState) => prevState + 10);
  };

  const fetchPrevPage = () => {
    setPageOffset((prevState) => prevState - 10);
  };

  const handlePageSizeChange = (e) => {
    setPageSize(Number(e.target.value));
  };

  const handleFilterChange = (filterParam) => {
    setSearchParams({ ...searchParams, filters: filterParam });
  };

  const onSearch = (params = "") => {
    setSearchParams({ ...searchParams, search: params });
  };

  // let complaints = Digit.Hooks.pgr.useInboxData(searchParams) || [];
  let tenant=""
  if(searchParams?.search?.phcType)
  {
    tenant = searchParams?.search?.phcType
  }
  let isMobile = Digit.Utils.browser.isMobile();
  const allSearchParams = { ...searchParams, ...(nearingSLA==="1" && {nearingSLA: true})};
  let { data: complaints, isLoading } =isMobile? Digit.Hooks.pgr.useInboxData({...allSearchParams, offset: pageOffset, limit: pageSize  }):Digit.Hooks.pgr.useInboxData({ ...allSearchParams, offset: pageOffset, limit: pageSize }) ;
  useEffect(()=>{
    if(complaints!==undefined && complaints.combinedRes.length!==0){
      const total=complaints.total
      setTotalRecords(total)
    }
  },[totalRecords, complaints]) 
  if (complaints.length!==null) {
    if (isMobile) {
      return (
        <MobileInbox data={complaints} isLoading={isLoading} onFilterChange={handleFilterChange} onSearch={onSearch} searchParams={searchParams} />
      );
    } else {
      return (
        <div>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
          <Header>{t("ES_COMMON_INBOX")}</Header>
          <div style={{color:"#9e1b32", marginBottom:'10px', textAlign:"right", marginRight:"15px"}}>
              <Link to={`/digit-ui/employee`}>{t("CS_COMMON_BACK")}</Link>
          </div> 
          </div>
          <DesktopInbox
            data={complaints}
            isLoading={isLoading}
            onFilterChange={handleFilterChange}
            onSearch={onSearch}
            searchParams={searchParams}
            onNextPage={fetchNextPage}
            onPrevPage={fetchPrevPage}
            onPageSizeChange={handlePageSizeChange}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={totalRecords}
            pageSizeLimit={pageSize}
          />
        </div>
      );
    }
  } else {
    return <Loader />;
  }
};

export default Inbox;
