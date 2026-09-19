import { translateOr } from "@/shared";
import { z } from "zod";

interface PasswordConfirmationKeys {
  newRequiredKey: string;
  confirmRequiredKey: string;
  mismatchKey: string;
}

export function passwordConfirmationShape(t: (key: string) => string, keys: PasswordConfirmationKeys) {
  return {
    newPassword: z.string().min(1, translateOr(t, keys.newRequiredKey, "New password is required")),
    confirmPassword: z
      .string()
      .min(1, translateOr(t, keys.confirmRequiredKey, "Confirm password is required")),
  };
}

export function refinePasswordConfirmation<
  Shape extends { newPassword: z.ZodString; confirmPassword: z.ZodString },
>(schema: z.ZodObject<Shape>, t: (key: string) => string, mismatchKey: string) {
  return schema.refine((data) => data.newPassword === data.confirmPassword, {
    message: translateOr(t, mismatchKey, "Passwords do not match"),
    path: ["confirmPassword"],
  });
}
