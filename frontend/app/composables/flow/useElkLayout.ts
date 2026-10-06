import type { ElkNode } from "elkjs/lib/elk-api"
import type { Edge, Node } from "@vue-flow/core"
import { Position, useVueFlow } from "@vue-flow/core"

const options = {
	"elk.algorithm": "layered",
	"elk.layered.spacing.nodeNodeBetweenLayers": "100",
	// fixes overlapping nodes
	"elk.spacing.edgeNode": "50",
	"elk.edgeRouting": "SPLINES",
}

export const useElkLayout = () => {
	const { findNode } = useVueFlow()

	async function layout(nodes: Node[], edges: Edge[], direction: "LR" | "TB") {
		if (!import.meta.client) return nodes

		const { default: ELK } = await import("elkjs/lib/elk.bundled.js")

		const elk = new ELK()

		const isHorizontal = direction === "LR"
		const elkEdges = edges.map(edge => ({
			...edge,
			sources: [edge.source],
			targets: [edge.target],
		}))
		const elkChildren: ElkNode[] = nodes.map(node => ({
			...node,
			targetPosition: isHorizontal ? Position.Left : Position.Top,
			sourcePosition: isHorizontal ? Position.Right : Position.Bottom,

			width: findNode(node.id)?.dimensions.width || 850,
			height: findNode(node.id)?.dimensions.height || 550,
		}))
		const graph: ElkNode = {
			id: "root",
			layoutOptions: options,
			children: elkChildren,
			edges: elkEdges,
		}

		const layoutedGraph = await elk.layout(graph)

		return layoutedGraph.children?.map(elkNode => ({
			...elkNode,
			targetPosition: isHorizontal ? Position.Left : Position.Top,
			sourcePosition: isHorizontal ? Position.Right : Position.Bottom,
			position: { x: elkNode.x ?? 0, y: elkNode.y ?? 0 },
		})) as Node[]
	}

	return {
		layout,
	}
}
