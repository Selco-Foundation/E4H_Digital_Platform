import { zodResolver } from "@hookform/resolvers/zod";
import {
  employeeLoginPath,
  extractApiErrorMessage,
  sendPasswordResetOtp,
  tenantId,
  translateOr,
  useTranslate,
} from "@/shared";
import {
  Button,
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Input,
  toast,
} from "@/ui";
import { Link } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { AuthLayout } from "../../components/AuthLayout";

function createForgotPasswordSchema(t: (key: string) => string) {
  return z.object({
    mobileNumber: z
      .string()
      .regex(
        /^[6-9]\d{9}$/,
        translateOr(
          t,
          "CORE_FORGOT_PASSWORD_MOBILE_INVALID",
          "Enter a valid 10-digit mobile number",
        ),
      ),
  });
}

type ForgotPasswordFormValues = z.infer<ReturnType<typeof createForgotPasswordSchema>>;

export function ForgotPasswordPage() {
  const { t } = useTranslate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const forgotPasswordSchema = useMemo(() => createForgotPasswordSchema(t), [t]);

  const form = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      mobileNumber: "",
    },
  });

  const onSubmit = async (values: ForgotPasswordFormValues) => {
    setIsSubmitting(true);

    try {
      await sendPasswordResetOtp({ mobileNumber: values.mobileNumber, tenantId: tenantId() });
      // The next step (OTP entry + new password) isn't implemented yet — for
      // now just confirm the OTP was sent. Once that page exists, navigate to
      // employeeChangePasswordPath() with { mobileNumber: values.mobileNumber }
      // in the search params, same as before.
      toast.success(
        translateOr(t, "CORE_FORGOT_PASSWORD_OTP_SENT_TOAST", "OTP sent to your mobile number"),
      );
    } catch (error) {
      toast.error(translateOr(t, "CS_FORGOT_PASSWORD_OTP_FAILED", "Failed to send OTP"), {
        description:
          extractApiErrorMessage(error) ??
          translateOr(t, "ES_INVALID_LOGIN_CREDENTIALS", "Check your mobile number and try again."),
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title={translateOr(t, "CORE_FORGOT_PASSWORD_TITLE", "Forgot Password?")}
      subtitle={translateOr(
        t,
        "CORE_FORGOT_PASSWORD_SUBTITLE",
        "Enter your registered mobile number to reset your password.",
      )}
    >
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="flex w-full flex-col gap-5">
          <FormField
            control={form.control}
            name="mobileNumber"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-sm leading-[21px] font-medium text-ink-950">
                  {translateOr(t, "CORE_FORGOT_PASSWORD_MOBILE_LABEL", "Mobile Number")}{" "}
                  <span className="text-destructive">*</span>
                </FormLabel>
                <FormControl>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-3 flex items-center text-sm text-ink-600">
                      +91
                    </span>
                    <Input
                      type="tel"
                      inputMode="numeric"
                      autoComplete="tel"
                      maxLength={10}
                      placeholder={translateOr(
                        t,
                        "CORE_FORGOT_PASSWORD_MOBILE_PLACEHOLDER",
                        "Enter your mobile number",
                      )}
                      className="h-9 rounded border-ink-300 px-3 py-2 pl-10 text-sm leading-[21px] text-ink-950 placeholder:text-ink-400"
                      {...field}
                    />
                  </div>
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <Button type="submit" size="lg" disabled={isSubmitting} className="w-full">
            {isSubmitting
              ? translateOr(t, "CORE_FORGOT_PASSWORD_BUTTON_LOADING", "Sending OTP...")
              : translateOr(t, "CORE_FORGOT_PASSWORD_BUTTON", "Send OTP")}
          </Button>

          <Link
            to={employeeLoginPath()}
            className="text-center text-sm leading-[21px] font-medium text-primary hover:underline"
          >
            {translateOr(t, "CORE_FORGOT_PASSWORD_BACK_TO_LOGIN", "Back to login")}
          </Link>
        </form>
      </Form>
    </AuthLayout>
  );
}
