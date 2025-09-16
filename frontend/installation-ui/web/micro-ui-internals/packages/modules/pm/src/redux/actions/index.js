import { POPULATE_RESPONSE } from "../../constants/ReduxActions";

const populateResponsePage = (responseData) => (dispatch) => {
  dispatch({
    type: POPULATE_RESPONSE,
    payload: responseData,
  })
}

export {
  populateResponsePage
};