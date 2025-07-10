import React from 'react';
import { useSelector } from "react-redux";
import { QCService } from "../../Service/QCService";

const QCActions = () => {

  const rejectionReasons = useSelector((state) => state.qc.rejectionReasons);

  const handleApprove = async () => {
    const response = await QCService.updateProjectWorkflow("", "APPROVE", "");
    console.log("Approved");
  }

  const handleReject = async () => {
    console.debug("Rejection Reasons", rejectionReasons);
    const rejectionReasonsToUpload = {};
    Object.keys(rejectionReasons).forEach(key => {
      if (rejectionReasons[key].length > 0) {
        rejectionReasonsToUpload[key] = rejectionReasons[key];
      }
    })
    const response = await QCService.updateProjectWorkflow("", "APPROVE", JSON.stringify(rejectionReasonsToUpload));
    console.log("Rejected");
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
