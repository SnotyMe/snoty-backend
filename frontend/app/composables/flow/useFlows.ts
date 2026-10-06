import { useApi } from "~/api/backend/api-clients"
import { FlowApi, type FlowCreateRequest, type ImportFlow } from "~/api/backend/generated"

export const useFlows = () => {
	const flowApi = useApi(FlowApi)

	const createFlow = async (workflow: FlowCreateRequest) =>
		flowApi.wiringFlowPost({ flowCreateRequest: workflow })

	const importFlow = async (flow: ImportFlow) =>
		flowApi.wiringFlowImportPost({ importFlow: flow })

	const listFlows = async () =>
		flowApi.wiringFlowListGet()

	return {
		createFlow,
		importFlow,
		listFlows,
	}
}
