import { useMutation, useQueryClient } from "react-query";
import { MOBILE_CHANGE_REQUESTS_KEY } from "./useMobileChangeRequests";

/**
 * The four write operations on a mobile number change request, as react-query mutations.
 *
 *   const { create, cancel, approve, reject } = Digit.Hooks.im.useMobileChangeRequestActions();
 *   create.mutate({ newMobileNumber }, { onSuccess, onError });
 *   cancel.mutate({ id, comment });                // requester, PENDING only
 *   approve.mutate({ id, comment });               // CRM, PENDING only
 *   reject.mutate({ id, comment });                // CRM, PENDING only
 *
 * Each mutation resolves with the normalised { requests, totalCount } (single request in requests[0]).
 * On failure the axios error is passed to onError; turn it into a message with
 *   Digit.Utils.mobileChangeRequest.getMobileChangeErrorKey(error)
 *
 * Every success invalidates the shared query so the profile card / CRM queue refetch.
 */
const useMobileChangeRequestActions = () => {
  const queryClient = useQueryClient();
  const normalize = (response) => Digit.Utils.mobileChangeRequest.normalizeMobileChangeResponse(response);
  const refresh = () => queryClient.invalidateQueries(MOBILE_CHANGE_REQUESTS_KEY);

  // A failed action can also mean the request changed under us (e.g. CRM processed it a second
  // ago -> NOT_PENDING), so refetch on error too and let the UI show the true current state.
  const options = { onSuccess: refresh, onError: refresh };

  const create = useMutation((payload) => Digit.FacilityService.createMobileChangeRequest(payload).then(normalize), options);
  const cancel = useMutation((payload) => Digit.FacilityService.cancelMobileChangeRequest(payload).then(normalize), options);
  const approve = useMutation((payload) => Digit.FacilityService.approveMobileChangeRequest(payload).then(normalize), options);
  const reject = useMutation((payload) => Digit.FacilityService.rejectMobileChangeRequest(payload).then(normalize), options);

  return { create, cancel, approve, reject };
};

export default useMobileChangeRequestActions;
