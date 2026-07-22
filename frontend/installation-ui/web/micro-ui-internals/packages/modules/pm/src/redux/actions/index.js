import { POPULATE_RESPONSE, POPULATE_WORKING_FIELD_PLAN, POPULATE_WORKING_PROJECT, POPULATE_WORKING_ASSESSMENT_PLAN } from "../../constants/ReduxActions";

const populateResponsePage = (responseData) => (dispatch) => {
  dispatch({
    type: POPULATE_RESPONSE,
    payload: responseData,
  })
}

const populateWorkingProject = (project) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_PROJECT,
    payload: project,
  })
}

const populateWorkingFieldPlan = (fieldPlan) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_FIELD_PLAN,
    payload: fieldPlan,
  })
}

const populateWorkingAssessmentPlan = (assessmentPlan) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_ASSESSMENT_PLAN,
    payload: assessmentPlan,
  })
}

export {
  populateResponsePage,
  populateWorkingProject,
  populateWorkingFieldPlan,
  populateWorkingAssessmentPlan
};