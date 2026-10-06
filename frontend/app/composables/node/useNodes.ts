import { useApi } from "~/api/backend/api-clients"
import { NodeApi, type NodeCreateRequest, type StandaloneNode } from "~/api/backend/generated"

export const useNodes = (flowId: string) => {
	const nodeApi = useApi(NodeApi)

	const createNode = (createRequest: Omit<NodeCreateRequest, "flowId">): Promise<StandaloneNode> =>
		nodeApi.wiringNodeCreatePost({
			nodeCreateRequest: {
				flowId,
				...createRequest,
			},
		})

	const deleteNode = (nodeId: string) =>
		nodeApi.wiringNodeIdDelete({ id: nodeId })

	return {
		createNode,
		deleteNode,
	}
}
