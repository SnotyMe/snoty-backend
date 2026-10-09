<script setup lang="ts">
import { useElkLayout } from "~/composables/flow/useElkLayout"
import { ControlButton, Controls } from "@vue-flow/controls"
import FlowExportButton from "~/components/flow/button/FlowExportButton.vue"
import FlowJournalDrawer from "~/components/flow/button/FlowJournalDrawer.vue"
import NodeAddDrawer from "~/components/flow/button/NodeAddDrawer.vue"
import { useFlowState } from "~/composables/flow/useFlowState.ts"
import { useVueFlow } from "@vue-flow/core"
import type { WorkflowWithNodes } from "~/api/backend/generated"
import { useFlow } from "~/composables/flow/useFlow.ts"

const flow = defineModel<WorkflowWithNodes>({ required: true })

const { renameFlow } = useFlow(flow.value.id)

const { nodes, edges } = useFlowState()
const { fitView } = useVueFlow()

const openDrawer = ref<FlowDrawer | undefined>()

function toggleDrawer(drawer: FlowDrawer) {
	openDrawer.value = openDrawer.value === drawer ? undefined : drawer
}

const { layout } = useElkLayout()

async function doLayoutGraph(direction: "LR" | "TB") {
	const layouted = await layout?.(nodes.value, edges.value, direction)
	if (layouted) {
		nodes.value = layouted
	}

	await nextTick(() => fitView({ padding: "10%" }))
}

const { executeImmediate: layoutGraph } = useAsyncState(doLayoutGraph, null, { immediate: false })
</script>

<template>
	<Controls :show-interactive="false">
		<template #icon-zoom-in>
			<UIcon name="i-lucide-plus"/>
		</template>
		<template #icon-zoom-out>
			<UIcon name="i-lucide-minus"/>
		</template>
		<template #icon-fit-view>
			<UIcon name="i-lucide-scan"/>
		</template>
		<ControlButton @click="layoutGraph('LR')">
			<UIcon name="i-lucide-network"/>
		</ControlButton>
	</Controls>
	<NodeAddDrawer v-model="openDrawer"/>
	<FlowJournalDrawer v-model="openDrawer" :flow-id="flow.id"/>
	<Controls
		position="top-left"
		:show-zoom="false"
		:show-fit-view="false"
		:show-interactive="false"
	>
		<div class="flex items-center gap-2">
			<UDashboardSidebarToggle/>
			<ControlButton @click="toggleDrawer(FlowDrawer.NodeAdd)">
				<UIcon name="i-lucide-plus"/>
			</ControlButton>
			<UInput
				v-model="flow.name"
				variant="ghost"
				color="primary"
				@change="renameFlow(flow.name)"
			/>
		</div>
	</Controls>
	<Controls
		position="top-right"
		:show-zoom="false"
		:show-fit-view="false"
		:show-interactive="false"
	>
		<div>
			<ControlButton @click="toggleDrawer(FlowDrawer.Journal)">
				<UIcon :name="openDrawer === FlowDrawer.Journal ? `i-lucide-scroll-text` : `i-lucide-scroll`"/>
			</ControlButton>
			<FlowExportButton :flow="flow"/>
		</div>
	</Controls>
</template>
