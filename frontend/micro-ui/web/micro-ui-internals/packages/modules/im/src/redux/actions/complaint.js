import { CREATE_COMPLAINT } from "./types";

const createComplaint = ({
  complaintType,
 comments,
 healthcentre,
 subType,
 systemFunctionality,
 uploadImages,
 uploadedFile,
  tenantId
}) => async (dispatch, getState) => {
  const response = await Digit.Complaint.create({
    comments,
    complaintType,
    uploadedFile,
    healthcentre,
    subType,
    systemFunctionality,
    uploadImages,
    tenantId

    
  });
  console.log("res", response)
  dispatch({
    type: CREATE_COMPLAINT,
    payload: response,
  });
};

export default createComplaint;
