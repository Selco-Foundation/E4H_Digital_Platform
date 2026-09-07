import { zodResolver } from "@hookform/resolvers/zod";
import {
  employeeProfileChangePasswordPath,
  extractApiErrorMessage,
  searchCurrentUser,
  translateOr,
  updateUserProfile,
  useAuthStore,
  useTranslate,
  type EmployeeProfile,
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
  TopBar,
  toast,
} from "@/ui";
import { Link } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";

function createProfileSchema(t: (key: string) => string) {
  return z
    .object({
      name: z
        .string()
        .trim()
        .min(1, translateOr(t, "CORE_PROFILE_NAME_REQUIRED", "Name is required")),
      email: z.string().optional(),
    })
    .refine((data) => !data.email || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email), {
      message: translateOr(t, "CORE_PROFILE_EMAIL_INVALID", "Enter a valid email address"),
      path: ["email"],
    });
}

type ProfileFormValues = z.infer<ReturnType<typeof createProfileSchema>>;

export function ProfilePage() {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const setUser = useAuthStore((state) => state.setUser);

  const [profile, setProfile] = useState<EmployeeProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const profileSchema = useMemo(() => createProfileSchema(t), [t]);

  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      name: "",
      email: "",
    },
  });

  useEffect(() => {
    const uuid = user?.uuid;
    if (!uuid || !employeeTenantId || !accessToken) {
      setIsLoading(false);
      return;
    }

    let isCurrent = true;

    (async () => {
      try {
        const result = await searchCurrentUser(uuid, employeeTenantId, accessToken, user);
        if (!isCurrent) {
          return;
        }

        setProfile(result);
        form.reset({
          name: result?.name ?? "",
          email: result?.emailId ?? "",
        });
      } catch (error) {
        toast.error(translateOr(t, "CORE_PROFILE_LOAD_FAILED", "Failed to load profile"), {
          description:
            extractApiErrorMessage(error) ??
            translateOr(t, "ES_SOMETHING_WRONG", "Something went wrong. Please try again."),
        });
      } finally {
        if (isCurrent) {
          setIsLoading(false);
        }
      }
    })();

    return () => {
      isCurrent = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.uuid, employeeTenantId, accessToken]);

  const onSubmit = async (values: ProfileFormValues) => {
    if (!profile || !employeeTenantId || !accessToken) {
      return;
    }

    setIsSubmitting(true);

    try {
      const { photo: _photo, ...profileWithoutPhoto } = profile;

      await updateUserProfile(
        {
          ...profileWithoutPhoto,
          name: values.name.trim(),
          emailId: values.email?.trim() || undefined,
        },
        employeeTenantId,
        accessToken,
        user,
      );

      if (user) {
        setUser({ ...user, name: values.name.trim() });
      }

      form.reset(values);
      toast.success(translateOr(t, "CORE_PROFILE_UPDATE_SUCCESS", "Profile updated successfully"));
    } catch {
      toast.error(translateOr(t, "CORE_PROFILE_UPDATE_FAILED", "Failed to update profile"));
    } finally {
      setIsSubmitting(false);
    }
  };

  const hasProfileChanges = Boolean(form.formState.dirtyFields.name || form.formState.dirtyFields.email);

  if (isLoading) {
    return (
      <div className="flex min-h-[200px] items-center justify-center text-sm text-muted-foreground">
        {translateOr(t, "CORE_COMMON_LOADING", "Loading...")}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <TopBar title={translateOr(t, "CORE_PROFILE_TITLE", "Profile")} />

      <section className="livelihood-card max-w-2xl space-y-6 p-6">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-6">
            <div className="flex flex-col gap-4">
              <FormField
                control={form.control}
                name="name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-sm leading-[21px] font-medium text-ink-950">
                      {translateOr(t, "CORE_PROFILE_NAME_LABEL", "Name")}{" "}
                      <span className="text-destructive">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input
                        className="h-9 rounded border-ink-300 px-3 py-2 text-sm leading-[21px] text-ink-950"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="flex flex-col gap-2">
                <label className="text-sm leading-[21px] font-medium text-ink-950">
                  {translateOr(t, "CORE_PROFILE_MOBILE_LABEL", "Mobile No.")}
                </label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-3 flex items-center text-sm text-ink-600">
                    +91
                  </span>
                  <Input
                    value={profile?.mobileNumber ?? ""}
                    disabled
                    className="h-9 rounded border-ink-300 bg-muted px-3 py-2 pl-10 text-sm leading-[21px] text-ink-950"
                  />
                </div>
              </div>

              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-sm leading-[21px] font-medium text-ink-950">
                      {translateOr(t, "CORE_PROFILE_EMAIL_LABEL", "Email")}
                    </FormLabel>
                    <FormControl>
                      <Input
                        type="email"
                        className="h-9 rounded border-ink-300 px-3 py-2 text-sm leading-[21px] text-ink-950"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Link
              to={employeeProfileChangePasswordPath()}
              className="self-start text-sm font-medium text-destructive hover:underline"
            >
              {translateOr(t, "CORE_PROFILE_CHANGE_PASSWORD", "Change Password")}
            </Link>

            <Button
              type="submit"
              size="lg"
              disabled={isSubmitting || !hasProfileChanges}
              className="w-fit"
            >
              {isSubmitting
                ? translateOr(t, "CORE_PROFILE_SAVE_LOADING", "Saving...")
                : translateOr(t, "CORE_PROFILE_SAVE", "Save")}
            </Button>
          </form>
        </Form>
      </section>
    </div>
  );
}
