export function extractApiErrorMessage(error: unknown): string | undefined {
  const data = (
    error as {
      response?: {
        data?: {
          Errors?: Array<{ message?: string }>;
          error?: { message?: string; fields?: Array<{ message?: string }> };
        };
      };
    }
  )?.response?.data;

  return (
    (Array.isArray(data?.Errors) ? data?.Errors[0]?.message : undefined) ??
    (Array.isArray(data?.error?.fields) ? data?.error?.fields[0]?.message : undefined) ??
    data?.error?.message
  );
}
