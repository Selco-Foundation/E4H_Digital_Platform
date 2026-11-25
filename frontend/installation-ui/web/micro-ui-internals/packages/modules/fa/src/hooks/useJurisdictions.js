import { useQuery } from "react-query";

import { HRMSService } from "../services/HRMSService";
import { getJurisdictions, setJurisdictions } from "../utilities/jurisdictionScope";

/**
 * The signed-in user's jurisdictions, refreshed once per page load.
 *
 * Identical copies live in fa, pm and org — keep them in sync. (Modules may not import from each
 * other; the DIGIT convention here is a copy per module.)
 *
 * Login stores the grants once, which left them stale for the whole session: an administrator
 * changing someone's jurisdictions had no effect until that person signed out and back in, and the
 * `localStorage` copy outlived the tab. This re-reads them from HRMS so a change lands on the next
 * reload.
 *
 * **Why a query and not a bare fetch.** Every consumer is a hook whose own query key already carries
 * the scope — `["BOUNDARY", boundaryType, codes]`, `["FACILITY", queryFilter]` — so returning the
 * grants through react-query is what makes a refresh re-render those screens and refetch them, with
 * no invalidation to write by hand. Writing to storage alone could not: the readers are synchronous
 * and React has no way to learn the answer changed. One `QueryClientProvider` wraps every module
 * (core's `DigitUI`), so the key is shared and the call is made once however many modules ask.
 *
 * `initialData` is the stored value, so the first paint uses what the last load learned and nothing
 * flashes unscoped while the refresh is in flight. A hard reload starts with an empty cache and
 * `staleTime: 0`, so it always refetches; within a session the shared cache serves the rest.
 *
 * Best-effort, like the fetch at login. On failure react-query keeps the last data, so a brief HRMS
 * outage leaves the previous grants in place rather than widening or blanking the screen.
 */
const useJurisdictions = () => {
  const user = Digit.UserService.getUser()?.info;
  const tenantId = user?.tenantId;
  const userName = user?.userName;

  const { data } = useQuery(
    ["HRMS_JURISDICTIONS", user?.uuid],
    async () => {
      const response = await HRMSService.searchByUserName(tenantId, userName);
      /*
       * An employee with no HRMS record stores an empty list, which the readers treat as "we do
       * not know, do not narrow" - the same answer as never having fetched. Widening is the right
       * failure here: an empty picker blocks the screen with nothing to explain it.
       */
      const jurisdictions = response?.Employees?.[0]?.jurisdictions || [];
      setJurisdictions(jurisdictions);
      return jurisdictions;
    },
    {
      enabled: !!tenantId && !!userName,
      initialData: getJurisdictions,
      staleTime: 0,
      retry: false,
      onError: (error) => console.error("Could not refresh the user's jurisdictions; keeping the stored ones", error),
    }
  );

  return data || [];
};

export default useJurisdictions;
