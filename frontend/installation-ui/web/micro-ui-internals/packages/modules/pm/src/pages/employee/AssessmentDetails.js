import React, { useEffect, useRef, useState } from "react";
import { Loader, Table, Toast } from "@egovernments/digit-ui-react-components";
import { useDispatch } from "react-redux";
import { Link, useHistory, useLocation } from "react-router-dom";
import Filter from "../../components/AssessmentDetails/Filter";
import InfoCard from "../../components/AssessmentDetails/InfoCard";
import FacilityActionBar from "../../components/AssessmentDetails/FacilityActionBar";
import ConfirmActionModal from "../../components/AssessmentDetails/ConfirmActionModal";
import ReasonRequiredModal from "../../components/AssessmentDetails/ReasonRequiredModal";
import CompleteAssessmentPlanModal from "../../components/AssessmentDetails/CompleteAssessmentPlanModal";
import CustomCheckBox from "../../components/Custom/CustomCheckBox";
import useAssessmentPlan from "../../hooks/useAssessmentPlan";
import useAssessmentPlanDetail from "../../hooks/useAssessmentPlanDetail";
import useAssessmentFacility from "../../hooks/useAssessmentFacility";
import useProject from "../../hooks/useProject";
import { populateWorkingAssessmentPlan, populateWorkingProject } from "../../redux/actions";
import { AssessmentFacilityService } from "../../services/AssessmentFacility";
import { AssessmentPlanService } from "../../services/AssessmentPlan";
import { PMService } from "../../services/PMService";
import { canAssignForOnSiteAssessment, evaluateMarkResultScenario } from "../../utilities/AssessmentPlanData";
import CommonUtils from "../../utilities/CommonUtils";
import {useTranslation} from "react-i18next";

const STATUS_BADGE_STYLES = {
  QUALIFIED: { backgroundColor: "#E7F6EC", color: "#1B8354" },
  ELIGIBLE: { backgroundColor: "#E7F6EC", color: "#1B8354" },
  PENDING: { backgroundColor: "#FFF4DE", color: "#B4790E" },
  PENDING_WRONG_NUMBER: { backgroundColor: "#FFF4DE", color: "#B4790E" },
  PENDING_NO_ANSWER: { backgroundColor: "#FFF4DE", color: "#B4790E" },
  NOT_INITIATED: { backgroundColor: "#F1F1F1", color: "#6B7280" },
  NOT_ELIGIBLE: { backgroundColor: "#FDEAEA", color: "#B91900" },
  NOT_QUALIFIED: { backgroundColor: "#FDEAEA", color: "#B91900" },
};

