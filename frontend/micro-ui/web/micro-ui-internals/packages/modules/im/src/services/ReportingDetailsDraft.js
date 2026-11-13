const DETAILS_DRAFT_KEY = "IM_REPORTING_DETAILS_DRAFT";

export const ReportingDetailsDraft = {
  read: (facility, issue) => {
    const draft = Digit.SessionStorage.get(DETAILS_DRAFT_KEY);
    const userId = Digit.UserService.getUser().info.uuid;
    const current = draft?.userId === userId && draft?.facility === facility && draft?.issue === issue
      ? draft : { userId, facility, issue, description: "", images: [], videos: [] };
    if (!current.draftId) {
      current.draftId = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
      Digit.SessionStorage.set(DETAILS_DRAFT_KEY, current);
    }
    return current;
  },
  save: (facility, issue, changes) => Digit.SessionStorage.set(DETAILS_DRAFT_KEY, {
    ...ReportingDetailsDraft.read(facility, issue), ...changes,
    userId: Digit.UserService.getUser().info.uuid, facility, issue,
  }),
  saveFiles: (facility, issue, kind, files) => ReportingDetailsDraft.save(facility, issue, {
    [kind]: files.filter(([, entry]) => entry?.fileStoreId?.fileStoreId).map(([name, entry]) => [name, {
      file: { name: entry.file.name, type: entry.file.type, size: entry.file.size },
      fileStoreId: entry.fileStoreId,
    }]),
  }),
  clear: () => Digit.SessionStorage.set(DETAILS_DRAFT_KEY, null),
};
