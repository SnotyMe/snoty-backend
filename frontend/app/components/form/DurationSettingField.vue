<i18n lang="yaml">
en:
  hours: Hours
  minutes: Minutes
  seconds: Seconds
  tooShort: Duration too short, has to be at least {min}
  tooLong: Duration too long, has to be at most {max}
de:
  hours: Stunden
  minutes: Minuten
  seconds: Sekunden
  tooShort: Interval zu kurz, muss mindestens {min} sein
  tooLong: Interval zu lang, muss maximal {max} sein
</i18n>

<script setup lang="ts">
import type { SchemaFieldDetailsDurationDetails } from "~/api/backend/generated"

const props = defineProps<{
	details: SchemaFieldDetailsDurationDetails
}>()
const min = Temporal.Duration.from(props.details.min ?? "PT0M")
const max = props.details.max ? Temporal.Duration.from(props.details.max) : undefined

const modelValue = defineModel<string>({ required: true })

const units = [
	"hours",
	"minutes",
	"seconds",
] satisfies (keyof Temporal.DurationLikeObject)[]

const unitItems = computed(() => units.map(it => ({
	value: it,
	label: $t(it),
})))

const initialDuration = Temporal.Duration.from(modelValue.value)
const getInitialUnit = () => {
	const units = [
		"hours" as const,
		"minutes" as const,
		"seconds" as const,
	]

	for (const key of units) {
		if (initialDuration[key] > 0) return key
	}

	return "minutes"
}

const unit = ref(getInitialUnit())
const value = ref(initialDuration[unit.value])

const duration = computed(() => Temporal.Duration.from({ [unit.value]: value.value }))
watch(duration, newDuration => modelValue.value = newDuration.toString())

const errorMessage = computed(() => {
	const durationValue = duration.value

	if (Temporal.Duration.compare(durationValue, min) < 0) return $t("tooShort", { min: min.toLocaleString() })
	if (max && Temporal.Duration.compare(durationValue, max) > 0) return $t("tooLong", { max: max.toLocaleString() })

	return undefined
})
const errorOpen = computed({
	get: () => errorMessage.value != undefined,
	set: () => {},
})
</script>

<template>
	<UFormField :error="errorOpen">
		<UTooltip v-model:open="errorOpen" :portal="false" arrow>
			<div class="flex gap-1">
				<UInput
					v-model="value"
					type="number"
				/>
				<USelect
					v-model="unit"
					:items="unitItems"
				/>
			</div>

			<template #content>
				<p class="text-xs">{{ errorMessage }}</p>
			</template>
		</UTooltip>
	</UFormField>
</template>
