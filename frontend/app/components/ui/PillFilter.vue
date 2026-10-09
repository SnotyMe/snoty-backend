<script setup lang="ts">
import type { TabsProps } from "#ui/components/Tabs.vue"

const model = defineModel<TabsProps["modelValue"]>()

const props = defineProps<{
	ui?: TabsProps["ui"]
}>()

const tabsUi = computed(() => mergeUi(props.ui ?? {}, {
	list: "bg-transparent p-0 gap-2 w-auto",
	trigger: "cursor-pointer rounded-full border border-default data-[state=active]:border-primary data-[state=active]:text-primary",
	indicator: "inset-y-0 rounded-full bg-primary/10 shadow-none",
}))
</script>

<template>
	<UTabs
		v-model="model"
		v-bind="$attrs"
		value-key="value"
		:content="false"
		variant="pill"
		size="sm"
		:ui="tabsUi"
	/>
</template>
