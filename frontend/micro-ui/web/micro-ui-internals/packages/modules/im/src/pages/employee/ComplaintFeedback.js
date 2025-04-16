import React, { useEffect, useState } from "react";
import { Redirect, useParams } from "react-router-dom";
import SelectRating from "./Rating/SelectRating";
import { Loader } from "@selco/digit-ui-react-components";

const ComplaintFeedback = ({ parentRoute }) => {
    const { incidentId, tenantId } = useParams();
    const [isEligible, setIsEligible] = useState(false);
    const [hasCheckedEligibility, setHasCheckedEligibility] = useState(false);

    const { isLoading, complaintDetails } = Digit.Hooks.pgr.useComplaintDetails({
        tenantId,
        id: incidentId,
    });

    useEffect(() => {
        if (isLoading) return;

        const currentLoginUser = JSON.parse(sessionStorage.getItem("Digit.User"))?.value?.info?.uuid;
        const isReportedByCurrentUser = currentLoginUser === complaintDetails?.incident?.reporter?.uuid;
        const complaintStatus = complaintDetails?.incident?.applicationStatus;
        const latestAction = complaintDetails?.workflow?.action;

        if (
            isReportedByCurrentUser && 
            complaintStatus === "CLOSEDAFTERRESOLUTION" && 
            latestAction === "CLOSE"
        ) {
            setIsEligible(true);
        }

        setHasCheckedEligibility(true);
    }, [isLoading, complaintDetails]);

    if (isLoading || !hasCheckedEligibility) {
        return <Loader />;
    }

    if (!isEligible) {
        return <Redirect to={`/${window.contextPath}/employee`} />;
    }

    return <SelectRating parentRoute={parentRoute} complaintDetails={complaintDetails} />;
};

export default ComplaintFeedback;