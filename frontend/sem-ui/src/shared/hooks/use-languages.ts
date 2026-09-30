import { useQuery } from "@tanstack/react-query";
import { fetchLanguages, type SupportedLanguage } from "../api/mdms";
import { useAuthStore } from "../stores/auth-store";

const FALLBACK_LANGUAGES: SupportedLanguage[] = [
  { code: "en_IN", label: "English", nativeLabel: "English" },
];

export function useLanguages(): SupportedLanguage[] {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);

  const { data } = useQuery({
    queryKey: ["languages"],
    queryFn: () => fetchLanguages(accessToken ?? undefined, user),
    staleTime: Infinity,
  });

  return data && data.length > 0 ? data : FALLBACK_LANGUAGES;
}
