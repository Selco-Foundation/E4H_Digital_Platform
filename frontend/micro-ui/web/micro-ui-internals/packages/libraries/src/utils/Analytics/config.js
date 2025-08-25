export const GA_MEASUREMENT_ID =
  process.env.REACT_APP_GA_ID || 'G-ZDFDM34WK4';

export const IS_PROD = process.env.NODE_ENV === 'production';
export const DEBUG_MODE = !IS_PROD;
