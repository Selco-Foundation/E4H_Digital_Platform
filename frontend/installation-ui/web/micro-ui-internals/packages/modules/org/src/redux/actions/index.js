import { POPULATE_WORKING_ORGANIZATION } from "../../constants/ReduxActions";

const populateWorkingOrganization = (project) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_ORGANIZATION,
    payload: project,
  })
}

export {
  populateWorkingOrganization,
};