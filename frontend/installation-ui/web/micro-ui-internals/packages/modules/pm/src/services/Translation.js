import { CustomRequest } from "../components/Custom/CustomRequest";

export const TranslationService = {
  translateExcel: async (translationRequest) => {
    return await CustomRequest({
      url: "/translator/translate/excel",
      data: translationRequest,
      method: "POST",
      auth: true,
      noRequestInfo: true,
      fileDownload: true,
      responseType: "blob",
      defaultFilename: "translated-file.xlsx",
    });
  },
};
