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

const getApiErrorMessage = (e) => {
  return (e?.response?.data?.Errors?.[0]?.message)
    ? e.response.data.Errors[0].message
    : (e?.message ? e.message : "");
};

const formatUTCDate = (timestamp, format = "YYYY-MM-DD") => {
  if (!timestamp) return "";

  const ts = String(timestamp).length === 10 ? timestamp * 1000 : timestamp;

  const date = new Date(ts);

  const monthsShort = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  const monthsLong = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
  const daysShort = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const daysLong = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

  const map = {
    YYYY: date.getUTCFullYear(),
    MM: String(date.getUTCMonth() + 1).padStart(2, "0"),
    DD: String(date.getUTCDate()).padStart(2, "0"),
    HH: String(date.getUTCHours()).padStart(2, "0"),
    mm: String(date.getUTCMinutes()).padStart(2, "0"),
    ss: String(date.getUTCSeconds()).padStart(2, "0"),

    MMM: monthsShort[date.getUTCMonth()],
    MMMM: monthsLong[date.getUTCMonth()],
    ddd: daysShort[date.getUTCDay()],
    dddd: daysLong[date.getUTCDay()],
  };

  return format.replace(/MMMM|MMM|YYYY|MM|DD|HH|mm|ss|dddd|ddd/g, (token) => map[token]);
};

export default {
  isNotEqual,
  getApiErrorMessage,
  formatUTCDate,
};