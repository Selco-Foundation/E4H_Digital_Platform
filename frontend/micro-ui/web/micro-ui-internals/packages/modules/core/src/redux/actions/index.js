
export const addLanguageOptions = (languages) => async (dispatch) => {
  dispatch({
    type: "ADD_LANGUAGE_OPTIONS",
    payload: languages,
  });
};

export const addStateLogos = (logos) => async (dispatch) => {
  dispatch({
    type: "ADD_STATE_LOGOS",
    payload: logos,
  })
}

export const setCrmHelplineNumber = (crmHelplineNumber) => async (dispatch) => {
  dispatch({
    type: "SET_CRM_HELPLINE_NUMBER",
    payload: crmHelplineNumber,
  })
}