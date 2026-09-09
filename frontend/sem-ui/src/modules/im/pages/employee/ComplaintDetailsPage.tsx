import { contextPath, employeeHomePath, translateOr, useTranslate } from "@/shared";
import { Button, TopBar } from "@/ui";
import { Link } from "@tanstack/react-router";
import { Loader2 } from "lucide-react";
import { useMemo } from "react";
import { ComplaintSummarySection } from "../../components/details/ComplaintSummarySection";
import { ComplaintTimelineSection } from "../../components/details/ComplaintTimelineSection";
import { IM_ROUTES } from "../../constants/routes";
import { useComplaintDetails } from "../../hooks/use-complaint-details";
import { isInitialComplaintAction } from "../../utils/complaint-details";

function useComplaintRouteParams() {
  return useMemo(() => {
    const segments = window.location.pathname.split("/").filter(Boolean);
    const detailsIndex = segments.indexOf("details");
    return {
      incidentId: detailsIndex >= 0 ? (segments[detailsIndex + 1] ?? "") : "",
      tenantId: detailsIndex >= 0 ? (segments[detailsIndex + 2] ?? "") : "",
    };
  }, []);
}

export function ComplaintDetailsPage() {
  const { t } = useTranslate();
  const { incidentId, tenantId } = useComplaintRouteParams();
  const basePath = `/${contextPath()}`;
  const homePath = employeeHomePath();
  const inboxPath = `${basePath}${IM_ROUTES.inbox}`;

  const {
    complaintDetails,
    workflowDetails,
    isLoading,
    isError,
    revalidate,
  } = useComplaintDetails(incidentId, tenantId);

  if (!incidentId || !tenantId) {
    return (
      <div className="space-y-4">
        <p className="text-sm text-destructive">
          {translateOr(t, "CS_COMMON_SOMETHING_WENT_WRONG", "Something went wrong!")}
        </p>
        <Button asChild variant="outline" size="lg">
          <Link to={inboxPath}>{translateOr(t, "ES_IM_VIEW_INBOX", "View inbox")}</Link>
        </Button>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex min-h-[320px] items-center justify-center">
        <Loader2 className="size-10 animate-spin text-primary" />
      </div>
    );
  }

  // complaintDetails always resolves now (see use-complaint-details.ts) even when
  // the incident-search response itself comes back empty — DIGIT-UI's own screen
  // shows that as a blank "Ticket Details" section rather than a hard failure, as
  // long as the Timeline (a separate, independent query) has something to show.
  // Only treat this as truly "not found" when both come back empty.
  const hasNothingToShow =
    (complaintDetails?.rows.length ?? 0) === 0 &&
    (workflowDetails?.timeline.length ?? 0) === 0;

  if (isError || !complaintDetails || !workflowDetails || hasNothingToShow) {
    return (
      <div className="space-y-4">
        <p className="text-sm text-destructive">
          {translateOr(t, "CS_COMMON_COMPLAINT_NOT_FOUND", "Ticket not found")}
        </p>
        <Button asChild variant="outline" size="lg">
          <Link to={inboxPath}>{translateOr(t, "ES_IM_VIEW_INBOX", "View inbox")}</Link>
        </Button>
      </div>
    );
  }

  const applyCheckpoint = workflowDetails.timeline.find((checkpoint) =>
    isInitialComplaintAction(checkpoint.performedAction),
  );
  const timelineMediaImages =
    applyCheckpoint?.thumbnailsToShow?.fullImage ?? complaintDetails.images;
  const timelineMediaVideos =
    applyCheckpoint?.thumbnailsToShow?.videos ?? complaintDetails.videos;

  const handleActionComplete = async () => {
    await revalidate();
  };

  return (
    <div className="space-y-6">
      <TopBar
        title={translateOr(t, "CS_HEADER_TICKET_DETAILS", "Ticket Details")}
        breadcrumbs={[
          { label: translateOr(t, "CORE_COMMON_OVERVIEW", "Overview"), to: homePath },
          { label: translateOr(t, "ES_IM_INBOX", "Inbox"), to: inboxPath },
          { label: incidentId },
        ]}
      />

      <ComplaintSummarySection
        complaintDetails={complaintDetails}
        images={timelineMediaImages}
        videos={timelineMediaVideos}
      />

      <ComplaintTimelineSection
        timeline={workflowDetails.timeline}
        complaintDetails={complaintDetails}
        workflowDetails={workflowDetails}
        onActionComplete={handleActionComplete}
      />
    </div>
  );
}
