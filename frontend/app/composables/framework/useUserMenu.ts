import type { DropdownMenuItem } from "@nuxt/ui"
import { useLocale } from "@nuxt/ui/composables/useLocale"
import { useAppConfig } from "#app"

export const useUserMenu = () => {
	const { t } = useI18n()
	const { t: uiT } = useLocale()
	const { locale, setLocale, locales } = useI18n()
	const colorMode = useColorMode()

	const appConfig = useAppConfig()

	const colorModes = computed(() => {
		/* eslint-disable @typescript-eslint/no-explicit-any */
		const icons = (appConfig.ui as any).icons
		return [
			{ label: uiT("colorMode.system"), value: "system", icon: icons.system },
			{ label: uiT("colorMode.light"), value: "light", icon: icons.light },
			{ label: uiT("colorMode.dark"), value: "dark", icon: icons.dark },
		]
	})

	const { user, logout } = useAuth()

	const items = computed<[DropdownMenuItem[], DropdownMenuItem[]]>(() => ([
		[
			{
				label: t("ui.locale"),
				icon: "i-lucide-languages",
				children:
					locales.value.map(loc => ({
						label: loc.name,
						type: "checkbox",
						checked: locale.value === loc.code,
						onSelect(e: Event) {
							setLocale(loc.code)
							e.preventDefault()
						},
					})),
			},
			{
				label: t("ui.colorScheme"),
				icon: "i-lucide-sun-moon",
				children: colorModes.value.map(({ label, value, icon }) => ({
					label,
					icon,
					type: "checkbox",
					checked: colorMode.preference === value,
					onSelect(e: Event) {
						colorMode.preference = value
						e.preventDefault()
					},
				})),
			},
		],
		[
			{
				label: t("ui.copyId"),
				icon: "i-lucide-copy",
				onSelect: () => navigator.clipboard.writeText(user?.id ?? ""),
			},
			{
				label: t("ui.logOut"),
				icon: "i-lucide-log-out",
				onSelect: logout,
			},
		],
	]))

	return {
		items,
	}
}
