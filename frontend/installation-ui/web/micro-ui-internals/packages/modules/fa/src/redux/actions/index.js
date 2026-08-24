import { POPULATE_RESPONSE, POPULATE_WORKING_FACILITY, POPULATE_WORKING_ACTIVITY, POPULATE_WORKING_ASSESSMENT } from "../../constants/ReduxActions";

const populateWorkingFacility = (facility) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_FACILITY,
    payload: facility,
  });
};

const populateResponsePage = (responseData) => (dispatch) => {
  dispatch({
    type: POPULATE_RESPONSE,
    payload: responseData,
  });
};

const populateWorkingActivity = (facility) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_ACTIVITY,
    payload: facility,
  });
};

const populateWorkingAssessment = (assessment) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_ASSESSMENT,
    payload: assessment,
  });
};

export { populateWorkingFacility, populateWorkingActivity, populateWorkingAssessment, populateResponsePage };
