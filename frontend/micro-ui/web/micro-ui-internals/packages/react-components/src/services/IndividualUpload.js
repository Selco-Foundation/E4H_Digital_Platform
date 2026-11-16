export const IndividualUploadService = {
  upload: async (module, file, tenantId, onProgress) => {
    const response = await Digit.UploadServices.MultipleFilesStorage(module, [file], tenantId, file.type.startsWith("video/"), onProgress);
    const reference = response?.data?.files?.[0];
    if (!reference?.fileStoreId) throw new Error("Upload response did not contain a file reference");
    return reference;
  },
};
