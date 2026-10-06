<i18n lang="yaml">
en:
  lastRun: Last run
  createdAt: Created
  modifiedAt: Modified
de:
  lastRun: Letze Ausführung
  createdAt: Erstellt
  modifiedAt: Modifiziert
</i18n>

<script setup lang="ts">
import type { FlowExecution, StandaloneWorkflow } from "~/api/backend/generated"
import DateTimeFormat from "~/components/primitives/DateTimeFormat.vue"
import { calculateColor } from "~/utils/colorLerp"
import { breakpointsTailwind } from "@vueuse/core"
import FlowOverviewFact from "~/components/flow/overview/FlowOverviewFact.vue"

const { t } = useI18n({ useScope: "local" })

const props = defineProps<{
	flow: StandaloneWorkflow
	execution: (FlowExecution & { timestamp: FlowExecution["timestamp"] | string }) | undefined
}>()

const now = useNow({ scheduler: cb => useIntervalFn(cb, 5000) })

const timestampColor = computed(() => {
	if (!props.execution?.timestamp) return undefined

	const date = typeof props.execution.timestamp === "string"
		? new Date(props.execution.timestamp)
		: props.execution.timestamp
	const diff = now.value.getTime() - date.getTime()

	return calculateColor(diff)
})

const breakpoints = useBreakpoints(breakpointsTailwind)
const smallerThanSm = breakpoints.smaller("sm")

const href = computed(() => `/flows/${props.flow.id}`)

const emit = defineEmits<{
	(e: "refresh"): void
}>()
</script>

<template>
	<a
		class="hover:bg-default/50 group grid grid-cols-[3rem_1fr_auto] p-4 text-sm items-center"
		:href="smallerThanSm ? href : undefined"
	>
		<div>
			<FlowExecutionStatus :value="execution?.status ?? undefined" variant="ghost"/>
		</div>

		<div class="flex flex-col">
			<h1 class="font-medium">{{ flow.name }}</h1>

			<div class="*:flex text-muted">
				<div class="separated">
					<FlowOverviewFact v-if="execution" :title="t('lastRun')" icon="i-lucide-clock">
						<DateTimeFormat :date="execution.timestamp" relative="ALWAYS" :style="{ color: timestampColor }"/>
					</FlowOverviewFact>
				</div>
				<div class="separated">
					<FlowOverviewFact :title="t('createdAt')" icon="i-lucide-clock-plus">
						<DateTimeFormat :date="flow.createdAt"/>
					</FlowOverviewFact>
					<FlowOverviewFact :title="t('modifiedAt')" icon="i-lucide-rotate-ccw-clock">
						<DateTimeFormat :date="flow.modifiedAt"/>
					</FlowOverviewFact>
				</div>
			</div>
		</div>

		<FlowOverviewRowActions :flow="flow" @refresh="emit('refresh')"/>
	</a>
</template>

<style>
.separated > *:not(:first-child)::before {
	content: '';
	display: inline-block;
	vertical-align: middle;
	width: 0.25rem;
	height: 0.25rem;
	margin: 0 0.5rem;
	border-radius: 100%;
	background: var(--ui-bg-accented);
}
</style>
