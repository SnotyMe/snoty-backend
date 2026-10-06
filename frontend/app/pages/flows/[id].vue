<script setup lang="ts">
import { ConnectionMode, useVueFlow, VueFlow } from "@vue-flow/core"
import { Background } from "@vue-flow/background"
import Node from "~/components/node/Node.vue"
import { useNodeMetadataStore } from "~/stores/nodeMetadata"
import { ControlButton, Controls } from "@vue-flow/controls"
import { FlowDrawer } from "~/types/drawer"
import { useElkLayout } from "~/composables/flow/useElkLayout"
import NodeAddDrawer from "~/components/flow/button/NodeAddDrawer.vue"
import FlowJournalDrawer from "~/components/flow/button/FlowJournalDrawer.vue"
import { useFlow } from "~/composables/flow/useFlow"
import FlowExportButton from "~/components/flow/button/FlowExportButton.vue"
import { FLOW_ID } from "~/utils/flowContext"
import { nextTick } from "#imports"
import { useOnce } from "~/composables/utils/useOnce"
import { useFlowEdges } from "~/composables/edge/useFlowEdges.ts"
import { snotyNodeToVueFlowNode } from "~/utils/vueFlow.ts"
import { provideFlowState } from "~/composables/flow/useFlowState.ts"
import NodeHelpSidebar from "~/components/node/NodeHelpSidebar.vue"
import { provideFlowExecutionSse } from "~/composables/flow/useFlowExecutionSse"

const { execute: loadNodeMetadata } = useNodeMetadataStore()

const route = useRoute()
const id = route.params.id as string
provide(FLOW_ID, id)

const { getFlow, renameFlow } = useFlow(id)
const { state: flow, execute: loadFlow, isReady: flowDone } = useAsyncState(getFlow, null, { shallow: false })
const { connect, disconnect } = provideFlowExecutionSse(id)

useHead({
	title: () => flow.value?.name,
})

const { layout } = useElkLayout()
const { onNodesInitialized, fitView: fitViewImpl } = useVueFlow()
const fitView = () => fitViewImpl({ padding: "10%" })
const fitViewOnce = useOnce(fitView)

const { nodes, edges } = provideFlowState()

useFlowEdges()

const nodesDone = ref(false)
onMounted(async () => {
	void connect()
	const [flow] = await Promise.all([
		loadFlow().then(it => it!),
		loadNodeMetadata(),
	])

	nodes.value = flow.nodes.map(it => snotyNodeToVueFlowNode(it))
	edges.value = flow.nodes.flatMap(node =>
		(node.next ?? []).map(next => ({
			id: `${node.id}:${next}`,
			source: node.id,
			target: next,
		})),
	)
	nodesDone.value = true
	onNodesInitialized(fitViewOnce)
})

onUnmounted(async () => {
	disconnect()
})

const openDrawer = ref<FlowDrawer | undefined>()
function toggleDrawer(drawer: FlowDrawer) {
	openDrawer.value = openDrawer.value === drawer ? undefined : drawer
}

async function doLayoutGraph(direction: "LR" | "TB") {
	const layouted = await layout?.(nodes.value, edges.value, direction)
	if (layouted) {
		nodes.value = layouted
	}

	await nextTick(() => fitView())
}
const { executeImmediate: layoutGraph } = useAsyncState(doLayoutGraph, null, { immediate: false })

const progress = computed(() => {
	const steps = [
		flowDone,
		nodesDone,
	]

	return (steps
		.map(it => it.value)
		.reduce((acc, done) => acc + (done ? 1 : 0), 0) + 1) * 100 / (steps.length + 1)
})
</script>

<template>
	<VueFlow
		:nodes="nodes"
		:edges="edges"
		:default-edge-options="{ animated: true }"
		:connection-mode="ConnectionMode.Strict"
	>
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
		<FlowJournalDrawer v-if="flow" v-model="openDrawer" :flow-id="flow.id"/>
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
					v-if="flow"
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
				<FlowExportButton v-if="flow" :flow="flow"/>
			</div>
		</Controls>
		<template #edge-default="edge">
			<EdgeButton v-bind="edge"/>
		</template>
		<template #node-default="node">
			<Node
				v-model:log-level="node.data.logLevel"
				v-model:settings="node.data.settings"
				v-model:name="node.data.name"
				:type="node.data.type"
				:highlight="node.data.highlight"
				:data="node.data"
			/>
		</template>
		<Transition leave-active-class="duration-500 ease-out" leave-from-class="opacity-100" leave-to-class="opacity-0">
			<div v-show="progress < 100" class="absolute z-6 size-full flex justify-center items-center">
				<UProgress v-model="progress" class="w-lg"/>
			</div>
		</Transition>
		<Transition leave-active-class="duration-700" leave-from-class="opacity-100" leave-to-class="opacity-0">
			<Background v-if="progress < 100" class="bg-default z-5"/>
		</Transition>
		<Background/>
	</VueFlow>
	<FlowExecutionToast/>
	<NodeHelpSidebar/>
</template>
