import { useQuery } from "@tanstack/react-query";
import { fetchLoginBannerImages, type LoginBannerImage } from "../api/mdms";
import { useAuthStore } from "../stores/auth-store";

export function useLoginBannerImages(): LoginBannerImage[] {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);

  const { data } = useQuery({
    queryKey: ["loginBannerImages"],
    queryFn: () => fetchLoginBannerImages(accessToken ?? undefined, user),
    staleTime: Infinity,
  });

  return data ?? [];
}
