import { Request } from "@egovernments/digit-ui-libraries";

/**
 * The signed-in employee's own HRMS record.
 *
 * Identical copies live in core, fa, pm and org — keep them in sync. (Modules may not import from
 * each other; the DIGIT convention here is a copy per module.)
 *
 * The **v2** endpoint, deliberately: v1 and v2 differ in the one thing this is for — the roles each
 * jurisdiction applies to. A jurisdiction with no roles applies to every role the employee holds,
 * which is what every grant made before role scoping looks like.
 */
export const HRMSService = {
  searchByUserName: async (tenantId, userName) =>
    await Request({
      url: "/egov-hrms/v2/employees/_search",
      method: "POST",
      auth: true,
      userService: true,
      params: { tenantId, codes: userName, isActive: true },
    }),
};
