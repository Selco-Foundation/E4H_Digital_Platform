import { translateOr, useTranslate } from "@/shared";
import { PolicyDocumentContent } from "../../components/PolicyDocumentContent";
import { usePolicyDocument, type PolicyDocumentType } from "../../hooks/use-policy-document";

interface TermsPrivacyPolicyPageProps {
  type: PolicyDocumentType;
}

const FALLBACK_TITLE: Record<PolicyDocumentType, string> = {
  privacy: "Privacy Policy",
  terms: "Terms of Use",
};

/**
 * One screen, parametrized by `type` — same shape as DIGIT-UI's
 * TermsPrivacyPolicy, which maps both /employee/privacy-policy and
 * /employee/terms-of-use to this same component with a different `type`.
 */
export function TermsPrivacyPolicyPage({ type }: TermsPrivacyPolicyPageProps) {
  const { t } = useTranslate();
  const { data: documentData, isLoading } = usePolicyDocument({ type });

  if (isLoading) {
    return (
      <div className="flex min-h-[200px] items-center justify-center text-sm text-muted-foreground">
        {translateOr(t, "CORE_COMMON_LOADING", "Loading...")}
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <div className="flex justify-end">
        <button
          type="button"
          onClick={() => window.history.back()}
          className="cursor-pointer text-sm font-medium text-destructive hover:underline"
        >
          {translateOr(t, "PP_COMMON_BACK", "Back")}
        </button>
      </div>
      <section className="livelihood-card space-y-6 p-6">
        <h1 className="text-2xl font-bold text-ink-950">
          {translateOr(t, documentData?.header ?? "", FALLBACK_TITLE[type])}
        </h1>
        <PolicyDocumentContent documentData={documentData} />
      </section>
    </div>
  );
}
