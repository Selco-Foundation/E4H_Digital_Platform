import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Toast } from "@egovernments/digit-ui-react-components";
import { clearRejectionReasons } from "../../redux/actions";
import { ActivityService } from "../../services/Activity";

const QCActions = ({ t, revalidateData, setUpdatingWorkflow, aggregatedDocuments }) => {

  const dispatch = useDispatch();
  const rejectionReasons = useSelector((state) => state.qc.rejectionReasons);
  const selectedFacility = useSelector((state) => state.qc.common.selectedFacility);
  const [toast, setToast] = useState(null);

  useEffect(()=>{
    if(toast){
      setTimeout(()=>{
        setToast(null);
      },2500)
    }
  },[toast])

  const handleApprove = async () => {
    setUpdatingWorkflow(true);

    try {
      await ActivityService.updateActivityFacilityWorkflow(
        selectedFacility?.id, "APPROVE",
        [], "Approved by Installation Reviewer",
        aggregatedDocuments
      );

      revalidateData();
      dispatch(clearRejectionReasons());
      setToast({
        key: "success",
        message: t("QC_FACILITY_APPROVE_SUCCESS"),
      });

    } catch (error) {
      console.error("Error approving", error);
      setToast({
        key: "error",
        message: t("QC_FACILITY_APPROVE_FAILURE"),
      });
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
      await ActivityService.updateActivityFacilityWorkflow(
        selectedFacility?.id, "REJECT_AND_ASSIGN_FOR_FIELD_QC",
        comments, "Rejected by Installation Reviewer",
        aggregatedDocuments
      );

      revalidateData();
      dispatch(clearRejectionReasons());
      setToast({
        key: "success",
        message: t("QC_FACILITY_REJECT_SUCCESS"),
      });

    } catch (error) {
      console.error("Error rejecting", error);
      setToast({
        key: "error",
        message: t("QC_FACILITY_REJECT_FAILURE"),
      });
    } finally {
      setUpdatingWorkflow(false);
    }
  }

  const handleFlagForQC = async () => {
    setUpdatingWorkflow(true);
    const comments = formatRejectionReasons(rejectionReasons);

    try {
      await ActivityService.updateActivityFacilityWorkflow(
        selectedFacility?.id, "FLAG_FOR_QC",
        comments, "Flagged for QC by Installation Reviewer",
        aggregatedDocuments
      );

      revalidateData();
      dispatch(clearRejectionReasons());
      setToast({
        key: "success",
        message: t("QC_FACILITY_FLAG_FOR_QC_SUCCESS"),
      });

    } catch (error) {
      console.error("Error flagging for QC", error);
      setToast({
        key: "error",
        message: t("QC_FACILITY_FLAG_FOR_QC_FAILURE"),
      });
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
            {t("QC_ACTION_FLAG_FOR_QC")}
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
            {t("CORE_COMMON_REJECT")}
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
          {t("CORE_COMMON_APPROVE")}
        </button>
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

export default QCActions;
