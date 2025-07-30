import React, { useCallback, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Loader, Header } from "@selco/digit-ui-react-components";

import DesktopInbox from "../../components/DesktopInbox";
import MobileInbox from "../../components/MobileInbox";
import { Link, useHistory, useLocation } from "react-router-dom";

const Inbox = () => {
  const { t } = useTranslation();
  let tenantId = Digit.ULBService.getCurrentTenantId();
  const stateTenantId = Digit.ULBService.getStateId();
  const { uuid } = Digit.UserService.getUser().info;
  const [totalRecords, setTotalRecords] = useState(0);
  const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
  const {nearingSLA} = Digit.Hooks.useQueryParams();
  const isCodePresent = (array, codeToCheck) =>{
    return array.some(item => item.code === codeToCheck);
  }
  const history = useHistory();
  const location = useLocation();
  const queryParams = new URLSearchParams(window.location.search);
  const [searchParams, setSearchParams] = useState((() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : null;
    } catch (error) {
      console.error("Failed to parse filter parameter:", error);
      return null;
    }
  }) || { filters: { wfFilters: { assignee: [{ code: isCodePresent(userRoles, "COMPLAINT_RESOLVER") ? uuid :"" }] } }, search: "", sort: {} });
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const prevSearchParamsRef = useRef(JSON.stringify(searchParams));
  const prevPageSizeRef = useRef(pageSize);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(searchParams)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
    });

    (async () => {
      const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
      const applicationStatus = searchParams?.filters?.pgrfilters?.applicationStatus?.map(e => e.code).join(",")
      if(searchParams?.filters?.pgrQuery?.phcType)
      {
        tenantId= searchParams?.filters?.pgrQuery?.phcType
      }
      else if (isCodePresent(userRoles, "COMPLAINT_RESOLVER") && (!searchParams?.filters?.pgrQuery || searchParams?.filters?.pgrfilters?.phcType.length ==0) && Digit.SessionStorage.get("Employee.tenantId") == stateTenantId)
      {
        const codes = Digit.SessionStorage.get("Tenants").filter(item => item.code !== stateTenantId)
        .map(item => item.code)
        .join(',');
        tenantId = codes
      }

      //let response = await Digit.PGRService.count(tenantId, applicationStatus?.length > 0  ? {applicationStatus} : {} );
      // if (response?.count) {
      //   setTotalRecords(response.count);
      // }
    })();
  }, [searchParams, pageSize, pageOffset]);

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(searchParams);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [searchParams, pageSize]);

  const fetchNextPage = () => {
    setPageOffset((prevState) => prevState + pageSize);
  };

  const fetchPrevPage = () => {
    setPageOffset((prevState) => prevState - pageSize);
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
              <Link to={`/${window.contextPath}/employee`}>{t("CS_COMMON_BACK")}</Link>
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
