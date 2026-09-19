import { fetchMdmsMasters, tenantId, useAuthStore } from "@/shared";
import { useQuery } from "@tanstack/react-query";

export type PolicyDescriptionType = "points" | "step" | null;

export interface PolicyDescription {
  text: string;
  type?: PolicyDescriptionType;
  isBold?: boolean;
  isSpaceRequired?: boolean;
  subDescriptions?: PolicyDescription[];
}

export interface PolicyContent {
  header: string;
  isSpaceRequired?: boolean;
  descriptions?: PolicyDescription[];
}

export interface PolicyDocument {
  header?: string;
  module?: string;
  active?: boolean;
  contents?: PolicyContent[];
}

export type PolicyDocumentType = "privacy" | "terms";

const POLICY_MASTER_NAMES: Record<PolicyDocumentType, string> = {
  privacy: "PrivacyPolicy",
  terms: "TermsOfUse",
};

interface UsePolicyDocumentOptions {
  type?: PolicyDocumentType;
  /** Matches DIGIT-UI's own hardcoded default — the value in each MDMS
   * record's own `module` field, not an MDMS moduleName. Selects among
   * multiple product-specific documents sharing the same `commonUiConfig`
   * masters. */
  module?: string;
}

/**
 * Matches DIGIT-UI's usePolicyDocument.js exactly: module `commonUiConfig`,
 * masters `PrivacyPolicy` / `TermsOfUse`, selecting the record whose own
 * `module` field matches (falling back to the first active one, then the
 * first record at all).
 */
export function usePolicyDocument({ type = "privacy", module = "E4H" }: UsePolicyDocumentOptions = {}) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const masterName = POLICY_MASTER_NAMES[type] ?? POLICY_MASTER_NAMES.privacy;
  const stateTenantId = tenantId();

  return useQuery({
    queryKey: ["policy-document", stateTenantId, masterName, module],
    queryFn: async (): Promise<PolicyDocument | undefined> => {
      const masters = await fetchMdmsMasters(
        stateTenantId,
        "commonUiConfig",
        [masterName],
        accessToken ?? undefined,
        user,
      );
      const policies = (masters[masterName] as PolicyDocument[]) ?? [];
      return (
        policies.find((policy) => policy.module === module) ??
        policies.find((policy) => policy.active) ??
        policies[0]
      );
    },
  });
}
