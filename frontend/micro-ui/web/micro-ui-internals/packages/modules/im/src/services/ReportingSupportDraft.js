const SUPPORT_DRAFT_KEY = "IM_REPORTING_SUPPORT_DESCRIPTION";

export const ReportingSupportDraft = {
  read: (facility) => {
    const draft = Digit.SessionStorage.get(SUPPORT_DRAFT_KEY);
    return draft?.userId === Digit.UserService.getUser().info.uuid && draft?.facility === facility ? draft.description : "";
  },
  save: (facility, description) => Digit.SessionStorage.set(SUPPORT_DRAFT_KEY, {
    userId: Digit.UserService.getUser().info.uuid, facility, description,
  }),
  clear: () => Digit.SessionStorage.set(SUPPORT_DRAFT_KEY, null),
};
