import { useQuery, useQueryClient } from "react-query";

// Shared cache key. The actions hook invalidates this prefix after every change,
// so the profile status card and the CRM queue refresh without manual wiring.
export const MOBILE_CHANGE_REQUESTS_KEY = "MOBILE_CHANGE_REQUESTS";

/**
 * Reads mobile number change requests (backend: _search).
 *
 *  - HCR / any non-CRM caller: backend returns ONLY their own requests, newest first.
 *    Profile page usage:   useMobileChangeRequests({ criteria: { limit: 1, offset: 0 } })
 *    -> data.requests[0] is the latest request (a PENDING one is always the newest, because
 *       a second request cannot be created while one is pending).
 *  - CRM (COMPLAINT_ASSESSOR): gets everyone's requests.
 *    Approval queue usage: useMobileChangeRequests({ criteria: { status: ["PENDING"], limit: 10, offset: 0 } })
 *
 * @param {{ criteria?: object, enabled?: boolean, config?: object }} options
 *   enabled - pass false until you know the user may use the feature (e.g. only for HCR/CRM)
 *   config  - extra react-query options
 * @returns {{ isLoading, isFetching, isError, error, data: { requests: object[], totalCount: number }, revalidate }}
 */
const useMobileChangeRequests = ({ criteria = {}, enabled = true, config = {} } = {}) => {
  const queryClient = useQueryClient();

  const { isLoading, isFetching, isError, error, data } = useQuery(
    [MOBILE_CHANGE_REQUESTS_KEY, criteria],
    async () => {
      const response = await Digit.FacilityService.searchMobileChangeRequests(criteria);
      return Digit.Utils.mobileChangeRequest.normalizeMobileChangeResponse(response);
    },
    {
      enabled,
      // Status is decided by someone else (CRM), so never serve a stale decision.
      staleTime: 0,
      retry: false,
      refetchOnWindowFocus: true,
      ...config,
    }
  );

  return {
    isLoading,
    isFetching,
    isError,
    error,
    data,
    revalidate: () => queryClient.invalidateQueries(MOBILE_CHANGE_REQUESTS_KEY),
  };
};

export default useMobileChangeRequests;
