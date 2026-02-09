import React, { useState, useEffect, useCallback, Fragment } from "react";
import { useHistory, useParams } from "react-router-dom";
import {
  BreakLine,
  Card,
  CardLabel,
  CardLabelDesc,
  CardSubHeader,
  ConnectingCheckPoints,
  CheckPoint,
  DisplayPhotos,
  MediaRow,
  LastRow,
  Row,
  StatusTable,
  PopUp,
  HeaderBar,
  ImageViewer,
  TextInput,
  TextArea,
  UploadFile,
  ButtonSelector,
  Toast,
  ActionBar,
  Menu,
  SubmitBar,
  Dropdown,
  Loader,
  LinkButton,
  Modal,
  SectionalDropdown,
  ImageUploadHandler,
  MultiUploadWrapper,
  RadioButtons,
} from "@selco/digit-ui-react-components";
import { Link, useLocation } from "react-router-dom";

import { Close } from "../../Icons";
import { useTranslation } from "react-i18next";
import { isError, useQueryClient } from "react-query";
import StarRated from "../../components/timelineInstances/StarRated";
import _ from "lodash";

const MapView = (props) => {
  return (
    <div onClick={props.onClick}>
      <img src="https://via.placeholder.com/640x280" />
    </div>
  );
};

const Heading = (props) => {
  return <h1 className="heading-m">{props.label}</h1>;
};

const CloseBtn = (props) => {
  return (
    <div className="icon-bg-secondary" onClick={props.onClick}>
      <Close />
    </div>
  );
};

const TLCaption = ({ data, comments }) => {
  const { t } = useTranslation();
  return (
    <div>
      {data?.date && <p>{data?.date}</p>}
      <p>{data?.name}</p>
      <p>{data?.mobileNumber}</p>
      {data?.source && <p>{t("ES_COMMON_FILED_VIA_" + data?.source.toUpperCase())}</p>}
      {comments?.map((e, index) => (
        <div key={index} className="TLComments">
          <h3>{t("WF_COMMON_COMMENTS")}</h3>
          <p style={{ overflowX: "scroll" }}>{e}</p>
        </div>
      ))}
    </div>
  );
};

