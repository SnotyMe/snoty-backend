import type { EnumeratedFlowExecution, FlowId } from "~/types/flow"
import { EventSource } from "eventsource"
import { injectAuth } from "~/utils/fetch"
import { FlowApi } from "~/api/backend/generated"
import { getBaseUrl, useApi } from "~/api/backend/api-clients.ts"

export const useFlowListExecutions = () => {
	const flowApi = useApi(FlowApi)
	const baseUrl = getBaseUrl()

	const listExecutions = async (): Promise<Map<FlowId, EnumeratedFlowExecution>> => {
		const response = await flowApi.wiringFlowListExecutionsGet()

		const executionMap = new Map<FlowId, EnumeratedFlowExecution>()

		response.forEach((execution) => {
			executionMap.set(execution.flowId, execution)
		})

		return executionMap
	}

	const { state: executions, executeImmediate: loadExecutions } = useAsyncState(listExecutions, new Map(), { shallow: false, immediate: false })

	const eventSource = ref<EventSource | null>(null)

	const connect = async () => {
		await loadExecutions()

		const path = baseUrl + (await flowApi.wiringFlowExecutionsSseGetRequestOpts()).path
		eventSource.value = new EventSource(path, {
			fetch: (input, init) => fetch(input, injectAuth(init)),
		})

		eventSource.value.addEventListener("FlowStarted", (event: MessageEvent) => {
			const data: EnumeratedFlowExecution = JSON.parse(event.data)

			executions.value.set(data.flowId, data)
		})

		eventSource.value.addEventListener("FlowEnded", (event: MessageEvent) => {
			const data: EnumeratedFlowExecution = JSON.parse(event.data)

			executions.value.set(data.flowId, data)
		})
	}

	const disconnect = () => {
		if (!eventSource.value) return

		eventSource.value.close()
		eventSource.value = null
	}

	return {
		executions,
		connect,
		disconnect,
	}
}
