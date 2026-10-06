import type { FlowNode } from "~/api/backend/generated"
import type { Node as VueNode } from "@vue-flow/core"

export const snotyNodeToVueFlowNode = (node: FlowNode, additionalData?: Record<string, unknown>) => ({
	id: node.id,
	position: node.position,
	width: node.position.width,
	height: node.position.height,
	// @ts-expect-error setting the field does have an impact so idk
	dimensions: { width: node.position.width, height: node.position.height },
	data: {
		...node,
		...additionalData,
	},
	type: "default",
}) satisfies VueNode
