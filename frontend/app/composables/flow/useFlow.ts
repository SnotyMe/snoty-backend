import type { FlowId } from "~/types/flow"
import { FlowApi, type WorkflowWithNodes } from "~/api/backend/generated"
import { useApi } from "~/api/backend/api-clients"
import { useFlowExecutions } from "~/composables/flow/useFlowExecutions"

export const useFlow = (id: FlowId) => {
	const flowApi = useApi(FlowApi)

	const getFlow = async (): Promise<WorkflowWithNodes> =>
		flowApi.wiringFlowIdGet({ id })

	const renameFlow = async (name: string): Promise<void> =>
		flowApi.wiringFlowIdRenamePut({ id, body: name })

	const deleteFlow = async () =>
		flowApi.wiringFlowIdDelete({ id })

	return {
		getFlow,
		renameFlow,
		deleteFlow,
		executions: useFlowExecutions(id),
	}
}
