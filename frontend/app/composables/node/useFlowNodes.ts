import { useNodes } from "~/composables/node/useNodes"
import { FLOW_ID } from "~/utils/flowContext"

export const useFlowNodes = () => {
	const flowFromContext = inject(FLOW_ID)

	if (!flowFromContext) {
		throw Error("No Flow in context")
	}

	return useNodes(flowFromContext)
}
