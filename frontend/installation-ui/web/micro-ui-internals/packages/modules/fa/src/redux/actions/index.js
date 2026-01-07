import { POPULATE_WORKING_FACILITY } from "../../constants/ReduxActions";

const populateWorkingFacility = (facility) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_FACILITY,
    payload: facility,
  });
};

export {
  populateWorkingFacility
};