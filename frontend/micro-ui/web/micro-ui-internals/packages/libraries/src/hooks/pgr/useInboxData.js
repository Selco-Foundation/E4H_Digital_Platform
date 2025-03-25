import { useQuery, useQueryClient } from "react-query";
import React, {useEffect, useState, useRef } from "react";
import { useTranslation } from "react-i18next";

const useInboxData = (searchParams,tenantIdNew) => {
  const { t } = useTranslation();
  let serviceIds = [];
  const inboxTotal=sessionStorage.getItem("inboxTotal");
  let commonFilters = { start: 1, end: 10 };
  const { limit, offset, nearingSLA } = searchParams;
  sessionStorage.setItem("limit", JSON.stringify(limit));
  sessionStorage.setItem("offset", JSON.stringify(offset));
  let appFilters = { ...commonFilters, ...searchParams?.filters?.pgrQuery, ...searchParams?.search, limit, offset, nearingSLA };
  sessionStorage.setItem("appFilters", JSON.stringify(appFilters));
  sessionStorage.setItem("searchParams", JSON.stringify(searchParams));
  let wfFilters
  if(searchParams?.filters?.wfFilters?.assignee?.[0]?.code !=="")
{
    wfFilters = { ...commonFilters, ...searchParams?.filters?.wfQuery,assignee:searchParams?.filters?.wfFilters?.assignee?.[0]?.code}
  }
  else {
    wfFilters = { ...commonFilters, ...searchParams?.filters?.wfQuery}
  }
  const { assignee }=wfFilters;
  const filterData=(data, appFilters)=>{
    //const {phcType, incidentType, incidentId, applicationStatus, start, limit, offset, end}=appFilters;
    const filteredItems= data.data.items;
    const totalItems=data.data.totalCount;
    const statusArray=data.data.statusMap;
    // const sortedItems=filteredItems.sort((a,b)=>{
    //   return b.businessObject?.auditDetails?.lastModifiedTime-a.businessObject?.auditDetails?.lastModifiedTime;
    // })
    //const paginationItems=filteredItems.slice(limit, offset+limit); 
    return {total: totalItems, items:filteredItems, statusarray: statusArray};
  };
  const { data, isLoading, isFetching, isSuccess, refetch } = Digit.Hooks.useNewInboxGeneral({
    tenantId: Digit.ULBService.getCurrentTenantId(),
    ModuleCode: "Incident",
    filters: {  ...appFilters, assignee, sortOrder: "DESC", services: ["Incident"] },
    config: {
      select: (data) => {
        return {data: data} || "-";
      },
      enabled: Digit.Utils.pgrAccess(),
    },


  });
  const filteredData= isSuccess && data ? filterData(data, appFilters) : {total:0, items:[], statusArray:[]};
  const client = useQueryClient();
  const prevSearchParams = useRef(searchParams);
  useEffect(() => {
    if (JSON.stringify(searchParams) !== JSON.stringify(prevSearchParams.current)) {
      refetch(); 
      prevSearchParams.current = searchParams; 
    }
  }, [searchParams, refetch]);

  const fetchInboxData =  () => {
    let tenantId = Digit.ULBService.getCurrentTenantId();
    const tenants = Digit.SessionStorage.get("Tenants")? Digit.SessionStorage.get("Tenants").map(item => item.code).join(','):"";
    const codes = Digit.SessionStorage.get("Tenants")?Digit.SessionStorage.get("Tenants").filter(item => item.code !== "pg")
      .map(item => item.code)
      .join(','):"";
    const sessionTenantId = Digit.SessionStorage.get("Employee.tenantId");
    const isCodePresent = (array, codeToCheck) =>{
      return array.some(item => item.code === codeToCheck);
    }
    const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
    if (searchParams?.filters?.pgrQuery?.phcType) {
      tenantId = searchParams.filters.pgrQuery.phcType;
    } else if (searchParams?.search?.phcType) {
      tenantId = searchParams.search.phcType === "pg" ? tenants : searchParams.search.phcType;
    } else {
      tenantId = sessionTenantId === "pg" ? isCodePresent(userRoles, "COMPLAINT_RESOLVER") ? codes:tenants : sessionTenantId;
    }

    //const tenant =  Digit.SessionStorage.get("Employee.tenantId") == "pg"?  Digit.SessionStorage.get("Tenants").map(item => item.code).join(',') :Digit.SessionStorage.get("Employee.tenantId") 



    let complaintDetailsResponse = null;
    let incidentDetails=null;
    let combinedRes = [];
    if(filteredData!==undefined&& filteredData?.items?.length>0){
      incidentDetails= filteredData.items.map(incident=>incident.businessObject.incident);
    }

    const workflowInstances=filteredData&& filteredData?.items.map(instances=>instances.ProcessInstance)

    if (workflowInstances.length>0) {
      combinedRes = combineResponses(incidentDetails, workflowInstances).map((data) => ({
        ...data,
        sla: data.sla!=="-" ? Math.round(data.sla / (24 * 60 * 60 * 1000)) : "-",
      }));

    }


    return {combinedRes:combinedRes, total:filteredData?.total, statusArray: filteredData?.statusarray};

  };

  const result = fetchInboxData()

  return {data:result};
};

const mapWfBybusinessId = (wfs) => {
  return wfs.reduce((object, item) => {
    let businessId="";
    if(item!==null){
      businessId=item["businessId"];
    }
    return { ...object, [businessId] : item };
  }, {});
};

const combineResponses = (incidentDetails, workflowInstances) => {
  let wfMap = mapWfBybusinessId(workflowInstances);
  let data = [];
  const currentTime=Date.now();
  incidentDetails.map((incident) => {
    if (wfMap?.[incident.incidentId]) {
      data.push({
        incidentId: incident.incidentId,
        incidentType:incident.incidentType,
        incidentSubType: incident.incidentSubType,
        phcType:incident.phcType,
        //priorityLevel : complaint.service.priority,
        //locality: complaint.service.address.locality.code,
        status: incident.applicationStatus,
        taskOwner: wfMap[incident.incidentId]?.assignes?.[0]?.name || "-",
        sla: incident.applicationStatus==="RESOLVED" ? "-" : wfMap[incident.incidentId]?.businesssServiceSla+wfMap[incident.incidentId]?.auditDetails?.createdTime-currentTime,
        tenantId: incident.tenantId,
      })
    }else if (!wfMap?.[incident.incidentId]){
      data.push({
        incidentId: incident.incidentId,
        incidentType:incident.incidentType,
        incidentSubType: incident.incidentSubType,
        phcType:incident.phcType,
        //priorityLevel : complaint.service.priority,
        //locality: complaint.service.address.locality.code,
        status: incident.applicationStatus,
        taskOwner: "-",
        sla: incident.applicationStatus==="RESOLVED" ? "-" : wfMap[incident.incidentId]?.businesssServiceSla+wfMap[incident.incidentId]?.auditDetails?.createdTime-currentTime,
        tenantId: incident.tenantId,
      })
    }});

  return data;
};

export default useInboxData;
