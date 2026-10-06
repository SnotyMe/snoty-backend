<script setup lang="ts">
import type { JobTriggerResult } from "~/api/backend/generated"
import { useFlowExecutionEvent } from "~/composables/flow/useFlowExecutionSse.ts"

const emit = defineEmits<{
	(e: "trigger", input: object[]): Promise<JobTriggerResult | undefined>
}>()

const triggering = ref<boolean>(false)
const runningJobId = ref<string | null>()

const onTrigger = async () => {
	triggering.value = true
	try {
		const triggerResult = await emit("trigger", [])
		runningJobId.value = triggerResult?.jobId ?? null
	} finally {
		triggering.value = false
	}
}

useFlowExecutionEvent("FlowStarted", (_, data) => {
	const waitingFor = runningJobId.value
	if (waitingFor === null || data.jobId === waitingFor) {
		runningJobId.value = undefined
	}
})
</script>

<template>
	<UButton
		icon="i-lucide-play"
		variant="ghost"
		size="sm"
		:disabled="triggering || runningJobId !== undefined"
		:loading="triggering || runningJobId !== undefined"
		@click="onTrigger"
	/>
</template>
