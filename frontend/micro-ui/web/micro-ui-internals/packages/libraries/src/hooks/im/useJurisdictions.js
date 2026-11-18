import { useQuery } from "react-query";
import { getJurisdictions, setJurisdictions } from "../../utils/jurisdictionScope";
import HrmsService from "../../services/elements/HRMS";

/**
 * The user's jurisdictions, re-read from HRMS on every page load.
 *
 * Before this, they were written once at login and never again, so a grant an administrator added
 * or removed mid-session was invisible until the user signed out and back in - and the stale copy
 * was used to *narrow* what they were shown, so the screen quietly hid things the server allowed.
 *
 * Stored grants are the initial data, so the first paint uses them and nothing flashes while the
 * refresh is in flight. A failed refresh keeps what was stored rather than blanking the pickers:
 * HRMS being briefly unreachable is not a reason to tell the user they hold nothing. The server
 * does fail closed on the same outage - that is the right place for it, because there the choice
 * is between showing too much and showing nothing.
 */
const useJurisdictions = () => {
  const user = Digit.UserService.getUser()?.info;
  const tenantId = user?.tenantId;
  const userName = user?.userName;

  const { data } = useQuery(
    ["HRMS_JURISDICTIONS", user?.uuid],
    async () => {
      const response = await HrmsService.v2search(tenantId, null, { codes: userName, isActive: true });
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
