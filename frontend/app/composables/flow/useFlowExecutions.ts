import { EventSource } from "eventsource"
import { injectAuth } from "~/utils/fetch"
import type { FlowId } from "~/types/flow"
import { getBaseUrl, useApi } from "~/api/backend/api-clients.ts"
import { FlowApi } from "~/api/backend/generated"

export const useFlowExecutions = (flowId: FlowId) => {
	const flowApi = useApi(FlowApi)
	const baseUrl = getBaseUrl()

	const getFlowExecutions = (startFrom: string | undefined = undefined, limit: number = 20) =>
		flowApi.wiringFlowIdExecutionsGet({
			id: flowId,
			startFrom,
			limit: String(limit),
		})

	const eventSource = ref<EventSource | null>(null)

	const connect = async () => {
		const path = baseUrl + (await flowApi.wiringFlowIdExecutionsSseGetRequestOpts({ id: flowId })).path
		eventSource.value = new EventSource(path, {
			fetch: (input, init) => fetch(input, injectAuth(init)),
		})

		return eventSource.value
	}

	const disconnect = () => {
		if (!eventSource.value) return

		eventSource.value.close()
		eventSource.value = null
	}

	return {
		getFlowExecutions,
		connect,
		disconnect,
	}
}
