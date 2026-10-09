<script setup lang="ts">
import { ConnectionMode, useVueFlow, VueFlow } from "@vue-flow/core"
import { Background } from "@vue-flow/background"
import Node from "~/components/node/Node.vue"
import { useNodeMetadataStore } from "~/stores/nodeMetadata"
import { useFlow } from "~/composables/flow/useFlow"
import { FLOW_ID } from "~/utils/flowContext"
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

const { getFlow } = useFlow(id)
const { state: flow, execute: loadFlow, isReady: flowDone } = useAsyncState(getFlow, null, { shallow: false })
const { connect, disconnect } = provideFlowExecutionSse(id)

useHead({
	title: () => flow.value?.name,
})

const { onNodesInitialized, fitView } = useVueFlow()
const fitViewOnce = useOnce(() => fitView({ padding: "10%" }))

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
		<FlowControls v-if="flow" v-model="flow"/>
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
