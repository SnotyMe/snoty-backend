import { useApi } from "~/api/backend/api-clients"
import { NodeApi, type NodePosition, StandaloneNodeLogLevelEnum } from "~/api/backend/generated"

export const useFlowNode = (nodeId: string) => {
	const nodeApi = useApi(NodeApi)

	const updateName = (name: string) =>
		nodeApi.wiringNodeIdPatch({ id: nodeId, nodePatchRequest: { name } })

	const updatePosition = (position: NodePosition) =>
		nodeApi.wiringNodeIdPatch({ id: nodeId, nodePatchRequest: { position } })

	const updateLogLevel = (logLevel: StandaloneNodeLogLevelEnum | undefined) =>
		nodeApi.wiringNodeIdPatch({ id: nodeId, nodePatchRequest: { logLevel: (logLevel as never as object) ?? null } })

	const updateSettings = (settings: object) =>
		nodeApi.wiringNodeIdPut({ id: nodeId, body: settings })

	const trigger = (input: object[]) =>
		nodeApi.wiringNodeIdTriggerPost({
			id: nodeId, nodeTriggerRequest: {
				logLevel: StandaloneNodeLogLevelEnum.Debug,
				input,
			},
		})

	return {
		updateName,
		updateSettings,
		updatePosition,
		updateLogLevel,
		trigger,
	}
}
