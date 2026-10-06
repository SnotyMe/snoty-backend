<i18n lang="yaml">
en:
  placeholder: Any Log Level
  level:
    TRACE: Trace
    DEBUG: Debug
    INFO: Info
    WARN: Warn
    ERROR: Error
de:
  placeholder: Alle Levels
  level:
    TRACE: Trace
    DEBUG: Debug
    INFO: Info
    WARN: Warnung
    ERROR: Fehler
</i18n>

<script setup lang="ts" generic="M extends LogLevel | LogLevel[]">
import { FlowNodeLogLevelEnum } from "~/api/backend/generated"
import type { LogLevel } from "~/types/node"
import type { SelectMenuProps } from "#ui/components/SelectMenu.vue"
import type { ChipProps } from "#ui/components/Chip.vue"

const { t } = useI18n({ useScope: "local" })

const props = defineProps<{
	multiple?: boolean
	placeholder?: string
}>()

const items = [
	{ value: FlowNodeLogLevelEnum.Trace, color: "neutral", cssColor: "var(--color-log-trace)" },
	{ value: FlowNodeLogLevelEnum.Debug, color: "neutral", cssColor: "var(--color-log-debug)" },
	{ value: FlowNodeLogLevelEnum.Info, color: "success", cssColor: "var(--color-log-info)" },
	{ value: FlowNodeLogLevelEnum.Warn, color: "warning", cssColor: "var(--color-log-warn)" },
	{ value: FlowNodeLogLevelEnum.Error, color: "error", cssColor: "var(--color-log-error)" },
].map(level => ({
	...level,
	label: t(`level.${level.value}`),
	chip: {
		color: level.color as ChipProps["color"],
	},
})) satisfies SelectMenuProps["items"]

const model = defineModel<M | undefined>({ required: true })
const sortedModel = computed<M | undefined>({
	get: () => {
		const modelValue = model.value
		if (!modelValue) return (props.multiple ? [] : undefined) as unknown as M

		return (Array.isArray(modelValue)
			? (modelValue as string[]).toSorted((a, b) => items.findIndex(i => i.value === a) - items.findIndex(i => i.value === b))
			: modelValue) as M
	},
	set: (newValue: M | undefined) => model.value = newValue,
})

const backgroundStyle = computed(() => {
	const modelValue = sortedModel.value
	let values: string[] = Array.isArray(modelValue) ? modelValue : (modelValue ? [modelValue] : [])
	if (values.length === 0) values = items.map(i => i.value as string)

	const colors = values
		.map(it => items.find(item => item.value === it))
		.filter(it => it != undefined)
		.filter(uniqueFilter(it => it.color)).map(it => it.cssColor)
		.filter(it => it != undefined)

	const step = 100 / colors.length
	const gradient = colors.map((color, i) => `${color} ${i * step}% ${(i + 1) * step}%`).join(", ")
	return { background: `conic-gradient(${gradient})` }
})
</script>

<template>
	<USelect
		:model-value="sortedModel"
		:items="items"
		:multiple="multiple"
		:placeholder="placeholder ?? t('placeholder')"
		@update:model-value="sortedModel = $event as M"
	>
		<template
			v-if="model"
			#leading="{ ui }"
		>
			<div
				class="rounded-full"
				:class="ui.itemLeadingChip()"
				:style="[
					{
						width: '1rem',
						height: '1rem',
					},
					backgroundStyle,
				]"
			/>
		</template>
		<template v-if="!multiple && model" #trailing>
			<UIcon
				name="i-lucide-x"
				variant="link"
				class="shrink-0 text-muted size-5"
				@click.stop="model = undefined"
			/>
		</template>
	</USelect>
</template>
