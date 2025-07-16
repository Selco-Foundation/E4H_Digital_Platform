import { useQuery, useQueryClient } from "react-query";

// Enhanced getThumbnails function that can handle both images and videos
const getThumbnails = async (ids, tenantId, documents = []) => {
  const res = await Digit.UploadServices.Filefetch(ids, tenantId);

  if (res.data.fileStoreIds && res.data.fileStoreIds.length !== 0) {
    // Create a Map to store file URLs by fileStoreId
    const urlMap = new Map(res.data.fileStoreIds.map((file) => [file.id, file.url]));

    // Separate images and videos based on documentType (matching by fileStoreId)
    const images = [];
    const videos = new Map();

    documents.forEach((doc) => {
      const fileUrl = urlMap.get(doc.fileStoreId);
      if (fileUrl) {
        if (doc.documentType === "HLS" || doc.documentType.toLowerCase().startsWith("video") || doc.documentType === "VIDEO") {
          const videoKey = doc.documentUid || doc.fileStoreId;

          if (!videos.has(videoKey)) {
            videos.set(videoKey, { master: null, original: null, fileStoreId: null });
          }

          if (doc.documentType === "HLS") {
            videos.get(videoKey).master = Digit.Utils.getFileUrl(fileUrl);
          } else {
            videos.get(videoKey).fileStoreId = doc.fileStoreId;
            videos.get(videoKey).original = Digit.Utils.getFileUrl(fileUrl);
          }
        } else {
          images.push(Digit.Utils.getFileUrl(fileUrl));
        }
      }
    });

    return {
      thumbs: Array.from(urlMap.values()).map((url) => url.split(",")[3] || url.split(",")[0]),
      images,
      videos: Array.from(videos.values()),
    };
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
  CS_ADDCOMPLAINT_HEALTH_CARE_CENTRE: `TENANT_TENANTS_${incident?.phcType?.replace(".", "_").toUpperCase()}`,
  CS_COMPLAINT_COMMENTS: incident?.comments,
  CS_ADDCOMPLAINT_HEALTH_CARE_SUB_TYPE: `${incident?.phcSubType}`,
  CS_COMPLAINT_FILED_DATE: incident.filedDate ? Digit.DateUtils.ConvertEpochToDate(incident.filedDate) : Digit.DateUtils.ConvertEpochToDate(incident.auditDetails.createdTime),
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
    videos: thumbnails?.videos,
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

    // Updated to fetch ALL verification documents, not just PHOTO
    const documentsToFetch = workflow.verificationDocuments || [];
    
    const ids = documentsToFetch.map((doc) => doc.fileStoreId || doc.id);
    
    const state = Digit.ULBService.getStateId();
    const thumbnails = ids.length > 0 ? await getThumbnails(ids, incident.tenantId, documentsToFetch) : null;
    
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