const ComplaintDetailsModal = ({ workflowDetails, complaintDetails, close, popup, selectedAction, onAssign, tenant, t }) => {
  // RAIN-5692 PGR : GRO is assigning complaint, Selecting employee and assign. Its not getting assigned.
  // Fix for next action  assignee dropdown issue
  const stateArray = workflowDetails?.data?.initialActionState?.nextActions?.filter((ele) => ele?.action == selectedAction);
  const useEmployeeData = Digit.Hooks.pgr.useEmployeeFilter(
    tenant,
    stateArray?.[0]?.assigneeRoles?.length > 0 ? stateArray?.[0]?.assigneeRoles?.join(",") : "",
    complaintDetails,
    complaintDetails?.incident?.boundaryCode
  );
  const employeeData = useEmployeeData
    ? useEmployeeData.map((departmentData) => {
        return { heading: departmentData.department, options: departmentData.employees };
      })
    : null;

  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [comments, setComments] = useState("");
  const [file, setFile] = useState(null);
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [uploadedFile, setUploadedFile] = useState(Array);
  const allowedFileTypes = /(docx|pdf|jpg|xlsx)$/i;
  const stateId = Digit.ULBService.getStateId();
  const [uploadedImages, setUploadedImagesIds] = useState(null);
  //const [uploadedFile, setUploadedFile] = useState(null);
  const [error, setError] = useState(null);
  const cityDetails = Digit.ULBService.getCurrentUlb();
  const [selectedReopenReason, setSelectedReopenReason] = useState(null);
  const [selectedRejectReason, setSelectedRejectReason] = useState(null);
  const [selectedSendBackReason, setSelectedSendBackReason] = useState(null);
  const [selectedOutOfScopeReason, setSelectedOutOfScopeReason] = useState(null);
  const state = Digit.ULBService.getStateId();
  const reopenReasonMenu = [t(`CS_REOPEN_OPTION_ONE`), t(`CS_REOPEN_OPTION_TWO`), t(`CS_REOPEN_OPTION_THREE`), t(`CS_REOPEN_OPTION_FOUR`), t(`CS_REOPEN_OPTION_FIVE`)];
  const { isMdmsLoading, data: rejectSendBackOutOfScopeReasons } = Digit.Hooks.pgr.useMDMS(state, "Incident", [
    "RejectReasons",
    "SendBackReasons",
    "OutOfScopeReasons",
  ]);
  const [dataState, setDataState] = useState({ newArr: [], mappedArray: [] });
  const [oowIssue, setOowIssue] = useState(null);
  const [oowRootCause, setOowRootCause] = useState(null);
  const [oowRecommendedSolution, setOowRecommendedSolution] = useState(null);
  const [oowTotalCostOfSolution, setOowTotalCostOfSolution] = useState(null);
  const [oowTimeToResolve, setOowTimeToResolve] = useState(null);

  useEffect(() => {
    if (selectedAction === "REJECT") {
      const uuid = JSON.parse(sessionStorage.getItem("Digit.User"))?.value?.info?.uuid;
      let name = JSON.parse(sessionStorage.getItem("Digit.User"))?.value?.info?.name;
      setSelectedEmployee({ name, uuid });
    }
  }, [selectedAction]);

  function onSelectEmployee(employee) {
    setSelectedEmployee(employee);
  }

  function addComment(e) {
    if (e.target.value.length > 256) {
      setError(t("CS_COMMENT_LENGTH_LIMIT_EXCEED"));
    }
    // else if(!/^[a-zA-Z0-9\s./,]*$/.test(e.target.value)){
    //   setError(t("CS_COMMENT_INVALID_CHARACTERS"))
    // }
    else {
      setError(null);
      setComments(e.target.value);
    }
  }

  function addOOWResponses(e, setField) {
    if (e.target.value.length > 256) {
      setError(t("CS_TEXT_LENGTH_LIMIT_EXCEED"));
    } else {
      setError(null);
      setField(e.target.value);
    }
  }

  function onSelectReopenReason(reason) {
    setSelectedReopenReason(reason);
  }
  function onSelectRejectReason(reason) {
    setSelectedRejectReason(reason);
  }
  function onSelectSendBackReason(reason) {
    setSelectedSendBackReason(reason);
    setComments("");
  }
  function onSelectOutOfScopeReason(reason) {
    setSelectedOutOfScopeReason(reason);
    setComments("");
  }
  const clearError = useCallback(() => {
    setError("");
  }, []);
  useEffect(() => {
    if (error) {
      const timeOut = setTimeout(() => {
        clearError();
      }, 1000);
      return () => clearTimeout(timeOut);
    }
  }, [error, clearError]);
  function selectfile(e, newArr) {
    let file = [];
    if (e) {
      if (newArr.length > 0) {
        file = newArr.map((e) => {
          const newFile = {
            documentType: e?.file?.type.includes(".sheet") ? ".xlsx" : e?.file?.type.includes(".document") ? ".docs" : e?.file?.type,
            fileStoreId: e?.fileStoreId?.fileStoreId,
            documentUid: "",
            additionalDetails: {},
          };
          return newFile;
        });
      }
      // const newFile={
      // documentType: e?.file?.type.includes(".sheet") ? ".xlsx": e?.file?.type.includes(".document")? ".docs": e?.file?.type,
      // fileStoreId: e?.fileStoreId?.fileStoreId,
      // documentUid: "",
      // additionalDetails: {},
      // };

      let temp = [...uploadedFile, ...file];

      const filterFileStoreIds = newArr.map((item) => item.fileStoreId.fileStoreId);

      // Use a Set to remove duplicates and filter the documents array
      const seen = new Set();
      const filteredDocuments = temp.filter((document) => {
        if (filterFileStoreIds.includes(document.fileStoreId) && !seen.has(document.fileStoreId)) {
          seen.add(document.fileStoreId);
          return true;
        }
        return false;
      });

      setUploadedFile(filteredDocuments);
      e && setFile(e.file);
    }
  }
  useEffect(() => {
    if (dataState.newArr && dataState.mappedArray) {
      selectfile(dataState.newArr, dataState.mappedArray);
    }
  }, [dataState]);

  const getData = (state) => {
    let data = Object.fromEntries(state);
    const mappedArray = state.map((item) => {
      return item[1];
    });
    let newArr = Object.values(data);
    setDataState({ newArr, mappedArray });
  };
  return (
    <Modal
      headerBarMain={
        <Heading
          label={
            selectedAction === "ASSIGN" || selectedAction === "REASSIGN"
              ? t("CS_ACTION_ASSIGN_TICKET")
              : selectedAction === "REJECT"
              ? t("CS_ACTION_DECLINE_TICKET")
              : selectedAction === "REOPEN"
              ? t("CS_COMMON_REOPEN")
              : selectedAction === "RESOLVE"
              ? t("CS_COMMON_RESOLVE")
              : selectedAction === "CLOSE"
              ? t("CS_COMMON_CLOSE")
              : selectedAction === "SENDBACK"
              ? t("CS_COMMON_SENDbACK")
              : selectedAction === "OUT_OF_WARRANTY"
              ? t("CS_COMMON_OUT_OF_WARRANTY")
              : selectedAction === "APPROVE"
              ? t("CS_COMMON_APPROVE")
              : selectedAction === "REVISE"
              ? t("CS_COMMON_REVISE")
              : selectedAction === "MARK_OUT_OF_SCOPE"
              ? t("CS_COMMON_MARK_OUT_OF_SCOPE")
              : selectedAction === "STATUS_UPDATE"
              ? t("CS_COMMON_STATUS_UPDATE")
              : t("CS_COMMON_SPARE_PART_NEEDED")
          }
        />
      }
      headerBarEnd={<CloseBtn onClick={() => close(popup)} />}
      actionCancelLabel={t("CS_COMMON_CANCEL")}
      actionCancelOnSubmit={() => close(popup)}
      actionSaveLabel={
        selectedAction === "ASSIGN" || selectedAction === "REASSIGN"
          ? t("CS_COMMON_ASSIGN")
          : selectedAction === "REJECT"
          ? t("CS_COMMON_DECLINE")
          : selectedAction === "REOPEN"
          ? t("CS_ACTION_REOPEN")
          : selectedAction === "RESOLVE"
          ? t("CS_COMMON_RESOLVE_BUTTON")
          : selectedAction === "CLOSE"
          ? t("CS_COMMON_CLOSE")
          : selectedAction === "SENDBACK"
          ? t("CS_COMMON_SENDbACK")
          : selectedAction === "OUT_OF_WARRANTY"
          ? t("CS_COMMON_OUT_OF_WARRANTY")
          : selectedAction === "APPROVE"
          ? t("CS_COMMON_APPROVE")
          : selectedAction === "REVISE"
          ? t("CS_COMMON_REVISE")
          : selectedAction === "MARK_OUT_OF_SCOPE"
          ? t("CS_COMMON_MARK_OUT_OF_SCOPE")
          : selectedAction === "STATUS_UPDATE"
          ? t("CS_COMMON_STATUS_UPDATE")
          : t("CS_COMMON_SPARE_PART_NEEDED")
      }
      actionSaveOnSubmit={() => {
        const isTextareaAction =
          ["SENDBACK", "REJECT", "MARK_OUT_OF_SCOPE"].includes(selectedAction) &&
          (selectedAction === "MARK_OUT_OF_SCOPE"
            ? selectedOutOfScopeReason.additionalInputs?.[0].type === "textarea"
            : selectedAction === "SENDBACK"
            ? selectedSendBackReason?.additionalInputs?.[0].type === "textarea"
            : selectedRejectReason?.additionalInputs?.[0].type === "textarea");

        const isCommentsMandatory =
          (isTextareaAction ||
            selectedAction === "RESOLVE" ||
            selectedAction === "SPARE_PART_NEEDED" ||
            selectedAction === "REVISE" ||
            selectedAction === "STATUS_UPDATE") &&
          !comments.trim();

        const oowMandateCondition = selectedAction === "OUT_OF_WARRANTY" && !(oowIssue && oowRootCause && oowRecommendedSolution && oowTimeToResolve);

        const validations = [
          { condition: selectedAction === "REJECT" && !selectedRejectReason, message: "CS_MANDATORY_DECLINE_REASON" },
          { condition: selectedAction === "SENDBACK" && !selectedSendBackReason, message: "CS_MANDATORY_SENDBACK_REASON" },
          { condition: isCommentsMandatory, message: "CS_MANDATORY_COMMENTS" },
          { condition: selectedAction === "REOPEN" && selectedReopenReason === null, message: "CS_REOPEN_REASON_MANDATORY" },
          { condition: selectedAction === "ASSIGN" && selectedEmployee === null, message: "CS_ASSIGNEE_MANDATORY" },
          { condition: oowMandateCondition, message: "ES_COMMON_PLEASE_ENTER_ALL_MANDATORY_FIELDS" },
          { condition: selectedAction === "OUT_OF_WARRANTY" && !oowTotalCostOfSolution, message: "CS_SOLUTION_COSE_MANDATORY" },
          {
            condition:
              (selectedAction === "RESOLVE" || selectedAction === "OUT_OF_WARRANTY" || selectedAction === "STATUS_UPDATE") &&
              uploadedFile.length === 0,
            message: "CS_MANDATORY_FILE_UPLOAD",
          },
          { condition: selectedAction === "REVISE" && comments?.length < 50, message: "CS_REVISE_ACTION_COMMENT_LENGTH" },
        ];

        const error = validations.find(({ condition }) => condition);
        if (error) {
          setError(t(error.message));
          return;
        }

        const oowResponses = {
          oowIssue,
          oowRootCause,
          oowRecommendedSolution,
          oowTimeToResolve,
          oowTotalCostOfSolution,
        };

        onAssign(
          selectedEmployee,
          comments,
          uploadedFile,
          selectedReopenReason,
          selectedRejectReason,
          selectedSendBackReason,
          oowResponses,
          selectedOutOfScopeReason
        );
      }}
      error={error}
      setError={setError}
    >
      <Card style={{ paddingTop: "0px", maxHeight: "50vh", overflow: "auto", boxShadow: "none" }}>
        {selectedAction === "REJECT" ? (
          <React.Fragment>
            <CardLabel>{t("CS_DECLINE_COMPLAINT")}*</CardLabel>
            <Dropdown
              selected={selectedRejectReason}
              option={rejectSendBackOutOfScopeReasons?.Incident?.RejectReasons?.map((reason) => ({
                ...reason,
                localizedCode: t(reason.code), // Use localized text if available, otherwise fallback to default name
              }))}
              optionKey={"localizedCode"}
              select={onSelectRejectReason}
            />
          </React.Fragment>
        ) : null}

        {selectedAction === "SENDBACK" ? (
          <React.Fragment>
            <CardLabel>{t("CS_SENDBACK_COMPLAINT")}*</CardLabel>
            <Dropdown
              selected={selectedSendBackReason}
              option={rejectSendBackOutOfScopeReasons?.Incident?.SendBackReasons?.map((reason) => ({
                ...reason,
                localizedCode: t(reason.code), // Use localized text if available, otherwise fallback to default name
              }))}
              optionKey={"localizedCode"}
              select={onSelectSendBackReason}
            />
          </React.Fragment>
        ) : null}

        {selectedAction === "MARK_OUT_OF_SCOPE" ? (
          <React.Fragment>
            <CardLabel>{t("CS_MARK_OUT_OF_SCOPE_COMPLAINT")}*</CardLabel>
            <Dropdown
              selected={selectedSendBackReason}
              option={rejectSendBackOutOfScopeReasons?.Incident?.OutOfScopeReasons?.map((reason) => ({
                ...reason,
                localizedCode: t(reason.code), // Use localized text if available, otherwise fallback to default name
              }))}
              optionKey={"localizedCode"}
              select={onSelectOutOfScopeReason}
            />
          </React.Fragment>
        ) : null}

        {selectedAction === "ASSIGN" || selectedAction === "REASSIGN" ? (
          <React.Fragment>
            <CardLabel>{t("CS_COMMON_EMPLOYEE_NAME")}*</CardLabel>

            {employeeData && (
              <Dropdown
                option={employeeData?.[0]?.options}
                optionKey="name"
                id="employee"
                selected={selectedEmployee}
                select={onSelectEmployee}
                required={true}
              />
            )}
          </React.Fragment>
        ) : null}
        {selectedAction === "REOPEN" ? (
          <React.Fragment>
            <CardLabel>{t("CS_REOPEN_COMPLAINT")}*</CardLabel>
            <Dropdown selected={selectedReopenReason} option={reopenReasonMenu} select={onSelectReopenReason} />
          </React.Fragment>
        ) : null}
        {(selectedAction !== "SENDBACK" && selectedAction !== "MARK_OUT_OF_SCOPE" && selectedAction !== "OUT_OF_WARRANTY") ||
        selectedSendBackReason?.additionalInputs?.[0].type === "textarea" ||
        selectedOutOfScopeReason?.additionalInputs?.[0].type === "textarea" ? (
          <>
            {selectedAction !== "ASSIGN" &&
            selectedAction !== "REOPEN" &&
            selectedAction !== "APPROVE" &&
            !(selectedAction === "REJECT" && selectedRejectReason?.additionalInputs?.[0].type !== "textarea") ? (
              <CardLabel>{t("CS_COMMON_EMPLOYEE_COMMENTS")}*</CardLabel>
            ) : (
              <CardLabel>{t("CS_COMMON_EMPLOYEE_COMMENTS")}</CardLabel>
            )}
            <TextArea name="comment" onChange={addComment} value={comments} />
          </>
        ) : null}
        {selectedAction === "OUT_OF_WARRANTY" && (
          <React.Fragment>
            <CardLabel>{t("OOW_ACTION_ISSUE_OBSERVATION")}*</CardLabel>
            <TextArea name="oowIssue" onChange={(e) => addOOWResponses(e, setOowIssue)} value={oowIssue} />
            <CardLabel>{t("OOW_ACTION_ISSUE_ROOT_CAUSE")}*</CardLabel>
            <TextArea name="oowRootCause" onChange={(e) => addOOWResponses(e, setOowRootCause)} value={oowRootCause} />
            <CardLabel>{t("OOW_ACTION_ISSUE_SOLUTION")}*</CardLabel>
            <TextArea name="oowRecommendedSolution" onChange={(e) => addOOWResponses(e, setOowRecommendedSolution)} value={oowRecommendedSolution} />
            <CardLabel>{t("OOW_ACTION_ISSUE_RESOLUTION_TIME")}*</CardLabel>
            <TextInput t={t} type={"text"} onChange={(e) => addOOWResponses(e, setOowTimeToResolve)} value={oowTimeToResolve} />
            <CardLabel>{t("OOW_ACTION_ISSUE_SOLUTION_COST")}*</CardLabel>
            <TextInput t={t} type={"number"} onChange={(e) => addOOWResponses(e, setOowTotalCostOfSolution)} value={oowTotalCostOfSolution} />
          </React.Fragment>
        )}
        {selectedAction === "OUT_OF_WARRANTY" ? (
          <CardLabel>{t("CS_ACTION_QUOTATION_DOCUMENT")}*</CardLabel>
        ) : (selectedAction === "RESOLVE" || selectedAction === "STATUS_UPDATE") ? (
          <CardLabel>{t("CS_ACTION_SUPPORTING_DOCUMENTS")}*</CardLabel>
        ) : (
          <CardLabel>{t("CS_ACTION_SUPPORTING_DOCUMENTS")}</CardLabel>
        )}

        <MultiUploadWrapper
          t={t}
          module="Incident"
          tenantId={complaintDetails?.incident?.tenantId || tenantId}
          requestSpecifcFileRemoval={uploadedFile?.[0]}
          getFormState={(e) => getData(e)}
          allowedFileTypesRegex={
            selectedAction === "OUT_OF_WARRANTY"
              ? /(pdf)$/i
              : selectedAction === "RESOLVE"
              ? /(docx|doc|pdf|xlsx|jpeg|png)$/i
              : /(pdf|jpg|jpeg|png)$/i
          }
          allowedMaxSizeInMB={5}
          acceptFiles={
            selectedAction === "OUT_OF_WARRANTY"
              ? ".pdf"
              : selectedAction === "RESOLVE"
              ? ".pdf, .xlsx, .docx, .doc, .jpeg, .png"
              : ".pdf, .jpg, .jpeg, .png"
          }
          ulb={complaintDetails?.incident?.tenantId || tenantId}
          analyticsPage="ticket_details_page"
        />
        {selectedAction === "OUT_OF_WARRANTY" ? (
          <CardLabelDesc style={{ marginTop: "8px", fontSize: "13px" }}> {t("CS_OOW_FILE_DESC")}</CardLabelDesc>
        ) : selectedAction === "RESOLVE" ? (
          <div style={{ marginTop: "6px", fontSize: "13px", color: "#36454F" }}>{t("RESOLVE_RESOLUTION_REPORT")}</div>
        ) : (
          <CardLabelDesc style={{ marginTop: "8px", fontSize: "13px" }}> {t("CS_FILE_LIMIT")}</CardLabelDesc>
        )}
      </Card>
    </Modal>
  );
};

