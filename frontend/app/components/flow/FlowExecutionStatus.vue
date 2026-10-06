<script setup lang="ts">
import { EnumeratedFlowExecutionStatusEnum } from "~/api/backend/generated"

const statusMap: Record<EnumeratedFlowExecutionStatusEnum, {
	color: string
	icon: string
	spin?: boolean
}> = {
	[EnumeratedFlowExecutionStatusEnum.Running]: {
		color: "info",
		icon: "i-lucide-loader-2",
		spin: true,
	},
	[EnumeratedFlowExecutionStatusEnum.Success]: {
		color: "success",
		icon: "i-lucide-check",
	},
	[EnumeratedFlowExecutionStatusEnum.Failed]: {
		color: "error",
		icon: "i-lucide-x",
	},
}

const props = defineProps<{
	value: EnumeratedFlowExecutionStatusEnum | undefined
	variant: "ghost" | "link"
}>()

const status = computed(() => {
	const config = ((props.value && statusMap[props.value]) || {
		color: "neutral",
		icon: "i-lucide-circle-question-mark",
	})

	return {
		style: {
			color: `var(--ui-color-${config.color}-500)`,
			borderColor: `color-mix(in oklch, var(--ui-color-${config.color}-500) 20%, transparent)`,
			backgroundColor: `color-mix(in oklch, var(--ui-color-${config.color}-500) 10%, transparent)`,
		},
		icon: config.icon,
		spin: config.spin ?? false,
	}
})
</script>

<template>
	<div
		v-if="variant === 'ghost'"
		class="flex items-center justify-center w-8 h-8 rounded-full shadow-sm border"
		:class="status.spin ? 'animate-spin' : ''"
		:style="status.style"
	>
		<UIcon :key="status.icon" :name="status.icon"/>
	</div>
	<UIcon
		v-else
		:key="status.icon"
		:name="status.icon"
		:style="`color: ${status.style.color}`"
	/>
</template>
