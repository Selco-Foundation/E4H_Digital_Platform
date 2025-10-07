import { POPULATE_RESPONSE, POPULATE_WORKING_FIELD_PLAN, POPULATE_WORKING_PROJECT } from "../../constants/ReduxActions";

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

export {
  populateResponsePage,
  populateWorkingProject,
  populateWorkingFieldPlan
};