const STORAGE_KEY = "IM_REPORTING_FIR_DRAFT";

export const ReportingFirDraft = {
  read: (facility, observation) => {
    const draft = Digit.SessionStorage.get(STORAGE_KEY);
    const userId = Digit.UserService.getUser().info.uuid;
    return draft?.userId === userId && draft?.facility === facility && draft?.observation === observation
      ? draft.files : [];
  },
  save: (facility, observation, files) => {
    Digit.SessionStorage.set(STORAGE_KEY, {
      userId: Digit.UserService.getUser().info.uuid,
      facility, observation,
      files: files.filter(([, entry]) => entry?.fileStoreId?.fileStoreId).map(([name, entry]) => [name, {
        file: { name: entry.file.name, type: entry.file.type, size: entry.file.size },
        fileStoreId: entry.fileStoreId,
      }]),
    });
  },
  clear: () => Digit.SessionStorage.set(STORAGE_KEY, null),
};
