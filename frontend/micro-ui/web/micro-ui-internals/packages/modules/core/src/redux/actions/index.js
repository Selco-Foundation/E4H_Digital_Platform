
export const addLanguageOptions = (languages) => async (dispatch) => {
  dispatch({
    type: "ADD_LANGUAGE_OPTIONS",
    payload: languages,
  });
};