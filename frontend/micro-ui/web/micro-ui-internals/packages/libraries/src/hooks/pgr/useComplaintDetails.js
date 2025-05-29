import { useQuery, useQueryClient } from "react-query";

// TODO: move to service
const getThumbnails = async (ids, tenantId) => {
  const res = await Digit.UploadServices.Filefetch(ids, tenantId);
  if (res.data.fileStoreIds && res.data.fileStoreIds.length !== 0) {
    return { thumbs: res.data.fileStoreIds.map((o) => o.url.split(",")[3]), images: res.data.fileStoreIds.map((o) => Digit.Utils.getFileUrl(o.url)) };
  } else {
    return null;
  }
};

const getDetailsRow = ({ id, incident, complaintType }) => ({
  CS_COMPLAINT_DETAILS_TICKET_NO: id,
  CS_COMPLAINT_DETAILS_APPLICATION_STATUS: `CS_COMMON_${incident.applicationStatus}`,
  CS_ADDCOMPLAINT_TICKET_TYPE: `SERVICEDEFS.${incident.incidentType.toUpperCase()}`,
  CS_ADDCOMPLAINT_TICKET_SUB_TYPE: `SERVICEDEFS.${incident.incidentSubType.toUpperCase()}`,
  CS_ADDCOMPLAINT_SYSTEM_FUNCTIONAL: incident.systemFunctional != null ? incident.systemFunctional : "",
  CS_ADDCOMPLAINT_DISTRICT: `${incident.district}`,
  CS_ADDCOMPLAINT_BLOCK: `${incident?.block}`,
  CS_ADDCOMPLAINT_HEALTH_CARE_CENTRE: `${incident?.phcType}`,
  CS_COMPLAINT_COMMENTS: incident?.comments,
  CS_ADDCOMPLAINT_HEALTH_CARE_SUB_TYPE: `${incident?.phcSubType}`,
  CS_COMPLAINT_FILED_DATE: Digit.DateUtils.ConvertEpochToDate(incident.auditDetails.createdTime),
})

const isEmptyOrNull = (obj) => obj === undefined || obj === null || Object.keys(obj).length === 0;

const transformDetails = ({ id, incident, workflow, thumbnails, complaintType }) => {
  const { Customizations, SessionStorage } = window.Digit;
  const role = (SessionStorage.get("user_type") || "CITIZEN").toUpperCase();
  const customDetails = Customizations?.PGR?.getComplaintDetailsTableRows
    ? Customizations.PGR.getComplaintDetailsTableRows({ id, incident, role })
    : {};
  return {
    details: !isEmptyOrNull(customDetails) ? customDetails : getDetailsRow({ id, incident, complaintType }),
    thumbnails: thumbnails?.thumbs,
    images: thumbnails?.images,
    workflow: workflow,
    incident: incident,
    audit: {
      details: incident.auditDetails,
      incidentType: incident.incidentSubType,
    },
    // service: service,
  };
};

const fetchComplaintDetails = async (tenantIdNew, id) => {

  let tenantId = window.location.href.split("/")[9]
  console.log("servkkkk", tenantId, id)
  var serviceDefs = await Digit.MDMSService.getServiceDefs(tenantId, "Incident");
  const { incident, workflow } = (await Digit.PGRService.search(tenantId, { incidentId: window.location.href.split("/")[8] })).IncidentWrappers[0];
  //console.log("service", service)
  //const workflow=await Digit.PGRService.search(tenantId, {incidentId: id }).IncidentWrappers[0];
  Digit.SessionStorage.set("complaintDetails", { incident, workflow });
  if (incident && workflow && serviceDefs) {
    //const complaintType =  service.incident.incidentType
    const complaintType = serviceDefs.filter((def) => def.serviceCode === incident.incidentSubType)[0].menuPath.toUpperCase();

    const ids = workflow.verificationDocuments
      ? workflow.verificationDocuments.filter((doc) => doc.documentType === "PHOTO").map((photo) => photo.fileStoreId || photo.id)
      : null;
    const state = Digit.ULBService.getStateId();
    const thumbnails = ids ? await getThumbnails(ids, incident.tenantId) : null;
    const details = transformDetails({ id, incident, workflow, thumbnails, complaintType });
    return details;
  } else {
    return {};
  }
};

const useComplaintDetails = ({ tenantId, id }) => {

  const queryClient = useQueryClient();
  const { isLoading, error, data } = useQuery(["complaintDetails", tenantId, id], () => fetchComplaintDetails(tenantId, id));

  return { isLoading, error, complaintDetails: data, revalidate: () => queryClient.invalidateQueries(["complaintDetails", tenantId, id]) };
};

export default useComplaintDetails;