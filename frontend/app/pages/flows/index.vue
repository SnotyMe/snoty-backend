<script setup lang="ts">
import { useFlows } from "~/composables/flow/useFlows"
import { useFlowListExecutions } from "~/composables/flow/useFlowListExecutions"
import FlowDetails from "~/components/flow/FlowDetails.vue"
import LoadingTexts from "~/components/ui/LoadingTexts.vue"
import FlowOverviewActions from "~/components/flow/overview/FlowOverviewActions.vue"
import FlowOverviewFilters from "~/components/flow/overview/FlowOverviewFilters.vue"
import { useFlowFiltersStore } from "~/stores/flowFilters"
import FlowOverviewNoFlowsFound from "~/components/flow/overview/FlowOverviewNoFlowsFound.vue"

useHead({
	title: "Flows",
})

const { listFlows } = useFlows()
const { state: allFlows, isLoading, executeImmediate: refresh } = useAsyncState(listFlows, null)

const flowFiltersStore = useFlowFiltersStore()

const filteredFlows = computed(() => allFlows.value
	?.filter(it => it.name.toLowerCase().includes(flowFiltersStore.query.toLowerCase()))
	?.sort(flowFiltersStore.sorter(executions)),
)

const { connect, disconnect, executions } = useFlowListExecutions()

onMounted(() => {
	connect()
})

onUnmounted(() => {
	disconnect()
})

const pageSize = ref(10)
const page = ref(1)
const from = computed(() => (page.value - 1) * pageSize.value)
const to = computed(() => Math.min(page.value * pageSize.value, filteredFlows.value?.length ?? 0))

const flows = computed(() => {
	const all = filteredFlows.value
	if (!all) return all

	return all.slice(from.value, to.value)
})

const flowOverviewActions = useTemplateRef("flowOverviewActions")
</script>

<template>
	<UDashboardPanel id="flows" :ui="{ body: 'lg:py-12 mx-auto md:max-w-2xl xl:max-w-3xl w-full' }">
		<template #header>
			<UDashboardNavbar title="Flows">
				<template #right>
					<FlowOverviewActions ref="flowOverviewActions" @refresh="refresh"/>
				</template>
			</UDashboardNavbar>
		</template>

		<template #body>
			<UProgress v-if="isLoading" size="xs"/>
			<LoadingTexts v-if="isLoading" class="text-center"/>
			<div v-if="flows" class="max-h-full grid grid-rows-[auto_1fr] *:w-full bg-muted border border-accented rounded divide-accented divide-y-3">
				<FlowOverviewFilters class="p-2"/>
				<div class="divide-default divide-y overflow-y-auto">
					<FlowDetails
						v-for="flow of flows"
						:key="flow.id"
						:flow="flow"
						:execution="executions.get(flow.id)"
						@refresh="refresh"
					/>
					<FlowOverviewNoFlowsFound v-if="flows.length === 0" @open-create="flowOverviewActions!.openCreate"/>
				</div>
			</div>
		</template>

		<template #footer>
			<div v-if="(flows?.length ?? -1) > 0" class="flex items-center flex-col not-sm:pb-4 not-sm:pt-0 sm:flex-row sm:justify-between gap-2 p-2">
				<p class="pl-1">
					{{
						$t('ui.pagination.info', {
							from: filteredFlows?.length == 0 ? 0 : from + 1,
							to,
							total: filteredFlows?.length ?? '?',
						})
					}}
				</p>
				<UPagination v-model:page="page" :items-per-page="pageSize" :total="filteredFlows?.length"/>
			</div>
		</template>
	</UDashboardPanel>
</template>
