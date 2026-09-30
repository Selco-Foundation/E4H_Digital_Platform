import { zodResolver } from "@hookform/resolvers/zod";
import {
  changePasswordInSession,
  employeeLoginPath,
  employeeProfilePath,
  translateOr,
  useAuthStore,
  useJurisdictionStore,
  useTranslate,
} from "@/shared";
import { Button, Form, TopBar, toast } from "@/ui";
import { Link, useNavigate } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { PasswordChangedDialog } from "../../components/PasswordChangedDialog";
import { PasswordFormField } from "../../components/PasswordFormField";
import {
  passwordConfirmationShape,
  refinePasswordConfirmation,
} from "../../utils/password-confirmation-schema";

function createChangePasswordSchema(t: (key: string) => string) {
  return refinePasswordConfirmation(
    z.object({
      currentPassword: z
        .string()
        .min(1, translateOr(t, "CORE_PROFILE_CURRENT_PASSWORD_REQUIRED", "Current password is required")),
      ...passwordConfirmationShape(t, {
        newRequiredKey: "CORE_PROFILE_NEW_PASSWORD_REQUIRED",
        confirmRequiredKey: "CORE_PROFILE_CONFIRM_PASSWORD_REQUIRED",
        mismatchKey: "CORE_PROFILE_PASSWORD_MISMATCH",
      }),
    }),
    t,
    "CORE_PROFILE_PASSWORD_MISMATCH",
  );
}

type ChangePasswordFormValues = z.infer<ReturnType<typeof createChangePasswordSchema>>;

export function ProfileChangePasswordPage() {
  const { t } = useTranslate();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const clearSession = useAuthStore((state) => state.clearSession);
  const clearJurisdiction = useJurisdictionStore((state) => state.clearJurisdiction);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const changePasswordSchema = useMemo(() => createChangePasswordSchema(t), [t]);

  const form = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
  });

  const [currentPasswordValue, newPasswordValue, confirmPasswordValue] = form.watch([
    "currentPassword",
    "newPassword",
    "confirmPassword",
  ]);
  const canSave = Boolean(currentPasswordValue && newPasswordValue && confirmPasswordValue);

  const onSubmit = async (values: ChangePasswordFormValues) => {
    const userName = user?.userName;
    if (!employeeTenantId || !accessToken || !userName) {
      return;
    }

    setIsSubmitting(true);

    try {
      await changePasswordInSession(
        {
          existingPassword: values.currentPassword,
          newPassword: values.newPassword,
          confirmPassword: values.confirmPassword,
          username: userName,
          tenantId: employeeTenantId,
        },
        accessToken,
        user,
      );

      setIsSuccess(true);
    } catch {
      toast.error(translateOr(t, "CORE_CHANGE_PASSWORD_FAILED", "Failed to update password"));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <TopBar title={translateOr(t, "CORE_CHANGE_PASSWORD_TITLE", "Change Password")} />

      <section className="livelihood-card max-w-2xl space-y-6 p-6">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <PasswordFormField
              control={form.control}
              name="currentPassword"
              label={translateOr(t, "CORE_PROFILE_CURRENT_PASSWORD_LABEL", "Current Password")}
              autoComplete="current-password"
            />

            <PasswordFormField
              control={form.control}
              name="newPassword"
              label={translateOr(t, "CORE_PROFILE_NEW_PASSWORD_LABEL", "New Password")}
              autoComplete="new-password"
            />

            <PasswordFormField
              control={form.control}
              name="confirmPassword"
              label={translateOr(t, "CORE_PROFILE_CONFIRM_PASSWORD_LABEL", "Confirm New Password")}
              autoComplete="new-password"
            />

            <div className="flex items-center gap-4">
              <Button type="submit" size="lg" disabled={isSubmitting || !canSave} className="w-fit">
                {isSubmitting
                  ? translateOr(t, "CORE_CHANGE_PASSWORD_BUTTON_LOADING", "Changing password...")
                  : translateOr(t, "CORE_CHANGE_PASSWORD_BUTTON", "Change Password")}
              </Button>
              <Link
                to={employeeProfilePath()}
                className="text-sm font-medium text-ink-600 hover:underline"
              >
                {translateOr(t, "CORE_COMMON_CANCEL", "Cancel")}
              </Link>
            </div>
          </form>
        </Form>
      </section>

      {isSuccess ? (
        <PasswordChangedDialog
          onConfirm={() => {
            clearSession();
            clearJurisdiction();
            navigate({ to: employeeLoginPath() });
          }}
        />
      ) : null}
    </div>
  );
}
