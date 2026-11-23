import { useCallback, useEffect, useRef, useState } from "react";
import {
  normalizeTollFreeContacts,
  readGlobalConfigContacts,
  TOLL_FREE_MDMS_MASTER,
  TOLL_FREE_MDMS_MODULE,
} from "./tollFreeUtils";

/**
 * Loads the state-wise CRM toll-free numbers BEFORE login.
 *
 * Why this source?
 * (configurable without a code change): the numbers live in the MDMS master `common-masters.CRMTollFreeContact`
 * (state unknown before login): we fetch the master at the STATE-LEVEL tenant
 *    (Digit.ULBService.getStateId(), e.g. "in"), which holds the rows for every state.
 *    No user / jurisdiction information is needed.
 *
 * Which API?
 *  - POST /egov-mdms-service/v1/_search?tenantId=in with
 *      { MdmsCriteria: { tenantId, moduleDetails: [{ moduleName, masterDetails: [{ name }] }] },
 *        RequestInfo: { authToken: null, ... } }
 *
 *ion.
 *
 * @param {boolean} enabled  Fetch only when true (the popup is open). Most users never open the
 *                           popup, so we do not add a network call to every login page load.
 * @returns {{ contacts: Array, isLoading: boolean, isError: boolean, refetch: Function }}
 */
const useCrmTollFreeContacts = (enabled) => {
  const [state, setState] = useState({ contacts: [], isLoading: false, isError: false, loaded: false });
  const [attempt, setAttempt] = useState(0);
  // Remembers which attempt was last fetched, so re-opening the popup does not refetch.
  const fetchedAttempt = useRef(null);
  // Prevents setState after the component unmounted (user navigates away mid-request).
  const isMounted = useRef(true);

  useEffect(() => {
    isMounted.current = true;
    return () => {
      isMounted.current = false;
    };
  }, []);

  useEffect(() => {
    // Do nothing until the popup is opened, or if this attempt was already fetched
    // (closing and re-opening the popup reuses the data; "Retry" bumps `attempt`).
    if (!enabled) return;
    if (fetchedAttempt.current === attempt) return;
    fetchedAttempt.current = attempt;

    const tenantId = window?.Digit?.ULBService?.getStateId?.();

    const load = async () => {
      setState((prev) => ({ ...prev, isLoading: true, isError: false }));

      let mdmsRows = [];
      let mdmsFailed = false;

      try {
        if (!tenantId) throw new Error("State tenant id not available");

        const response = await window.Digit.MDMSService.call(tenantId, {
          moduleDetails: [
            {
              moduleName: TOLL_FREE_MDMS_MODULE,
              masterDetails: [{ name: TOLL_FREE_MDMS_MASTER }],
            },
          ],
        });
        mdmsRows = response?.MdmsRes?.[TOLL_FREE_MDMS_MODULE]?.[TOLL_FREE_MDMS_MASTER] || [];
      } catch (error) {
        // Network / proxy / MDMS error. We still try the globalConfigs fallback below.
        mdmsFailed = true;
        console.error("CRM toll-free: MDMS fetch failed", error);
      }

      let contacts = normalizeTollFreeContacts(mdmsRows);

      // Fallback: only used when MDMS gave us nothing usable.
      if (contacts.length === 0) {
        contacts = normalizeTollFreeContacts(readGlobalConfigContacts());
      }

      if (!isMounted.current) return;

      setState({
        contacts,
        isLoading: false,
        // Only report an error when we truly have nothing to show; if the fallback
        // supplied numbers, the user is still helped and should not see an error.
        isError: mdmsFailed && contacts.length === 0,
        loaded: true,
      });
    };

    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, attempt]);

  const refetch = useCallback(() => setAttempt((n) => n + 1), []);

  return {
    contacts: state.contacts,
    // Treat "open but not fetched yet" as loading so the popup never flashes the empty state.
    isLoading: state.isLoading || (enabled && !state.loaded),
    isError: state.isError,
    refetch,
  };
};

export default useCrmTollFreeContacts;
