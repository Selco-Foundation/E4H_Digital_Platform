import { SET_REJECTION_REASONS, CLEAR_REJECTION_REASONS } from "../../constants/ReduxActions";

export const rejectionReasonsReducer = (defaultData) => (state = defaultData, action) => {
  switch (action.type) {
    case SET_REJECTION_REASONS:
      return { ...state, [action.payload?.sectionName] : [...action.payload?.reasons] };

    case CLEAR_REJECTION_REASONS :
      return {};

    default:
      return state;
  }
}