export const ComplaintDetails = (props) => {
  let { incidentId, tenantId } = useParams();
  const { t } = useTranslation();
  const [fullscreen, setFullscreen] = useState(false);
  const [imageZoom, setImageZoom] = useState(null);
  const mobileDeviceWidth = 780;
  const iPadMaxWidth = 1024;
  const iPadMinWidth = 768;
  const isMobile = window.Digit.Utils.browser.isMobile();
  const location = useLocation();
  const history = useHistory();
  const [isIpadView, setIsIpadView] = React.useState(window.innerWidth <= iPadMaxWidth && window.innerWidth >= iPadMinWidth);
  const onResize = () => {
    if (window.innerWidth <= iPadMaxWidth && window.innerWidth >= iPadMinWidth) {
      setIsIpadView(true);
    } else {
      setIsIpadView(false);
    }
  };
  React.useEffect(() => {
    window.addEventListener("resize", () => {
      onResize();
    });
    return () => {
      window.addEventListener("resize", () => {
        onResize();
      });
    };
  }, []);
  const [toast, setToast] = useState(false);
  const [error, setError] = useState("");
  const stateTenantId = Digit.ULBService.getStateId();
  const tenant =
    Digit.SessionStorage.get("Employee.tenantId") === stateTenantId
      ? Digit.SessionStorage.get("IM_TENANTS")
          .map((item) => item.code)
          .join(",")
      : Digit.SessionStorage.get("Employee.tenantId");

  const { isLoading, complaintDetails, revalidate: revalidateComplaintDetails } = Digit.Hooks.pgr.useComplaintDetails({
    tenantId: tenant,
    id: incidentId,
  });

  const workflowDetails = Digit.Hooks.useWorkflowDetails({
    tenantId: tenantId,
    id: incidentId,
    moduleCode: "Incident",
    role: "EMPLOYEE",
  });
  let currentLoginUser = JSON.parse(sessionStorage.getItem("Digit.User"))?.value?.info?.userName;

  const [imagesToShowBelowComplaintDetails, setImagesToShowBelowComplaintDetails] = useState([]);

  // RAIN-5692 PGR : GRO is assigning complaint, Selecting employee and assign. Its not getting assigned.
  // Fix for next action  assignee dropdown issue
  if (workflowDetails && workflowDetails?.data) {
    workflowDetails.data.initialActionState = workflowDetails?.data?.initialActionState || { ...workflowDetails?.data?.actionState } || {};
    workflowDetails.data.actionState = { ...workflowDetails.data };
  }
  if (complaintDetails) {
    complaintDetails.details.CS_COMPLAINT_DETAILS_TICKET_NO = complaintDetails?.details?.CS_COMPLAINT_DETAILS_TICKET_NO.split("/")[0];
  }

  const timeline = workflowDetails?.data?.timeline || [];

  let filteredTimeline = timeline;

  // check if timeline has a CLOSEDAFTERRESOLUTION with action RATE
  const closeAfterResolutionActions = timeline.filter((cp) => cp.status === "CLOSEDAFTERRESOLUTION");
  const hasRated = closeAfterResolutionActions.some((cp) => cp.performedAction === "RATE");

  if (hasRated) {
    // Find the RATE checkpoint (assumed to be the latest)
    const rateCheckpoint = closeAfterResolutionActions.find((cp) => cp.performedAction === "RATE");

    // Keep only the RATE one and filter out older CLOSE with same status
    filteredTimeline = timeline.filter((cp) => {
      // Keep everything else OR the RATE checkpoint
      if (cp === rateCheckpoint) return true;
      if (cp.status === "CLOSEDAFTERRESOLUTION" && cp.performedAction === "CLOSE") return false;
      return true;
    });
  }

  useEffect(() => {
    if (workflowDetails) {
      const { data: { timeline: complaintTimelineData } = {} } = workflowDetails;
      if (complaintTimelineData) {
        const applyAction = complaintTimelineData.find((action) => action.performedAction === "APPLY");
        const initiate = complaintTimelineData.find((action) => action.performedAction === "INITIATE");
        if (!initiate) {
          const complaintTimelineDataNew = {
            ...applyAction,
            performedAction: "INITIATE",
            state: "PENDINGRESOLUTIONNEW",
            status: "PENDINGRESOLUTIONNEW",
          };
          applyAction["wfComment"] = [];

          complaintTimelineData.push(complaintTimelineDataNew);
        }
        const actionByCitizenOnComplaintCreation = complaintTimelineData?.find((e) => e?.performedAction === "APPLY");
        const { thumbnailsToShow } = actionByCitizenOnComplaintCreation;

        thumbnailsToShow ? setImagesToShowBelowComplaintDetails(thumbnailsToShow) : null;
      }
    }
  }, [workflowDetails]);

  useEffect(() => {
    Digit?.Utils?.analytics?.trackPageView("ticket_details_page", {
      page_path: location?.pathname || "/ticket-details",
      page_title: "Ticket Details",
    });
  }, [location?.pathname]);

  const [displayMenu, setDisplayMenu] = useState(false);
  const [popup, setPopup] = useState(false);
  const [selectedAction, setSelectedAction] = useState(null);
  const [assignResponse, setAssignResponse] = useState(null);
  const [loader, setLoader] = useState(false);
  const [rerender, setRerender] = useState(1);
  const client = useQueryClient();
  function popupCall(option) {
    setDisplayMenu(false);
    setPopup(true);
  }
  useEffect(() => {
    setTimeout(() => setError(""), 10000);
  });

  useEffect(() => {
    (async () => {
      if (complaintDetails !== undefined) {
        let fetchedTenantId = complaintDetails?.incident?.tenantId;
        let fetchedId = complaintDetails?.incident?.incidentId;
        const assignWorkflow = await Digit?.WorkflowService?.getByBusinessId(fetchedTenantId, fetchedId);
      }
    })();
  }, [complaintDetails]);

  const refreshData = async () => {
    await client.refetchQueries(["fetchInboxData"]);
    await workflowDetails.revalidate();
    await revalidateComplaintDetails();
  };

  useEffect(() => {
    (async () => {
      if (complaintDetails) {
        setLoader(true);
        await refreshData();
        setLoader(false);
      }
    })();
  }, []);

  function zoomView() {
    setFullscreen(!fullscreen);
  }

  function close(state) {
    switch (state) {
      case fullscreen:
        setFullscreen(!fullscreen);
        break;
      case popup:
        setPopup(!popup);
        break;
      default:
        break;
    }
  }

  function zoomImage(imageSource, index) {
    setImageZoom(imageSource);
  }
  function zoomImageWrapper(imageSource, index) {
    if (imageSource.includes("jpeg") || imageSource.includes("jpg") || imageSource.includes("jpeg") || imageSource.includes("png")) {
      zoomImage(imagesToShowBelowComplaintDetails?.fullImage[index]);
    } else {
      window.open(imagesToShowBelowComplaintDetails?.fullImage[index]);
    }
  }
  function onCloseImageZoom() {
    setImageZoom(null);
  }

  function onActionSelect(action) {
    setSelectedAction(action);
    switch (action) {
      case "ASSIGN":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "REASSIGN":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "RESOLVE":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "REJECT":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "REOPEN":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "CLOSE":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "SENDBACK":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "OUT_OF_WARRANTY":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "SPARE_PART_NEEDED":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "APPROVE":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "REVISE":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "MARK_OUT_OF_SCOPE":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "STATUS_UPDATE":
        setPopup(true);
        setDisplayMenu(false);
        break;
      default:
        setDisplayMenu(false);
    }
  }

  async function onAssign(
    selectedEmployee,
    comments,
    uploadedFile,
    selectedReopenReason,
    selectedRejectReason,
    selectedSendBackReason,
    oowResponses,
    selectedOutOfScopeReason
  ) {
    setPopup(false);
    const response = await Digit.Complaint.assign(
      complaintDetails,
      selectedAction,
      selectedEmployee,
      comments,
      uploadedFile,
      tenant,
      selectedReopenReason,
      selectedRejectReason,
      selectedSendBackReason,
      oowResponses,
      selectedOutOfScopeReason
    );
    if (response?.IncidentWrappers) {
      setAssignResponse(response);
    } else {
      setError(response);
      //setTimeout(() => setError(false), 10000);
    }

    setToast(true);
    setLoader(true);
    await refreshData();
    setLoader(false);
    setRerender(rerender + 1);
    setTimeout(() => setToast(false), 10000);
  }

  function closeToast() {
    setToast(false);
  }

  if (isLoading || workflowDetails.isLoading || loader) {
    return <Loader />;
  }
  if (workflowDetails.isError) return <React.Fragment>{workflowDetails.error}</React.Fragment>;

  const getTimelineCaptions = (checkpoint, index, arr) => {
    const reopenReasons = Array.from(complaintDetails?.incident?.additionalDetail?.reopenreason || []).reverse();
    const rejectReasons = Array.from(complaintDetails?.incident?.additionalDetail?.rejectReason || []).reverse();
    const sendBackReasons = Array.from(complaintDetails?.incident?.additionalDetail?.sendBackReason || []).reverse();
    const oowResponsesArray = Array.from(complaintDetails?.incident?.additionalDetail?.oowResponses || []).reverse();
    const outOfScopeReasons = Array.from(complaintDetails?.incident?.additionalDetail?.outOfScopeReason || []).reverse();

    let arrNew = arr.map((abc) => {
      switch (abc.performedAction) {
        case "REOPEN":
          return { ...abc, reopenreason: reopenReasons.shift() };
        case "REJECT":
          return { ...abc, rejectReason: rejectReasons.shift() };
        case "SENDBACK":
          return { ...abc, sendBackReason: sendBackReasons.shift() };
        case "OUT_OF_WARRANTY":
          return { ...abc, oowResponses: oowResponsesArray.shift() };
        case "MARK_OUT_OF_SCOPE":
          return { ...abc, outOfScopeReason: outOfScopeReasons.shift() };
        default:
          return abc;
      }
    });

    const currentOowResponses =
      checkpoint.performedAction === "OUT_OF_WARRANTY" && arrNew[index]?.oowResponses
        ? {
            OOW_ACTION_ISSUE_OBSERVATION: arrNew[index]?.oowResponses?.oowIssue,
            OOW_ACTION_ISSUE_ROOT_CAUSE: arrNew[index]?.oowResponses?.oowRootCause,
            OOW_ACTION_ISSUE_SOLUTION: arrNew[index]?.oowResponses?.oowRecommendedSolution,
            OOW_ACTION_ISSUE_RESOLUTION_TIME: arrNew[index]?.oowResponses?.oowTimeToResolve,
            OOW_ACTION_ISSUE_SOLUTION_COST: arrNew[index]?.oowResponses?.oowTotalCostOfSolution,
          }
        : null;

    const arr1 = arr;
    const { wfComment: comment, thumbnailsToShow } = checkpoint;
    function zoomImageTimeLineWrapper(imageSource, index, thumbnailsToShow, arr) {
      if (arr1[index]?.status == "RESOLVED") {
        window.open(arr1[index].thumbnailsToShow.fullImage[0], "_blank");
      } else {
        let newIndex = thumbnailsToShow.thumbs?.findIndex((link) => link === imageSource);
        zoomImage((newIndex > -1 && thumbnailsToShow?.fullImage?.[newIndex]) || imageSource);
      }
    }
    const captionForOtherCheckpointsInTL = {
      date: checkpoint?.auditDetails?.lastModified,
      name: checkpoint?.assigner?.name,
      mobileNumber: checkpoint?.assigner?.mobileNumber,
      ...(checkpoint.status === "COMPLAINT_FILED" && complaintDetails?.audit
        ? {
            source: complaintDetails.audit.source,
          }
        : {}),
    };
    const isFirstPendingForAssignment = arr.length - (index + 1) === 1 ? true : false;
    if (checkpoint.status === "PENDINGFORASSIGNMENT" && complaintDetails?.audit) {
      if (isFirstPendingForAssignment) {
        const caption = {
          date: Digit.DateUtils.ConvertEpochToDate(complaintDetails.audit.details.createdTime),
        };
        return <TLCaption data={caption} comments={checkpoint?.wfComment} />;
      } else {
        const caption = {
          date: Digit.DateUtils.ConvertEpochToDate(complaintDetails.audit.details.lastModifiedTime),
        };
        return (
          <>
            {checkpoint.performedAction !== "SENDBACK" ? (
              <div className="TLComments">
                <h3>{t("WF_REOPEN_REASON")}</h3>
                <h1>{arrNew[index]?.reopenreason}</h1>
              </div>
            ) : null}
            {checkpoint.performedAction === "SENDBACK" ? (
              <div className="TLComments">
                <h3>{t("WF_SENDBACK_REASON")}</h3>
                <h1>{[arrNew[index]?.sendBackReason?.reason, arrNew[index]?.sendBackReason?.subReason].filter(Boolean).join(" - ")}</h1>
              </div>
            ) : null}
            {checkpoint?.wfComment ? (
              <div>
                {checkpoint?.wfComment?.map((e, index) => (
                  <div key={`comment-${index}`} className="TLComments">
                    <h3>{t("WF_COMMON_COMMENTS")}</h3>
                    <p style={{ overflowX: "scroll" }}>{e}</p>
                  </div>
                ))}
              </div>
            ) : null}
            {thumbnailsToShow?.thumbs?.length > 0 ? (
              <div className="TLComments">
                <h3>{t("CS_COMMON_ATTACHMENTS")}</h3>
                <DisplayPhotos
                  srcs={thumbnailsToShow.fullImage}
                  onClick={(src, index) => zoomImageTimeLineWrapper(src, index, thumbnailsToShow, arr)}
                />
                <DisplayPhotos
                  srcs={thumbnailsToShow.videos}
                  onClick={(src, index) => zoomImageTimeLineWrapper(src, index, thumbnailsToShow, arr)}
                />
              </div>
            ) : null}
            {checkpoint.performedAction === "SENDBACK"
              ? captionForOtherCheckpointsInTL?.date && <TLCaption data={captionForOtherCheckpointsInTL} />
              : caption?.date && <TLCaption data={caption} />}
          </>
        );
      }
    } else if (checkpoint.status === "CLOSEDAFTERRESOLUTION") {
      return (
        <>
          {complaintDetails.workflow.action === "RATE" ? (
            <div className="TLComments">
              <h3>{t("CS_COMPLAINT_RATING")}</h3>
              <StarRated rating={complaintDetails.workflow.rating} />
            </div>
          ) : (
            currentLoginUser === workflowDetails?.data?.processInstances?.[workflowDetails.data.processInstances.length - 1]?.assigner?.userName && (
              <div>
                <span className="link">
                  <Link to={`/${window.contextPath}/employee/im/complaint/feedback/${incidentId}/${tenantId}`}>Rate</Link>
                </span>
              </div>
            )
          )}
        </>
      );
    }
    // return (checkpoint.caption && checkpoint.caption.length !== 0) || checkpoint?.wfComment?.length > 0 ? <TLCaption data={checkpoint?.caption?.[0]} comments={checkpoint?.wfComment} /> : null;
    return (
      <>
        {checkpoint.status === "REJECTED" ? (
          <div className="TLComments">
            <h3>{t("WF_DECLINE_REASON")}</h3>
            <h1>{arrNew[index]?.rejectReason}</h1>
          </div>
        ) : null}
        {currentOowResponses && (
          <div>
            {Object.keys(currentOowResponses).map((field, index) => (
              <div key={`comment-${index}`} className="TLComments">
                <h3>{t(field)}</h3>
                <p style={{ overflowX: "scroll" }}>{currentOowResponses[field]}</p>
              </div>
            ))}
          </div>
        )}
        {checkpoint.performedAction === "MARK_OUT_OF_SCOPE" && (
          <div className="TLComments">
            <h3>{t("WF_OUT_OF_SCOPE_REASON")}</h3>
            <h1>{arrNew[index]?.outOfScopeReason}</h1>
          </div>
        )}
        {comment ? (
          <div>
            {comment?.map((e, index) => (
              <div key={`comment-${index}`} className="TLComments">
                <h3>{t("WF_COMMON_COMMENTS")}</h3>
                <p style={{ overflowX: "scroll" }}>{e}</p>
              </div>
            ))}
          </div>
        ) : null}
        {thumbnailsToShow?.thumbs?.length > 0 ? (
          <div className="TLComments">
            <h3>{t("CS_COMMON_ATTACHMENTS")}</h3>
            <DisplayPhotos srcs={thumbnailsToShow.fullImage} onClick={(src, index) => zoomImageTimeLineWrapper(src, index, thumbnailsToShow, arr)} />
          </div>
        ) : null}
        {captionForOtherCheckpointsInTL?.date ? <TLCaption data={captionForOtherCheckpointsInTL} /> : null}
        {checkpoint.status == "CLOSEDAFTERRESOLUTION" && complaintDetails.workflow.action == "RATE" && index <= 1 && complaintDetails.audit.rating ? (
          <StarRated text={t("CS_ADDCOMPLAINT_YOU_RATED")} rating={complaintDetails.audit.rating} />
        ) : null}
      </>
    );
  };
  const handleBack = () => {
    if (window.history.length > 1) {
      history.goBack();
    } else {
      history.push(`/${window.contextPath}/employee/im/inbox`)
    }
  };
  return (
    <React.Fragment>
      <div style={{ color: "#9e1b32", marginBottom: "10px", display:"flex", justifyContent: "end", marginRight: "15px" }}>
        <div onClick={handleBack} style={{ width: "fit-content", cursor: "pointer" }}>{t("CS_COMMON_BACK")}</div>
      </div>
      <Card>
        <div style={{ display: "flex", flexDirection: "column", gap: "5px" }}>
          <CardSubHeader>{t(`CS_HEADER_INCIDENT_SUMMARY`)}</CardSubHeader>
          <div
            style={{
              fontWeight: "bolder",
              fontSize: isMobile ? "16px" : "21px",
              marginTop: isMobile || isIpadView ? "20px" : -20,
              marginBottom: "22px",
            }}
          >
            {t("CS_HEADER_TICKET_DETAILS")}
          </div>
        </div>

        {isLoading ? (
          <Loader />
        ) : (
          <StatusTable>
            {complaintDetails &&
              Object.keys(complaintDetails?.details).map((k, i, arr) => (
                <Row
                  key={k}
                  label={t(k)}
                  text={
                    Array.isArray(complaintDetails?.details[k])
                      ? complaintDetails?.details[k].map((val) => (typeof val === "object" ? t(val?.code) : t(val)))
                      : t(complaintDetails?.details[k], { defaultValue: complaintDetails?.details[k] }) || "N/A"
                  }
                  last={arr.length - 1 === i}
                />
              ))}

            {1 === 1 ? null : (
              <MediaRow label="CS_COMPLAINT_DETAILS_GEOLOCATION">
                <MapView onClick={zoomView} />
              </MediaRow>
            )}
          </StatusTable>
        )}
        {imagesToShowBelowComplaintDetails?.thumbs ? (
          <div>
            <CardLabel style={{ marginTop: "18px", fontWeight: "bolder" }}>{t("CS_TICKET_ADDITIONAL_DETAILS")}</CardLabel>
            <DisplayPhotos srcs={imagesToShowBelowComplaintDetails?.fullImage || []} onClick={(source, index) => zoomImageWrapper(source, index)} />
            <DisplayPhotos srcs={imagesToShowBelowComplaintDetails?.videos || []} onClick={(source, index) => zoomImageWrapper(source, index)} />
          </div>
        ) : null}
        <BreakLine />
        {workflowDetails?.isLoading && <Loader />}
        {!workflowDetails?.isLoading && (
          <React.Fragment>
            <CardSubHeader>{t(`CS_COMPLAINT_DETAILS_COMPLAINT_TIMELINE`)}</CardSubHeader>

            {workflowDetails?.data?.timeline && workflowDetails?.data?.timeline?.length === 1 ? (
              <CheckPoint isCompleted={true} label={t("CS_COMMON_" + workflowDetails?.data?.timeline[0]?.status)} />
            ) : (
              <ConnectingCheckPoints>
                {workflowDetails?.data?.timeline &&
                  filteredTimeline.map((checkpoint, index, arr) => {
                    return (
                      <React.Fragment key={index}>
                        <CheckPoint
                          keyValue={index}
                          isCompleted={index === 0}
                          label={t("CS_COMMON_" + checkpoint.status)}
                          customChild={getTimelineCaptions(checkpoint, index, arr)}
                        />
                      </React.Fragment>
                    );
                  })}
              </ConnectingCheckPoints>
            )}
          </React.Fragment>
        )}
      </Card>
      {fullscreen ? (
        <PopUp>
          <div className="popup-module">
            <HeaderBar main={<Heading label="Complaint Geolocation" />} end={<CloseBtn onClick={() => close(fullscreen)} />} />
            <div className="popup-module-main">
              <img src="https://via.placeholder.com/912x568" />
            </div>
          </div>
        </PopUp>
      ) : null}
      {imageZoom ? <ImageViewer imageSrc={imageZoom} onClose={onCloseImageZoom} /> : null}
      {popup ? (
        <ComplaintDetailsModal
          workflowDetails={workflowDetails}
          complaintDetails={complaintDetails}
          close={close}
          popup={popup}
          selectedAction={selectedAction}
          onAssign={onAssign}
          tenantId={tenant}
          t={t}
        />
      ) : null}
      {toast && assignResponse && assignResponse?.IncidentWrappers && <Toast label={t(`CS_ACTION_${selectedAction}_TEXT`)} onClose={closeToast} />}
      {!isLoading &&
        complaintDetails?.incident?.applicationStatus !== "CLOSEDAFTERRESOLUTION" &&
        workflowDetails?.data?.nextActions?.length > 0 && (
          <ActionBar style={{ marginLeft: isIpadView ? "250px" : "none" }}>
            {displayMenu && workflowDetails?.data?.nextActions ? (
              <Menu options={workflowDetails?.data?.nextActions.map((action) => action.action)} t={t} onSelect={onActionSelect} />
            ) : null}
            <SubmitBar label={t("WF_TAKE_ACTION")} onSubmit={() => setDisplayMenu(!displayMenu)} />
          </ActionBar>
        )}
      {error && error[0].message && <Toast error={error[0].message} label={error[0].message} onClose={closeToast} />}
    </React.Fragment>
  );
};
