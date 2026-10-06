import type { NavigationMenuItem } from "@nuxt/ui"
import { useAuth } from "~/stores/auth"
import { devBadge } from "~/utils/dev.ts"

export const useNavigation = () => {
	const { t } = useI18n({ useScope: "global" })
	const route = useRoute()

	const { hasRole } = useAuth()
	const { data: isAdmin } = useAsyncData("role-admin", () => hasRole(ROLE_ADMIN))

	const links = computed<[NavigationMenuItem[], NavigationMenuItem[]]>(() => [
		[
			{
				label: t("navigation.page.home"),
				icon: "i-lucide-house",
				to: "/",
			},
			{
				label: t("navigation.page.flows"),
				icon: "i-lucide-workflow",
				to: "/flows",
				active: route.path.startsWith("/flows"),
			},
			{
				label: t("navigation.page.credentials"),
				icon: "i-lucide-key-round",
				to: "/credentials",
			},
			...((isAdmin.value || import.meta.dev)
				? [
						{
							type: "label" as const,
							label: t("navigation.page.admin._"),
						},
						{
							label: t("navigation.page.admin.apiDocs"),
							icon: "i-lucide-webhook",
							to: "/admin/api-docs",
							badge: isAdmin.value ? undefined : devBadge,
						},
					]
				: []),
		],
		[
			{
				label: t("navigation.page.reportIssue"),
				icon: "i-lucide-bug",
				to: "https://github.com/SnotyMe/Snoty/issues/new?template=bug_report.md",
				target: "_blank",
			},
			{
				label: t("navigation.page.documentation"),
				icon: "i-lucide-book-open",
				to: "https://docs.snoty.me",
				target: "_blank",
			},
			{
				label: t("navigation.page.settings._"),
				icon: "i-lucide-settings",
				to: "/settings",
			},
		],
	])

	return {
		links,
	}
}
