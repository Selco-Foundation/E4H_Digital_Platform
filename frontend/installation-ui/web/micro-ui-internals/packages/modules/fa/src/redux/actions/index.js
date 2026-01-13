import { POPULATE_RESPONSE, POPULATE_WORKING_FACILITY } from "../../constants/ReduxActions";

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

export {
  populateWorkingFacility, populateResponsePage
};