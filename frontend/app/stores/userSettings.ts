import { useLocalStorage } from "@vueuse/core"

export const useUserSettingsStore = defineStore("userSettings", () => {
	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	const { locale, defaultLocale } = useI18n()

	const dateFormatDefault: Record<typeof defaultLocale, string> = {
		de: "DD.MM.YYYY HH:mm",
		en: "YYYY-MM-DD HH:mm",
	}

	const dateFormatStorage = useLocalStorage<string>(
		"userSettings.dateFormat",
		null,
		{ initOnMounted: true },
	)
	const dateFormat = computed({
		get: () => dateFormatStorage.value ?? dateFormatDefault[locale.value],
		set: value => dateFormatStorage.value = value,
	})

	return {
		dateFormatDefault,
		dateFormat,
	}
})
