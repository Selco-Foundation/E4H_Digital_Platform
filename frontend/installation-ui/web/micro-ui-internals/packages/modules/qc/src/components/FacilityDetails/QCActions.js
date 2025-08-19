import React from 'react';
import { useDispatch, useSelector } from "react-redux";
import { clearRejectionReasons } from "../../redux/actions";

const QCActions = ({ invalidateFieldPlan, invalidateFacilityDetails, setUpdatingWorkflow }) => {

  const dispatch = useDispatch();
  const rejectionReasons = useSelector((state) => state.qc.rejectionReasons);
  const selectedFacility = useSelector((state) => state.qc.common.selectedFacility);

  const handleApprove = async () => {
    setUpdatingWorkflow(true);

    try {
      const response = await Digit.QCService.updateProjectWorkflow(
        selectedFacility?.projectId, "APPROVE",
        [], "Approved by Installation Reviewer"
      );

      if (response) {
        invalidateFieldPlan();
        invalidateFacilityDetails();
        dispatch(clearRejectionReasons());
      }

    } catch (error) {
      console.error("Error approving", error);
    } finally {
      setUpdatingWorkflow(false);
    }
  }

  const formatRejectionReasons = (reasons) => {
    const rejectionReasonsToUpload = {};
    Object.keys(reasons).forEach(key => {
      if (reasons[key].length > 0) {
        rejectionReasonsToUpload[key] = reasons[key].map(reason => ({
          reason: reason.reason,
          comment: reason.comment,
        }));
      }
    })

    const comments = [];
    Object.keys(rejectionReasonsToUpload).forEach(key => {
      rejectionReasonsToUpload[key].forEach(rejectionReason => {
        comments.push({
          commentMessage : JSON.stringify(rejectionReason),
          assetType : key.toUpperCase(),
        });
      })
    })

    return comments;
  }

  const handleReject = async () => {
    setUpdatingWorkflow(true);
    const comments = formatRejectionReasons(rejectionReasons);

    try {
      const response = await Digit.QCService.updateProjectWorkflow(
        selectedFacility?.projectId, "REJECT_AND_ASSIGN_FOR_FIELD_QC",
        comments, "Rejected by Installation Reviewer"
      );

      if (response) {
        invalidateFieldPlan();
        invalidateFacilityDetails();
        dispatch(clearRejectionReasons());
      }

    } catch (error) {
      console.error("Error rejecting", error);
    } finally {
      setUpdatingWorkflow(false);
    }
  }

  const handleFlagForQC = async () => {
    setUpdatingWorkflow(true);
    const comments = formatRejectionReasons(rejectionReasons);

    try {
      const response = await Digit.QCService.updateProjectWorkflow(
        selectedFacility?.projectId, "FLAG_FOR_QC",
        comments, "Flagged for QC by Installation Reviewer"
      );

      if (response) {
        invalidateFieldPlan();
        invalidateFacilityDetails();
        dispatch(clearRejectionReasons());
      }

    } catch (error) {
      console.error("Error flagging for QC", error);
    } finally {
      setUpdatingWorkflow(false);
    }
  }

  const showRejectActions = Object.values(rejectionReasons).some(reasons => reasons.length > 0);

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
      {showRejectActions ? (
        <div style={{display: 'flex', gap: '12px'}}>
          <button
            onClick={handleFlagForQC}
            style={{
              backgroundColor: "white",
              color: '#C1440E',
              border: "1px solid #C1440E",
              padding: '10px 24px',
              fontSize: '16px',
              fontWeight: 'bold',
              borderRadius: '2px',
              cursor: 'pointer'
            }}
          >
            Flag for QC
          </button>
          <button
            onClick={handleReject}
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
            Reject
          </button>
        </div>
      ) : (
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
          Approve
        </button>
      )}

    </div>
  );
};

export default QCActions;
