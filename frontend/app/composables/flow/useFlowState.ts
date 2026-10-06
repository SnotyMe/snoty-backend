import { type Edge as VueEdge, type Node as VueNode, useVueFlow } from "@vue-flow/core"
import { snotyNodeToVueFlowNode } from "~/utils/vueFlow.ts"
import type { StandaloneNode } from "~/api/backend/generated"

export type FlowState = ReturnType<typeof createFlowState>

/**
 * Handles the synchronization of Nodes and Edges of a singular Flow with VueFlow.
 * Does NOT communicate with the Backend!
 */
const createFlowState = () => {
	const { removeNodes, setCenter } = useVueFlow()

	const nodes = shallowRef<VueNode[]>([])
	const edges = shallowRef<VueEdge[]>([])

	const createNode = async (created: StandaloneNode) => {
		const vueFlowNode = snotyNodeToVueFlowNode(created, { highlight: true })
		nodes.value = [
			...nodes.value,
			vueFlowNode,
		]
		await setCenter(
			vueFlowNode.position.x + (vueFlowNode.dimensions?.width ?? created.position.width) / 2,
			vueFlowNode.position.y + (vueFlowNode.dimensions?.height ?? created.position.height) / 2,
			{ zoom: 1, duration: 500 },
		)
	}

	const deleteNode = (nodeId: string) => {
		nodes.value = nodes.value.filter(it => it.id !== nodeId)
		removeNodes(nodeId)
	}

	return {
		nodes,
		edges,
		createNode,
		deleteNode,
	}
}

const flowStateKey = Symbol("flowState") as InjectionKey<FlowState>

export const provideFlowState = () => {
	const flowState = createFlowState()
	provide(flowStateKey, flowState)

	return flowState
}
export const useFlowState = (): FlowState => {
	const composable = inject(flowStateKey)
	if (!composable) throw Error("No Flow State in context")

	return composable
}
