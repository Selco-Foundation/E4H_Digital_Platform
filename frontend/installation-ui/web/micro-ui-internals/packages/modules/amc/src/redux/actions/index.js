import {
  PROJECT_SELECT,
  VISIT_SELECT
} from "../../constants/ReduxActions";

const populateWorkingProject = (fieldPlan) => (dispatch) => {
  dispatch({
    type: PROJECT_SELECT,
    payload: fieldPlan,
  })
}

const populateWorkingVisit = (visit) => (dispatch) => {
  dispatch({
    type: VISIT_SELECT,
    payload: visit,
  })
}

export {
  populateWorkingProject,
  populateWorkingVisit
};