import React, { useEffect, useMemo, useState } from "react";
import useProject from "../../hooks/useProject";
import InfoCard from "../../components/ProjectFieldPlans/InfoCard";
import { Loader } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { populateWorkingProject } from "../../redux/actions";
import { useDispatch } from "react-redux";
import IntroModal from "../../components/IntroModal";
import FieldPlanTable from "../../components/ProjectFieldPlans/FieldPlanTable";
import AssessmentTable from "../../components/ProjectFieldPlans/AssessmentTable";
import AMCTable from "../../components/ProjectFieldPlans/AMCTable";

const ProjectFieldPlans = () => {

  const { t } = useTranslation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const history = useHistory();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const [createdProject, setCreatedProject] = useState(null);
  const dispatch = useDispatch();
  const [introModalData, setIntroModalData] = useState(null);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { isLoading: projectDataLoading, data: projectData } = useProject({
    id: [projectId],
  });

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData])

  const handleFieldPlanCreationNavigation = () => {
    history.push(`/${window.contextPath}/employee/pm/project/${projectId}/field-plan/create`);
  }

  const handleAMCCreationNavigation = () => {
    history.push(`/${window.contextPath}/employee/pm/project/${projectId}/amc/create`);
  }

  const handleAssessmentCreationNavigation = () => {
    history.push(`/${window.contextPath}/employee/pm/project/${projectId}/assessment/create`);
  }

  const sections = useMemo(() => [
    {
      key: "ASSESSMENTS",
      heading: t("PM_LABEL_ASSESSMENT_PLANS"),
      buttonLabel: t("PM_ACTION_ADD_ASSESSMENT_PLAN"),
      showAddIcon: true,
      action: handleAssessmentCreationNavigation,
      introTitle: "PM_BEFORE_CREATING_ASSESSMENT_PLAN_TITLE",
      introSubTitle: "PM_BEFORE_CREATING_ASSESSMENT_PLAN_SUBTITLE",
      introDescription: "PM_BEFORE_CREATING_ASSESSMENT_PLAN_DESC",
      Table: AssessmentTable,
    },
    {
      key: "FIELD_PLANS",
      heading: t("CS_COMMON_FIELD_PLANS"),
      buttonLabel: t("PM_ACTION_ADD_FIELD_PLAN"),
      showAddIcon: true,
      action: handleFieldPlanCreationNavigation,
      introTitle: "PM_BEFORE_CREATING_FIELD_PLAN_TITLE",
      introSubTitle: "PM_BEFORE_CREATING_FIELD_PLAN_SUBTITLE",
      introDescription: "PM_BEFORE_CREATING_FIELD_PLAN_DESC",
      Table: FieldPlanTable,
    },
    {
      key: "AMC",
      heading: t("PM_LABEL_AMCS"),
      buttonLabel: t("PM_ACTION_SET_UP_AMC"),
      showAddIcon: false,
      action: handleAMCCreationNavigation,
      introTitle: "PM_BEFORE_CREATING_AMC_TITLE",
      introSubTitle: "PM_BEFORE_CREATING_AMC_SUBTITLE",
      introDescription: "PM_BEFORE_CREATING_AMC_DESC",
      Table: AMCTable,
    },
  ], [t, projectId]);

  if (projectDataLoading) {
    return <Loader />
  }

  return (
    <div style={{padding: mobileView ? "15px" : "0px"}}>
      {createdProject?.name && (
        <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
          {createdProject?.name}
        </div>
      )}
      {createdProject && (<InfoCard t={t} project={createdProject} />)}
      {sections.map((section) => {
        const SectionTable = section.Table;
        return (
          <div key={section.key} style={{marginTop: "40px"}}>
            <div style={{display: "flex", gap: "15px", alignItems: "center", justifyContent: "space-between", marginBottom: "25px"}}>
              <div style={{fontSize: "32px", fontWeight: "bold", fontFamily: "Roboto Condensed", color: "#0B0C0C"}}>
                {section.heading}
              </div>
              <button
                type="button"
                className={"jk-digit-secondary-btn"}
                style={{
                  display: "flex",
                  justifyContent: "space-around",
                  alignItems: "center",
                  height: "32px",
                  padding: "0px 20px",
                  cursor: !createdProject?.status ? "default" : "pointer",
                  opacity: !createdProject?.status ? "0.5" : "1",
                }}
                disabled={!createdProject?.status}
                onClick={() => setIntroModalData({
                  action: section.action,
                  title: section.introTitle,
                  subTitle: section.introSubTitle,
                  description: section.introDescription,
                })}
              >
                {section.showAddIcon && (
                  <span
                    style={{
                      width: "18px",
                      height: "18px",
                      borderRadius: "5px",
                      background: "#C84C0E",
                      color: "white",
                      fontSize: "20px",
                      fontWeight: "bold",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      marginRight: "10px"
                    }}
                  >
                    +
                  </span>
                )}
                <span
                  style={{
                    fontSize: "16px",
                    fontWeight: "500",
                    fontFamily: "Roboto"
                  }}
                >
                  {section.buttonLabel}
                </span>
              </button>
            </div>
            <SectionTable t={t} projectId={projectId} />
          </div>
        );
      })}
      <IntroModal
        open={!!introModalData}
        onClose={() => setIntroModalData(null)}
        t={t}
        action={introModalData?.action}
        title={introModalData?.title}
        subTitle={introModalData?.subTitle}
        description={introModalData?.description}
      />
    </div>
  )
}

export default ProjectFieldPlans;
