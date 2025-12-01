export const Complaint = {
  create: async ({
    cityCode,
    comments,
    district,
    block,
    uploadedFile,
    complaintType,
    subType,
    systemFunctionality,
    healthcentre,
    tenantId,
  }) => {
    const defaultData = {
      incident: {
        tenantId: tenantId,
        district: district?.name,
        block: block?.name,
        incidentType: complaintType?.key,
        incidentSubtype: subType?.key,
        systemFunctional: systemFunctionality?.key,
        boundaryCode: healthcentre?.code,
        comments: comments,
        additionalDetail: {
          fileStoreId: uploadedFile,
          reopenreason: [],
          rejectReason: [],
          sendBackReason: []
        },
        source: Digit.Utils.browser.isWebview() ? "mobile" : "web",
      },
      workflow: {
        action: "APPLY",
        //: uploadedImages
      },
    };
    if (uploadedFile !== null) {
      defaultData.workflow = {
        ...defaultData.workflow,
        verificationDocuments: uploadedFile.map((file) => ({
          ...file,
          documentType: file.documentType.toLowerCase().startsWith("video")
            ? "VIDEO"
            : file.documentType.toLowerCase().startsWith("image")
            ? "PHOTO"
            : file.documentType,
        })),
      };
    }

    if (Digit.SessionStorage.get("user_type") === "employee") {
      let userInfo = Digit.SessionStorage.get("User");
      defaultData.incident.reporter = {
        uuid: userInfo.info.uuid,
        tenantId: userInfo.info.tenantId,
        // name:reporterName,
        // type: "EMPLOYEE",
        // mobileNumber: mobileNumber,
        // roles: [
        //   {
        //     id: null,
        //     name: "Citizen",
        //     code: "CITIZEN",
        //     tenantId: tenantId,
        //   },
        // ],
      };
    }
    const response = await Digit.PGRService.create(defaultData, cityCode);
    return response;
  },

  assign: async (
    complaintDetails,
    action,
    employeeData,
    comments,
    uploadedDocument,
    tenantId,
    selectedReopenReason,
    selectedRejectReason,
    selectedSendBackReason
  ) => {
    complaintDetails.workflow.action = action;
    complaintDetails.workflow.assignes = employeeData ? [employeeData.uuid] : null;
    complaintDetails.workflow.comments = comments;
    const reasonMap = {
      reopenreason: selectedReopenReason && { value: selectedReopenReason },
      rejectReason: selectedRejectReason && { value: selectedRejectReason?.localizedCode },
      sendBackReason: selectedSendBackReason && {
        value: {
          reason: selectedSendBackReason?.localizedCode
        },
      },
    };

    Object.entries(reasonMap).forEach(([key, data]) => {
      if (data) {
        complaintDetails.workflow[key] = data.value;
        if (!complaintDetails.incident.additionalDetail[key]) {
          complaintDetails.incident.additionalDetail[key] = [];
        }
        complaintDetails.incident.additionalDetail[key].push(data.value);
      }
    });

    uploadedDocument ? (complaintDetails.workflow.verificationDocuments = uploadedDocument) : null;

    if (!uploadedDocument) complaintDetails.workflow.verificationDocuments = [];
    // let userInfo=Digit.SessionStorage.get("User")
    // complaintDetails.incident.reporter = {

    //   uuid:userInfo.info.uuid,
    //   tenantId: userInfo.info.tenantId,
    // };
    //console.log("assignassign",complaintDetails)
    //TODO: get tenant id
    let response;
    try {
      response = await Digit.PGRService.update(complaintDetails, tenantId);
      //return response;
    } catch (error) {
      response = error?.response?.data?.Errors;
    }
    return response;
  },
};
