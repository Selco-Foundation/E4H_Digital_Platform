import React, { useCallback, useState } from "react";
import { useDispatch } from "react-redux";
import { RatingCard, CardLabelError } from "@selco/digit-ui-react-components";
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { updateComplaints } from "../../../redux/actions/index";

const SelectRating = ({ parentRoute, complaintDetails }) => {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const history = useHistory();

  const [submitError, setSubmitError] = useState(false)

  const handleComplaintUpdate = useCallback((complaintData) => {
    dispatch(updateComplaints(complaintData));
  }, [dispatch]);

  const navigateToResponsePage = useCallback(() => {
    history.push(`${parentRoute}/incident/response`);
  }, [history, parentRoute]);

  const handleRatingSubmit = useCallback((data) => {
    if (!complaintDetails || data.rating <= 0) {
      setSubmitError(true);
      return;
    }

    const updatedComplaintDetails = {
      ...complaintDetails,
      workflow: {
        action: "RATE",
        comments: data.comments,
        rating: data.rating,
        verificationDocuments: [],
      }
    };
    
    handleComplaintUpdate(updatedComplaintDetails);
    navigateToResponsePage();
    
    setSubmitError(false);
  }, [complaintDetails, handleComplaintUpdate, navigateToResponsePage]);

  const config = {
    texts: {
      header: "CS_COMPLAINT_RATE_HELP_TEXT",
      submitBarLabel: "CS_COMMON_BUTTON_SUBMIT",
    },
    inputs: [
      {
        type: "rate",
        maxRating: 5,
        label: "",
        styles: { marginLeft: 'auto', marginRight: 'auto' },
        starStyles: { cursor: "pointer", marginRight: "unset" },
        error: submitError ? <CardLabelError>{t("CS_FEEDBACK_ENTER_RATING_ERROR")}</CardLabelError> : null
      },
      {
        type: "textarea",
        label: t("CS_COMMON_EMPLOYEE_COMMENTS"),
        name: "comments",
      },
    ],
  };
  return (
    <RatingCard 
      config={config} 
      t={t} 
      onSelect={handleRatingSubmit} 
    />
  );
};
export default SelectRating;
