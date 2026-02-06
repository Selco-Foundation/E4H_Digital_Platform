import { Request } from "@egovernments/digit-ui-libraries";

export const LocalizationService = {

  upsertLocalization: async (localizationPayload) => {
    const endpoint = "/localization/messages/v1/_upsert";
    const headers = {
      "Content-Type": "application/json",
    };

    return await Request({
      url: endpoint,
      data: localizationPayload,
      method: "POST",
      userService: true,
      auth: true,
      headers: headers,
    });
  },

};
