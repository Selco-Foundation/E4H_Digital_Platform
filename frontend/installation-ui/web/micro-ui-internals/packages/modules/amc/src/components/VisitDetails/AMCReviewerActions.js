import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { Toast } from "@egovernments/digit-ui-react-components";
import AddRejectionReasonModal from "./AddRejectionReasonModal";
import {VisitService} from "../../services/VisitService";

const AMCReviewerActions = ({ t, revalidateData, setUpdatingWorkflow, aggregatedDocuments }) => {

  const workingVisit = useSelector((state) => state.amc.common.workingVisit);
  const [showRejectionModal, setShowRejectionModal] = useState(false);
  const [toast, setToast] = useState(null);

  useEffect(()=>{
    let timeoutId;
    if(toast){
      timeoutId = setTimeout(()=>{
        setToast(null);
      },2500)
    }

    return () => {
      if (timeoutId) clearTimeout(timeoutId);
    };
  },[toast])

  const handleApprove = async () => {
    setUpdatingWorkflow(true);

    try {
      await VisitService.updateVisitWorkflow(
        workingVisit?.id, "APPROVE",
        "Approved by AMC Reviewer",
        aggregatedDocuments
      );

      revalidateData();
      setUpdatingWorkflow(false);
      setToast({
        key: "success",
        message: t("AMC_VISIT_APPROVE_SUCCESS"),
      });

    } catch (error) {
      console.error("Error approving", error);
      setUpdatingWorkflow(false);
      setToast({
        key: "error",
        message: t("AMC_VISIT_APPROVE_FAILURE"),
      });

    } finally {
      setUpdatingWorkflow(false);
    }
  }

  const formatRejectionReasons = (rejectionReasons) => {
    return rejectionReasons.map((rejectionReason) => ({
      reason: rejectionReason.reason,
      comment: rejectionReason.comment,
    }));
  }

  const handleReject = async (rejectionReasons) => {
    setShowRejectionModal(false);
    setUpdatingWorkflow(true);
    const comments = formatRejectionReasons(rejectionReasons);

    try {
      await VisitService.updateVisitWorkflow(
        workingVisit?.id, "REJECT",
        JSON.stringify(comments),
        aggregatedDocuments
      );

      revalidateData();
      setUpdatingWorkflow(false);
      setToast({
        key: "success",
        message: t("AMC_VISIT_REJECT_SUCCESS"),
      });

    } catch (error) {
      console.error("Error rejecting", error);
      setUpdatingWorkflow(false);
      setToast({
        key: "error",
        message: t("AMC_VISIT_REJECT_FAILURE"),
      });

    } finally {
      setUpdatingWorkflow(false);
    }
  }

  return (
    <div style={{
      position: 'fixed',
      bottom: 0,
      right: 0,
      padding: '12px 50px',
      display: 'flex',
      justifyContent: 'flex-end',
      backgroundColor: '#fff',
      width: '100%',
      boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
      zIndex: 1,
    }}>
      <div style={{display: 'flex', gap: '12px'}}>
        <button
          style={{
            backgroundColor: '#C1440E',
            color: '#fff',
            border: "1px solid #C1440E",
            padding: '10px 24px',
            fontSize: '16px',
            fontWeight: 'bold',
            borderRadius: '2px',
            cursor: 'pointer'
          }}
          onClick={() => setShowRejectionModal(true)}
        >
          {t("CORE_COMMON_REJECT")}
        </button>
        <button
          onClick={handleApprove}
          style={{
            backgroundColor: '#C1440E',
            color: '#fff',
            border: "1px solid #C1440E",
            padding: '10px 24px',
            fontSize: '16px',
            fontWeight: 'bold',
            borderRadius: '2px',
            cursor: 'pointer'
          }}
        >
          {t("CORE_COMMON_APPROVE")}
        </button>
      </div>
      {showRejectionModal && (
        <AddRejectionReasonModal
          t={t}
          onClose={() => setShowRejectionModal(false)}
          handleConfirmRejection={handleReject}
        />
      )}
      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          label={toast.message}
          onClose={() => setToast(null)}
          style={{
            ...(toast.key === "error" ? {backgroundColor: "#B91900"} : {}),
            maxWidth: "670px"
          }}
          isDleteBtn={true}
        />
      )}
    </div>
  );
};

export default AMCReviewerActions;
