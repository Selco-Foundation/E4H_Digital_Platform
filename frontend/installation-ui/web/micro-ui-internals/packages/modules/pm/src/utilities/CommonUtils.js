import _ from "lodash";

const normalize = (value) => {
  if (_.isArray(value)) {
    const normalizedArray = value.map(normalize);
    return _.sortBy(normalizedArray, (v) => JSON.stringify(v));

  } else if (_.isPlainObject(value)) {
    const normalizedObj = {};
    Object.keys(value)
      .sort()
      .forEach((key) => {
        normalizedObj[key] = normalize(value[key]);
      });
    return normalizedObj;

  } else if (_.isSet(value)) {
    // Convert Set to sorted normalized array
    return normalize(Array.from(value));

  } else if (_.isMap(value)) {
    // Convert Map to a sorted array of [key, value] pairs
    return normalize(Array.from(value.entries()));

  } else if (_.isDate(value)) {
    // Convert Date to its timestamp for stable comparison
    return value.getTime();
  }

  return value; // primitive, symbol, function, etc. (functions will still be unequal unless same reference)
};

const isNotEqual = (a, b) => {
  const normA = normalize(a);
  const normB = normalize(b);
  return !_.isEqual(normA, normB);
};

const getApiErrorMessages = (e) => {
  const data = e?.response?.data;
  const detailErrors = data?.detail?.errors;

  if (data?.Errors?.[0]?.message) {
    return [data.Errors[0].message];
  }

  if (detailErrors?.length) {
    return detailErrors
      .map((error) => `${error.fileName ? `${error.fileName} ` : ""}${error.error}`);
  }

  if (data?.detail?.message) {
    return [data.detail.message];
  }

  return e?.message ? [e.message] : [];
};

const getApiErrorMessage = (e) => {
  return getApiErrorMessages(e)?.[0] || "";
};

export default {
  isNotEqual,
  getApiErrorMessage,
  getApiErrorMessages,
}
