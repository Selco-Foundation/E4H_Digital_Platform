import React from 'react';
import { useSelector } from "react-redux";
import { QCService } from "../../Service/QCService";

const QCActions = () => {

  const rejectionReasons = useSelector((state) => state.qc.rejectionReasons);
  const selectedFieldPlan = useSelector((state) => state.qc.common.selectedFieldPlan)

  const handleApprove = async () => {
    await QCService.updateProjectWorkflow(selectedFieldPlan.id, "APPROVE", "")
      .then(response => {
        console.debug("Approved", response);
      })
      .catch(error => {
        console.error("Error approving", error);
      })
  }

  const handleReject = async () => {
    console.debug("Rejection Reasons", rejectionReasons);
    const rejectionReasonsToUpload = {};
    Object.keys(rejectionReasons).forEach(key => {
      if (rejectionReasons[key].length > 0) {
        rejectionReasonsToUpload[key] = rejectionReasons[key];
      }
    })
    await QCService.updateProjectWorkflow(selectedFieldPlan.id, "REJECT_AND_ASSIGN_FOR_FIELD_QC", JSON.stringify(rejectionReasonsToUpload))
      .then(response => {
        console.debug("Rejecting", response);
      })
      .catch(error => {
        console.error("Error rejecting", error);
      })
  }

  const handleFlagForQC = () => {
    console.log("Flagged for QC");
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
      zIndex: 1000,
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
