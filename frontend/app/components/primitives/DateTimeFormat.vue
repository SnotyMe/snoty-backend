<script setup lang="ts">
import { useUserSettingsStore } from "~/stores/userSettings"

const props = defineProps<{
	date: string | Date
	relative?: "ALWAYS" | "NEVER" | undefined
}>()

const { dateFormat } = useUserSettingsStore()

const { locale } = useI18n()

const date = computed(() => typeof props.date === "string" ? new Date(props.date) : props.date)

const formatted = computed(() => {
	const dateValue = date.value
	if (!dateValue) return undefined

	// last 24 hours -> relative
	if (
		props.relative == "ALWAYS"
		|| (props.relative == undefined && Date.now() - dateValue.getTime() < 24 * 60 * 60 * 1000)
	) {
		return useTimeAgoIntl(dateValue, { scheduler: cb => useIntervalFn(cb, 1000) })
	}

	return useDateFormat(dateValue, dateFormat, { locales: locale })
})
</script>

<template>
	<span :style="$attrs.style">
		{{ formatted }}
	</span>
</template>
