import { POPULATE_RESPONSE, POPULATE_WORKING_FACILITY, POPULATE_WORKING_ACTIVITY } from "../../constants/ReduxActions";

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

export { populateWorkingFacility, populateWorkingActivity, populateResponsePage };