const AssessmentDetails = () => {

  const { t } = useTranslation();
  const [mainCheck, setMainCheck] = useState(false);
  const dispatch = useDispatch();
  const [assessmentPlan, setAssessmentPlan] = useState({});
  const [selectedFacilityIds, setSelectedFacilityIds] = useState([]);
  const [fetchedData, setData] = useState([]);
  const [pendingAction, setPendingAction] = useState(null);
  const [completePlanModalOpen, setCompletePlanModalOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [toast, setToast] = useState(null);
  const history = useHistory();
  const location = useLocation();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const assessmentId = url.split("assessment/")[1].split("/")[0];
  const queryParams = new URLSearchParams(window.location.search);

  const [projectQueryFilter, setProjectQueryFilter] = useState((() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : null;
    } catch (error) {
      console.error("Failed to parse filter parameter:", error);
      return null;
    }
  })() || {
    facilityFilter: {
      category: [],
      facilityType: [],
      district: [],
      block: [],
      remoteStatus: [],
      onSiteStatus: [],
      result: [],
    },
    facilitySearch: {
      name: ""
    },
    facilityFilterQuery: {},
    facilitySearchQuery: {},
  });
  const prevSearchParamsRef = useRef(JSON.stringify(projectQueryFilter));

  const [pageSize, setPageSize] = useState(queryParams.get("pageSize") || 10);
  const [pageOffset, setPageOffset] = useState(queryParams.get("pageOffset") || 0);
  const prevPageSizeRef = useRef(pageSize);

  const {
    isLoading: projectDataLoading,
    data: projectData,
  } = useProject({
    id: [projectId]
  });

  const {
    isLoading: assessmentPlanDataLoading,
    data: assessmentPlanData,
    revalidate: revalidateAssessmentPlan,
  } = useAssessmentPlan({
    id: [assessmentId]
  });

  const {
    isLoading,
    isFetching: facilityDataFetching,
    data: facilityData,
    revalidate: revalidateFacilities,
  } = useAssessmentFacility(assessmentId, projectQueryFilter, pageSize, pageOffset);

  const {
    isLoading: planDetailLoading,
    data: planDetailData,
    revalidate: revalidatePlanDetail,
  } = useAssessmentPlanDetail(assessmentId);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(projectQueryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
    });
  }, [projectQueryFilter, pageSize, pageOffset])

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(projectQueryFilter);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [projectQueryFilter, pageSize]);

  useEffect(() => {
    if (facilityData) {
      setData(facilityData.facilities);
      setSelectedFacilityIds([]);
      setMainCheck(false);
    }
  }, [facilityData])

  useEffect(() => {
    if (assessmentPlanData) {
      const plan = assessmentPlanData.assessmentPlans[0];
      setAssessmentPlan(plan);
      dispatch(populateWorkingAssessmentPlan(plan));
    }
  }, [assessmentPlanData])

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
    }
  }, [projectData])

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 2500);
      return () => clearTimeout(timer);
    }
  }, [toast])

  const planCompleted = assessmentPlan?.status === "CLOSED";

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  }

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  }

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  }

  const handleFilterChange = (filters) => {
    setProjectQueryFilter({
      ...projectQueryFilter,
      ...filters,
    });
  };

  const handleEditAssessmentPlan = () => {
    history.push(`/${window.contextPath}/employee/pm/project/${projectId}/assessment/create?assessmentId=${assessmentPlan.id}`);
  };

  const handleDownload = async () => {
    setActionLoading(true);
    try {
      await PMService.downloadAssessmentPlanFacilityExport(assessmentId, projectQueryFilter.facilityFilterQuery);
    } catch (error) {
      console.error("Error exporting assessment plan facilities", error);
      setToast({ key: "error", label: CommonUtils.getApiErrorMessage(error) || t("PM_ASSESSMENT_FACILITY_DATA_DOWNLOAD_ERROR") });
    } finally {
      setActionLoading(false);
    }
  };

  const GetCell = (value) => <span className="cell-text" style={{ color: "#000000" }}>{value}</span>;

  const GetStatusBadge = (code) => {
    if (!code) {
      return GetCell("-");
    }
    const style = STATUS_BADGE_STYLES[code] || STATUS_BADGE_STYLES.NOT_INITIATED;
    return (
      <span
        style={{
          ...style,
          borderRadius: "4px",
          padding: "4px 12px",
          fontSize: "13px",
          fontWeight: 500,
          whiteSpace: "nowrap",
          width: "fit-content",
          display: "inline-block",
        }}
      >
        {t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`)}
      </span>
    );
  };

  const mainCheckboxChange = () => {
    const prevMainCheck = mainCheck;
    setMainCheck(!prevMainCheck);
    setSelectedFacilityIds(!prevMainCheck ? fetchedData.map((row) => row.id) : []);
  };

  const sideCheckboxChange = (id) => {
    setMainCheck(false);

    if (selectedFacilityIds.some((facilityId) => facilityId === id)) {
      setSelectedFacilityIds(selectedFacilityIds.filter((facilityId) => facilityId !== id));
    } else {
      setSelectedFacilityIds([...selectedFacilityIds, id]);
    }
  };

  const canBulkAssignOnSite = () => {
    if (planCompleted || !selectedFacilityIds.length) return false;

    const selectedFacilities = fetchedData.filter((facility) => selectedFacilityIds.includes(facility.id));
    return selectedFacilities.length > 0 && selectedFacilities.every((facility) => canAssignForOnSiteAssessment(facility));
  };

  const canBulkMarkResult = (targetResult) => {
    if (planCompleted || !selectedFacilityIds.length) return false;

    const selectedFacilities = fetchedData.filter((facility) => selectedFacilityIds.includes(facility.id));
    return selectedFacilities.length > 0 && selectedFacilities.every((facility) => facility.result !== targetResult);
  };

  const openAssignOnSiteConfirm = (facilityIds) => {
    setPendingAction({ type: "ASSIGN_ONSITE", facilityIds });
  };

  // Mark Eligible/Mark Not Eligible run through a strict validation order: a blocking modal if
  // any selected facility still has a pending remote assessment, then warnings if any selected
  // facility's on-site assessment is pending/not-initiated, then (only once every selected
  // facility has finalised both assessments) either a single shared reason prompt when every
  // facility needs one, a "not supported" notice when only some do, or a plain confirmation.
  const openMarkResultConfirm = (facilityIds, targetResult) => {
    const selectedFacilities = fetchedData.filter((facility) => facilityIds.includes(facility.id));
    const scenario = evaluateMarkResultScenario(selectedFacilities, targetResult);

    if (scenario === "BLOCK_REMOTE_PENDING") {
      setPendingAction({ type: "BLOCK_REMOTE_PENDING" });
      return;
    }

    if (scenario === "BULK_NOT_SUPPORTED") {
      setPendingAction({ type: "BULK_ACTION_NOT_SUPPORTED" });
      return;
    }

    setPendingAction({
      type: scenario === "REASON_REQUIRED" ? "MARK_RESULT_REASON_REQUIRED" : scenario,
      targetResult,
      facilityIds,
    });
  };

  const openMarkEligibleConfirm = (facilityIds) => openMarkResultConfirm(facilityIds, "ELIGIBLE");

  const openMarkNotEligibleConfirm = (facilityIds) => openMarkResultConfirm(facilityIds, "NOT_ELIGIBLE");

  const closePendingAction = () => setPendingAction(null);

  const handleConfirmPendingAction = async (reason) => {
    if (!pendingAction) return;

    setActionLoading(true);
    try {
      const decisionFields = pendingAction.type === "ASSIGN_ONSITE"
        ? { assignForField: true }
        : { overallStatus: pendingAction.targetResult, ...(reason ? { remarks: reason } : {}) };

      const decisions = pendingAction.facilityIds.map((facilityId) => ({ planFacilityId: facilityId, ...decisionFields }));
      await AssessmentFacilityService.bulkUpdateFacilityDecisions(assessmentId, decisions);

      await revalidateFacilities();
      await revalidatePlanDetail();
      setSelectedFacilityIds([]);
      setMainCheck(false);
      setPendingAction(null);
      setToast({ key: "success", label: t("PM_ASSESSMENT_ACTION_SUCCESS") });
    } catch (error) {
      console.error("Error updating assessment facility", error);
      setToast({ key: "error", label: CommonUtils.getApiErrorMessage(error) || t("PM_ASSESSMENT_ACTION_ERROR") });
    } finally {
      setActionLoading(false);
    }
  };

  const canCompletePlan = () => {
    if (planCompleted) return false;
    const summary = planDetailData;
    if (!summary?.totalFacilities) return false;
    return (summary.eligible + summary.notEligible) === summary.totalFacilities;
  };

  const handleCompletePlan = async () => {
    setActionLoading(true);
    try {
      const updatedPlan = await AssessmentPlanService.completeAssessmentPlan(assessmentPlan);
      if (updatedPlan) {
        setAssessmentPlan(updatedPlan);
        dispatch(populateWorkingAssessmentPlan(updatedPlan));
      }
      await revalidateAssessmentPlan();
      setCompletePlanModalOpen(false);
      setToast({ key: "success", label: t("PM_ASSESSMENT_COMPLETE_PLAN_SUCCESS") });
    } catch (error) {
      console.error("Error completing assessment plan", error);
      setToast({ key: "error", label: CommonUtils.getApiErrorMessage(error) || t("PM_ASSESSMENT_COMPLETE_PLAN_ERROR") });
    } finally {
      setActionLoading(false);
    }
  };

  const bulkActions = [
    {
      key: "assign-onsite",
      label: t("PM_ASSESSMENT_ACTION_ASSIGN_ONSITE"),
      backgroundColor: "#0B4B66",
      disabled: !canBulkAssignOnSite(),
      onClick: () => openAssignOnSiteConfirm(selectedFacilityIds),
    },
    {
      key: "mark-eligible",
      label: t("PM_ASSESSMENT_ACTION_MARK_ELIGIBLE"),
      backgroundColor: "#1B8354",
      disabled: !canBulkMarkResult("ELIGIBLE"),
      onClick: () => openMarkEligibleConfirm(selectedFacilityIds),
    },
    {
      key: "mark-not-eligible",
      label: t("PM_ASSESSMENT_ACTION_MARK_NOT_ELIGIBLE"),
      backgroundColor: "#B91900",
      disabled: !canBulkMarkResult("NOT_ELIGIBLE"),
      onClick: () => openMarkNotEligibleConfirm(selectedFacilityIds),
    },
  ];

  const columns = [
    {
      id: "selection",
      Header: () => (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
          <CustomCheckBox
            checked={mainCheck}
            disable={planCompleted}
            onChange={mainCheckboxChange}
            styles={{ width: "24px", height: "24px" }}
          />
        </div>
      ),
      Cell: ({ row }) => (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
          <CustomCheckBox
            checked={selectedFacilityIds.some((facilityId) => facilityId === row.original["id"])}
            disable={planCompleted}
            onChange={() => sideCheckboxChange(row.original["id"])}
            styles={{ width: "24px", height: "24px" }}
          />
        </div>
      ),
    },
    {
      Header: t("PM_ASSESSMENT_FACILITY_NAME"),
      Cell: ({ row }) => (
        <Link
          to={`/${window.contextPath}/employee/pm/project/${projectId}/assessment/${assessmentId}/facilities/${row.original["id"]}/details`}
          style={{ fontWeight: 700, color: "#C84C0E" }}
        >
          {row.original["name"]}
        </Link>
      ),
    },
    {
      Header: t("PM_ASSESSMENT_FACILITY_TYPE"),
      Cell: ({ row }) => GetCell(row.original["facilityType"] || "-"),
    },
    {
      Header: t("PM_ASSESSMENT_DISTRICT_BLOCK"),
      Cell: ({ row }) => (
        <div>
          <div>{row.original["district"] || "-"}</div>
          <div style={{ color: "#6B7280", fontSize: "12px" }}>{row.original["block"]}</div>
        </div>
      ),
    },
    {
      Header: t("PM_ASSESSMENT_REMOTE_STATUS"),
      Cell: ({ row }) => GetStatusBadge(row.original["remoteStatus"]),
    },
    {
      Header: t("PM_ASSESSMENT_ONSITE_STATUS"),
      Cell: ({ row }) => GetStatusBadge(row.original["onSiteStatus"]),
    },
    {
      Header: t("PM_ASSESSMENT_RESULT"),
      Cell: ({ row }) => GetStatusBadge(row.original["result"]),
    },
  ];

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("PM_ASSESSMENT_NO_FACILITIES_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div style={{ backgroundColor: "white", padding: "15px 0px 0px 0px" }}>
        <div
          className={"health-facility-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            customTableWrapperClassName={"health-facility-table"}
            data={fetchedData}
            columns={columns}
            getCellProps={() => {
              return {
                style: {
                  maxWidth: "100%",
                  padding: "17.24px 18px",
                  fontSize: "15px",
                },
              };
            }}
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={facilityData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    );
  }

  const getConfirmModalProps = () => {
    const count = pendingAction?.facilityIds?.length || 0;

    if (pendingAction?.type === "ASSIGN_ONSITE") {
      return {
        title: t("PM_ASSESSMENT_CONFIRM_ONSITE_TITLE"),
        description: t("PM_ASSESSMENT_CONFIRM_ONSITE_DESC"),
        message: `${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_PREFIX")} ${count} ${t("PM_ASSESSMENT_FACILITIES_UNIT")} ${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_TO")} '${t("PM_ASSESSMENT_FACILITY_STATUS_PENDING")}'.`,
        confirmLabel: t("PM_ASSESSMENT_ACTION_ASSIGN_ONSITE"),
        confirmColor: "#0B4B66",
      };
    }

    if (pendingAction?.type === "BLOCK_REMOTE_PENDING") {
      return {
        title: t("PM_ASSESSMENT_BLOCK_REMOTE_PENDING_TITLE"),
        description: t("PM_ASSESSMENT_BLOCK_REMOTE_PENDING_DESC"),
        confirmLabel: t("CORE_COMMON_DISMISS"),
        confirmColor: "#0B4B66",
        singleAction: true,
        informational: true,
      };
    }

    if (pendingAction?.type === "BULK_ACTION_NOT_SUPPORTED") {
      return {
        title: t("PM_ASSESSMENT_BULK_NOT_SUPPORTED_TITLE"),
        description: t("PM_ASSESSMENT_BULK_NOT_SUPPORTED_DESC"),
        confirmLabel: t("CORE_COMMON_OK"),
        confirmColor: "#0B4B66",
        singleAction: true,
        informational: true,
      };
    }

    if (pendingAction?.type === "WARN_ONSITE_PENDING") {
      return {
        title: t("PM_ASSESSMENT_WARN_ONSITE_PENDING_TITLE"),
        description: t("PM_ASSESSMENT_WARN_ONSITE_PENDING_DESC"),
        confirmLabel: t("CORE_COMMON_CONFIRM"),
        confirmColor: pendingAction.targetResult === "NOT_ELIGIBLE" ? "#B91900" : "#1B8354",
      };
    }

    if (pendingAction?.type === "WARN_ONSITE_NOT_INITIATED") {
      return {
        title: t("PM_ASSESSMENT_WARN_ONSITE_NOT_INITIATED_TITLE"),
        description: t("PM_ASSESSMENT_WARN_ONSITE_NOT_INITIATED_DESC"),
        confirmLabel: t("CORE_COMMON_CONFIRM"),
        confirmColor: pendingAction.targetResult === "NOT_ELIGIBLE" ? "#B91900" : "#1B8354",
      };
    }

    if (pendingAction?.type === "PROCEED" && pendingAction?.targetResult === "ELIGIBLE") {
      return {
        title: t("PM_ASSESSMENT_CONFIRM_RESULT_TITLE"),
        description: t("PM_ASSESSMENT_CONFIRM_ELIGIBLE_DESC"),
        message: `${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_PREFIX")} ${count} ${t("PM_ASSESSMENT_FACILITIES_UNIT")} ${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_TO")} '${t("PM_ASSESSMENT_FACILITY_STATUS_ELIGIBLE")}'.`,
        confirmLabel: t("PM_ASSESSMENT_ACTION_MARK_ELIGIBLE"),
        confirmColor: "#1B8354",
      };
    }

    if (pendingAction?.type === "PROCEED" && pendingAction?.targetResult === "NOT_ELIGIBLE") {
      return {
        title: t("PM_ASSESSMENT_CONFIRM_RESULT_TITLE"),
        message: `${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_PREFIX")} ${count} ${t("PM_ASSESSMENT_FACILITIES_UNIT")} ${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_TO")} '${t("PM_ASSESSMENT_FACILITY_STATUS_NOT_ELIGIBLE")}'.`,
        confirmLabel: t("PM_ASSESSMENT_ACTION_MARK_NOT_ELIGIBLE"),
        confirmColor: "#B91900",
      };
    }

    return null;
  };

  const getReasonModalDescription = () => (
    pendingAction?.targetResult === "ELIGIBLE"
      ? t("PM_ASSESSMENT_REASON_REQUIRED_ELIGIBLE_DESC")
      : t("PM_ASSESSMENT_REASON_REQUIRED_NOT_ELIGIBLE_DESC")
  );

  if (projectDataLoading || assessmentPlanDataLoading || planDetailLoading) {
    return <Loader />;
  }

  const confirmModalProps = getConfirmModalProps();

  return (
    <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
      {(actionLoading || (!isLoading && facilityDataFetching)) && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000000,
            backgroundColor: "gray",
            opacity: 0.5,
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
        </div>
      )}
      <div style={{ display: "flex", flexWrap: "wrap", gap: "15px", alignItems: "center", justifyContent: "space-between", marginBottom: "20px" }}>
        <div style={{ fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", color: "#0B0C0C" }}>
          {assessmentPlan?.name}
        </div>
        <div style={{ display: "flex", gap: "12px" }}>
          <button
            type="button"
            className={"jk-digit-secondary-btn"}
            disabled={planCompleted}
            onClick={handleEditAssessmentPlan}
            style={{
              height: "40px",
              padding: "0px 20px",
              fontSize: "16px",
              fontWeight: "500",
              fontFamily: "Roboto",
              cursor: planCompleted ? "default" : "pointer",
              opacity: planCompleted ? 0.5 : 1,
            }}
          >
            {t("PM_ACTION_EDIT_ASSESSMENT_PLAN")}
          </button>
          <button
            type="button"
            disabled={!canCompletePlan()}
            onClick={() => setCompletePlanModalOpen(true)}
            style={{
              height: "40px",
              padding: "0px 20px",
              fontSize: "16px",
              fontWeight: "500",
              fontFamily: "Roboto",
              border: "none",
              borderRadius: "4px",
              backgroundColor: canCompletePlan() ? "#0B4B66" : "#D6D5D4",
              color: canCompletePlan() ? "white" : "#6B7280",
              cursor: canCompletePlan() ? "pointer" : "default",
            }}
          >
            {t("PM_ACTION_PROCEED_TO_FIELD_PLAN_CREATION")}
          </button>
        </div>
      </div>
      <InfoCard t={t} summary={planDetailData} />
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter
            t={t}
            assessmentPlan={assessmentPlan}
            projectQueryFilter={projectQueryFilter}
            onFilterChange={handleFilterChange}
          />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <FacilityActionBar
              t={t}
              selectedFacilityIds={selectedFacilityIds}
              bulkActions={planCompleted ? [] : bulkActions}
              onDownload={handleDownload}
            />
          </div>
          {renderFacilities()}
        </div>
      </div>

      {confirmModalProps && (
        <ConfirmActionModal
          t={t}
          title={confirmModalProps.title}
          description={confirmModalProps.description}
          message={confirmModalProps.message}
          confirmLabel={confirmModalProps.confirmLabel}
          confirmColor={confirmModalProps.confirmColor}
          singleAction={confirmModalProps.singleAction}
          loading={actionLoading}
          onConfirm={() => (confirmModalProps.informational ? closePendingAction() : handleConfirmPendingAction())}
          onClose={closePendingAction}
        />
      )}

      {pendingAction?.type === "MARK_RESULT_REASON_REQUIRED" && (
        <ReasonRequiredModal
          t={t}
          description={getReasonModalDescription()}
          loading={actionLoading}
          onConfirm={(reason) => handleConfirmPendingAction(reason)}
          onClose={closePendingAction}
        />
      )}

      {completePlanModalOpen && (
        <CompleteAssessmentPlanModal
          t={t}
          loading={actionLoading}
          onConfirm={handleCompletePlan}
          onClose={() => setCompletePlanModalOpen(false)}
        />
      )}

      {toast && (
        <div style={{ position: "fixed", zIndex: 10000001 }}>
          <Toast
            error={toast.key === "error"}
            warning={toast.key === "warning"}
            style={toast.key === "error" ? { backgroundColor: "#B91900" } : {}}
            label={toast.label}
            isDleteBtn={true}
            onClose={() => setToast(null)}
          />
        </div>
      )}
    </div>
  );
};

export default AssessmentDetails;
