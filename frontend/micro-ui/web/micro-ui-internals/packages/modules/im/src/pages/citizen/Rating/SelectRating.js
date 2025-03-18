import React, { useCallback, useState } from "react";
import { useDispatch } from "react-redux";
import { RatingCard, CardLabelError } from "@selco/digit-ui-react-components";
import { useParams, Redirect, useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { updateComplaints } from "../../../redux/actions/index";

const SelectRating = ({ parentRoute }) => {
  const { t } = useTranslation();
  const { id } = useParams();
  const dispatch = useDispatch();
  const history = useHistory();

  let tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const complaintDetails = Digit.Hooks.pgr.useComplaintDetails({ tenantId: tenantId, id: id }).complaintDetails;
  const updateComplaint = (complaintDetails) => dispatch(updateComplaints(complaintDetails));
  const [submitError, setError] = useState(false)
  
  function log(data) {
    if (complaintDetails && data.rating > 0 ) {
      complaintDetails.audit.rating = data.rating;
      // complaintDetails.audit.additionalDetail = data.CS_FEEDBACK_WHAT_WAS_GOOD.join(",");
      complaintDetails.workflow = {
        action: "RATE",
        comments: data.comments,
        verificationDocuments: [],
      };
      console.debug("Final", complaintDetails)
      updateComplaint({ service: complaintDetails.service, workflow: complaintDetails.workflow });
      // history.push(`${parentRoute}/response`);
    }
    else{
      setError(true)
    }
  }

  const config = {
    texts: {
      header: "CS_COMPLAINT_RATE_HELP_TEXT",
      submitBarLabel: "CS_COMMON_BUTTON_SUBMIT",
    },
    inputs: [
      {
        type: "rate",
        maxRating: 5,
        label: t("CS_COMPLAINT_RATE_TEXT"),
        starStyles: {cursor: "pointer"},
        error: submitError ? <CardLabelError>{t("CS_FEEDBACK_ENTER_RATING_ERROR")}</CardLabelError> : null
      },
      {
        type: "textarea",
        label: t("CS_COMMON_EMPLOYEE_COMMENTS"),
        name: "comments",
      },
    ],
  };
  return <RatingCard {...{ config: config }} t={t} onSelect={log} />;
};
export default SelectRating;
