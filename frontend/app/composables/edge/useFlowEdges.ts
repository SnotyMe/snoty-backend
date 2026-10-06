import { useVueFlow } from "@vue-flow/core"
import { useApi } from "~/api/backend/api-clients.ts"
import { NodeApi } from "~/api/backend/generated"

export const useFlowEdges = () => {
	const nodeApi = useApi(NodeApi)

	const { onConnect, onEdgesChange, addEdges } = useVueFlow()

	onConnect(addEdges)

	onEdgesChange((changes) => {
		for (const change of changes) {
			switch (change.type) {
				case "add":
					void nodeApi.wiringNodeConnectPut({
						connectionRequest: {
							from: change.item.source,
							to: change.item.target,
						},
					})
					break
				case "remove":
					void nodeApi.wiringNodeDisconnectPut({
						connectionRequest: {
							from: change.source,
							to: change.target,
						},
					})
					break
			}
		}
	})
}
