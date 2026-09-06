import { zodResolver } from "@hookform/resolvers/zod";
import {
  assertEmployeeRolesAllowed,
  employeeForgotPasswordPath,
  employeeHomePath,
  filterRolesForEmployeeTenant,
  hydrateEmployeeJurisdictions,
  loginUser,
  resolveQrLogin,
  tenantId,
  translateOr,
  useAuthStore,
  useJurisdictionStore,
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
import { Link, useNavigate, useSearch } from "@tanstack/react-router";
import { Loader2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { AuthLayout } from "../../components/AuthLayout";
import { PasswordFormField } from "../../components/PasswordFormField";
import type { LoginRouteSearch } from "../../routes";

function createLoginSchema(t: (key: string) => string) {
  return z.object({
    username: z.string().min(1, translateOr(t, "CORE_LOGIN_USERNAME_REQUIRED", "Username is required")),
    password: z.string().min(1, translateOr(t, "CORE_LOGIN_PASSWORD_REQUIRED", "Password is required")),
  });
}

type LoginFormValues = z.infer<ReturnType<typeof createLoginSchema>>;

function extractOAuthErrorDescription(error: unknown): string | undefined {
  const response = (
    error as { response?: { data?: { error_description?: string } } }
  )?.response;
  return response?.data?.error_description;
}

function resolveRedirectPath(from?: string): string {
  const fallback = employeeHomePath();

  if (!from) {
    return fallback;
  }

  try {
    return decodeURIComponent(from) || fallback;
  } catch {
    return fallback;
  }
}

export function LoginPage() {
  const { t } = useTranslate();
  const navigate = useNavigate();
  const search = useSearch({ strict: false }) as LoginRouteSearch;
  const from = search.from;
  const prefillUsername = search.username ?? "";
  const qrTenantId = search.tenantId;
  const qrFacilityId = search.facilityId;
  const setSession = useAuthStore((state) => state.setSession);
  const setJurisdictionData = useJurisdictionStore((state) => state.setJurisdictionData);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isResolvingQr, setIsResolvingQr] = useState(Boolean(qrTenantId && qrFacilityId));
  const loginSchema = useMemo(() => createLoginSchema(t), [t]);

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: prefillUsername,
      password: "",
    },
  });

  useEffect(() => {
    if (!qrTenantId || !qrFacilityId) {
      return;
    }

    let cancelled = false;

    resolveQrLogin({ tenantId: qrTenantId, facilityId: qrFacilityId })
      .then((result) => {
        if (!cancelled) {
          form.setValue("username", result.userName);
        }
      })
      .catch(() => {
        if (!cancelled) {
          toast.error(translateOr(t, "CORE_QR_LOGIN_FAILED", "Sign in failed"), {
            description: translateOr(
              t,
              "CORE_QR_LOGIN_FAILED_DESC",
              "We couldn't recognize this QR code. Please log in manually.",
            ),
          });
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsResolvingQr(false);
        }
      });

    return () => {
      cancelled = true;
    };
    // Only ever needs to run once per mount for the tenantId/facilityId this
    // page was opened with.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onSubmit = async (values: LoginFormValues) => {
    setIsSubmitting(true);

    try {
      const response = await loginUser({
        username: values.username.trim(),
        password: values.password.trim(),
        tenantId: tenantId(),
      });

      const userInfo = response.UserRequest;
      if (!userInfo?.tenantId) {
        throw new Error("Could not resolve employee tenant");
      }

      const scopedUser = filterRolesForEmployeeTenant(userInfo, userInfo.tenantId);
      assertEmployeeRolesAllowed(scopedUser);

      const jurisdictionData = await hydrateEmployeeJurisdictions(
        scopedUser,
        response.access_token,
      );

      setSession({
        accessToken: response.access_token,
        refreshToken: response.refresh_token,
        user: scopedUser,
        employeeTenantId: userInfo.tenantId,
      });
      setJurisdictionData(jurisdictionData);

      toast.success(translateOr(t, "CORE_LOGIN_SUCCESS_TOAST", "Signed in successfully"));
      await navigate({ to: resolveRedirectPath(from) });
    } catch (error) {
      const message =
        error instanceof Error && error.message === "ES_ERROR_USER_NOT_PERMITTED"
          ? translateOr(
              t,
              "ES_ERROR_USER_NOT_PERMITTED",
              "You are not permitted to access this application.",
            )
          : (extractOAuthErrorDescription(error) ??
            translateOr(
              t,
              "CS_LOGIN_INVALID_CREDENTIALS",
              "Check your credentials and try again.",
            ));

      toast.error(translateOr(t, "CS_LOGIN_FAILED", "Sign in failed"), {
        description: message,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title={translateOr(t, "CORE_LOGIN_WELCOME_TITLE", "Welcome")}
      subtitle={translateOr(t, "CORE_LOGIN_SUBTITLE", "Please enter your details to login.")}
    >

      {isResolvingQr ? (
        <div className="flex items-center gap-2 rounded border border-ink-300 bg-card px-3 py-2 text-sm text-ink-600">
          <Loader2 className="size-4 shrink-0 animate-spin" />
          {translateOr(t, "CORE_QR_LOGIN_RESOLVING", "Reading QR code details...")}
        </div>
      ) : null}

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="flex w-full flex-col gap-5">
          <div className="flex flex-col gap-4">
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-sm leading-[21px] font-medium text-ink-950">
                    {translateOr(t, "CORE_LOGIN_USERNAME_LABEL", "Username")}{" "}
                    <span className="text-destructive">*</span>
                  </FormLabel>
                  <FormControl>
                    <Input
                      autoComplete="username"
                      disabled={isResolvingQr}
                      placeholder={translateOr(
                        t,
                        "CORE_LOGIN_USERNAME_PLACEHOLDER",
                        "Enter your username",
                      )}
                      className="h-9 rounded border-ink-300 px-3 py-2 text-sm leading-[21px] text-ink-950 placeholder:text-ink-400"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <PasswordFormField
              control={form.control}
              name="password"
              disabled={isResolvingQr}
              label={translateOr(t, "CORE_LOGIN_PASSWORD_LABEL", "Password")}
              placeholder={translateOr(t, "CORE_LOGIN_PASSWORD_PLACEHOLDER", "Enter your password")}
              autoComplete="current-password"
              headerExtra={
                <Link
                  to={employeeForgotPasswordPath()}
                  className="text-sm leading-[21px] font-medium text-primary hover:underline"
                >
                  {translateOr(t, "CORE_LOGIN_FORGOT_PASSWORD", "Forgot Password?")}
                </Link>
              }
            />
          </div>

          <Button
            type="submit"
            size="lg"
            disabled={isSubmitting || isResolvingQr}
            className="w-full"
          >
            {isSubmitting
              ? translateOr(t, "CORE_LOGIN_BUTTON_LOADING", "Logging in...")
              : translateOr(t, "CORE_LOGIN_BUTTON", "Log in")}
          </Button>
        </form>
      </Form>
    </AuthLayout>
  );
}
