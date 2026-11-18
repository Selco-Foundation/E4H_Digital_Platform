import { useEffect, useState } from "react";
import useComplaintStatus from "./useComplaintStatus";

const useComplaintStatusCount = (complaints,tenant) => {
  const [complaintStatusWithCount, setcomplaintStatusWithCount] = useState([]);
  let complaintStatus = useComplaintStatus();
  let tenantId = Digit.ULBService.getCurrentTenantId();
  const [statusCount, setStatusCount]=useState();
  const appFilters=JSON.parse(sessionStorage.getItem("appFilters"));
  const searchParams=JSON.parse(sessionStorage.getItem("searchParams"));
  // Read the same flag the list reads, so the sidebar counts follow the toggle. This used to
  // rebuild an assignee out of sessionStorage, which meant the counts and the list could disagree.
  const assignedToMe = searchParams?.filters?.wfFilters?.assignedToMe !== false;
  const { limit, offset, incidentType, phcType, applicationStatus }=appFilters;
    const { data, isLoading, isFetching, isSuccess } = Digit.Hooks.useNewInboxGeneral({
      tenantId: Digit.ULBService.getCurrentTenantId(),
      ModuleCode: "Incident",
      filters: { ...appFilters, assignedToMe, limit: limit, offset: offset,sortOrder: "DESC", services: ["Incident"]},
      config: {
        select: (data) => {
          return data;
        },
        enabled: Digit.Utils.pgrAccess(),
      },  
    });
        useEffect(() => { 
        if(data && data.items && data.items.length>0 && data.statusMap){
          const counts=data.statusMap.reduce((acc, item)=>{
              const status=item.statusid;
              if(status){
                acc[status]=item.count;
              }
              return acc; 
            },{});
            setStatusCount(counts);
        }
        else{
          setStatusCount("-")
        }
      }, [data]);
  useEffect(() => {
    const getStatusWithCount = async () => {
        let statusWithCount = complaintStatus.map(async (status) => {
          const count=statusCount[status.code]||0;
          return{
            ...status,
            count: count,
          }
        });
        setcomplaintStatusWithCount(await Promise.all(statusWithCount));
      }
    if(complaintStatus.length>0 && statusCount!==undefined){
      getStatusWithCount();
    };
  }, [complaints, complaintStatus, statusCount]);
  return complaintStatusWithCount;
};

export default useComplaintStatusCount;