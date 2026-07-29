import React, { useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-components";
import { useSelector } from "react-redux";
import CreateProject from "./CreateProject";
import Response from "./Response";
import CreateFieldPlan from "./CreateFieldPlan";
import ProjectFieldPlans from "./ProjectFieldPlans";
import ProjectTable from "./ProjectTable";
import ProjectDetails from "./ProjectDetails";
import CreateAMC from "./CreateAMC";
import CreateAssessment from "./CreateAssessment";
import AssessmentDetails from "./AssessmentDetails";
import Translation from "./Translation";
import FieldPlanList from "./FieldPlanList";
import FieldPlanFacilities from "./FieldPlanFacilities";
import FacilityDetails from "./FacilityDetails";

const PMApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();
  const pmStore = useSelector((state) => state.pm.common);

  const breadCrumbsConfig = useMemo(() => ({
    home: {
      content: t("CS_COMMON_HOME"),
      internalLink: `/${window.contextPath}/employee`,
      show: true,
    },
    projectCreation: {
      content: t("PM_ACTION_CREATE_PROJECT"),
      internalLink: match.url + `/project/create`,
      show: true,
    },
    projects: {
      content: t("PM_LABEL_MY_PROJECTS"),
      internalLink: match.url + `/projects`,
      show: true,
    },
    projectFieldPlans: {
      content: pmStore?.workingProject?.name,
      internalLink: match.url + `/project/${pmStore?.workingProject?.id}/field-plans`,
      show: true,
    },
    projectDetails: {
      content: t("CS_COMMON_DETAILS"),
      internalLink: match.url + `/project/${pmStore?.workingProject?.id}/details`,
      show: true,
    },
    fieldPlanCreation: {
      content: t("PM_ACTION_CREATE_FIELD_PLAN"),
      internalLink: match.url + `/project/${pmStore?.workingProject?.id}/field-plan/create`,
      show: true,
    },
    amcCreation: {
      content: t("PM_ACTION_SET_UP_AMC"),
      internalLink: match.url + `/project/${pmStore?.workingProject?.id}/amc/create`,
      show: true,
    },
    assessmentCreation: {
      content: t("PM_ACTION_ADD_ASSESSMENT_PLAN"),
      internalLink: match.url + `/project/${pmStore?.workingProject?.id}/assessment/create`,
      show: true,
    },
    assessmentDetails: {
      content: pmStore?.workingAssessmentPlan?.name,
      internalLink: match.url + `/project/${pmStore?.workingProject?.id}/assessment/${pmStore?.workingAssessmentPlan?.id}/details`,
      show: true,
    },
    fieldPlans: {
      content: t("CS_COMMON_FIELD_PLANS"),
      internalLink: match.url + `/field-plans`,
      show: true,
    },
    fieldPlanFacilities: {
      content: pmStore?.workingFieldPlan?.name,
      internalLink: match.url + `/field-plans/${pmStore?.workingFieldPlan?.id}/facilities`,
      show: true,
    },
    facilityDetails: {
      content: pmStore?.workingFacility?.facilityName || pmStore?.workingFacility?.id,
      internalLink: match.url + `/field-plans/${pmStore?.workingFieldPlan?.id}/facilities/${pmStore?.workingFacility?.id}/details`,
      show: true,
    },
    response: {
      content: t("CORE_COMMON_RESPONSE"),
      internalLink: match.url + `/response`,
      show: true,
    },
    translation: {
      content: t("Translation"),
      internalLink: match.url + `/translation`,
      show: true,
    },
  }), [pmStore]);

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/project/create`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projectCreation]}
          />
          <CreateProject />
        </Route>
        <Route path={`${path}/projects`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projects]}
          />
          <ProjectTable />
        </Route>
        <Route path={`${path}/project/:projectId/field-plans`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projects, breadCrumbsConfig.projectFieldPlans]}
          />
          <ProjectFieldPlans />
        </Route>
        <Route path={`${path}/project/:projectId/details`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projects, breadCrumbsConfig.projectFieldPlans, breadCrumbsConfig.projectDetails]}
          />
          <ProjectDetails />
        </Route>
        <Route path={`${path}/project/:projectId/field-plan/create`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projects, breadCrumbsConfig.projectFieldPlans, breadCrumbsConfig.fieldPlanCreation]}
          />
          <CreateFieldPlan />
        </Route>
        <Route path={`${path}/project/:projectId/amc/create`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projects, breadCrumbsConfig.projectFieldPlans, breadCrumbsConfig.amcCreation]}
          />
          <CreateAMC />
        </Route>
        <Route path={`${path}/project/:projectId/assessment/create`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projects, breadCrumbsConfig.projectFieldPlans, breadCrumbsConfig.assessmentCreation]}
          />
          <CreateAssessment />
        </Route>
        <Route path={`${path}/project/:projectId/assessment/:assessmentId/details`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projects, breadCrumbsConfig.projectFieldPlans, breadCrumbsConfig.assessmentDetails]}
          />
          <AssessmentDetails t={t} />
        </Route>
        <Route path={`${path}/field-plans`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.fieldPlans]}
          />
          <FieldPlanList />
        </Route>
        <Route path={`${path}/field-plans/:fieldPlanId/facilities`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.fieldPlans, breadCrumbsConfig.fieldPlanFacilities]}
          />
          <FieldPlanFacilities />
        </Route>
        <Route path={`${path}/field-plans/:fieldPlanId/facilities/:facilityId/details`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.fieldPlans, breadCrumbsConfig.fieldPlanFacilities, breadCrumbsConfig.facilityDetails]}
          />
          <FacilityDetails />
        </Route>
        <Route path={`${path}/response`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.response]}
          />
          <Response />
        </Route>
        <Route path={`${path}/translation`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.translation]}
          />
          <Translation />
        </Route>
      </Switch>
    </div>
  );
};

export default PMApp;
