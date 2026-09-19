export function translateOr(t: (key: string) => string, key: string, fallback: string): string {
  const value = t(key);
  return value === key ? fallback : value;
}
