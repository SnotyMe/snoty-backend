import type { FlowId } from "~/types/flow.ts"
import { getBaseUrl, useApi } from "~/api/backend/api-clients.ts"
import {
	FlowApi,
	type FlowExecutionEventFlowStartedEvent,
	type FlowExecutionEventFlowEndedEvent,
	type WiringFlowIdExecutionsSseGetRequest,
} from "~/api/backend/generated"
import { EventSource } from "eventsource"
import { injectAuth } from "~/utils/fetch.ts"

interface EventTypes {
	FlowStarted: FlowExecutionEventFlowStartedEvent
	FlowEnded: FlowExecutionEventFlowEndedEvent
}
type EventType = keyof EventTypes

const eventTypes: EventType[] = [
	"FlowStarted" as const,
	"FlowEnded" as const,
]

type EventHandler<T extends EventType> = (ev: MessageEvent, data: EventTypes[T]) => void

export const useFlowExecutionSse = (flowId: FlowId) => {
	const flowApi = useApi(FlowApi)
	const baseUrl = getBaseUrl()

	const eventSource = ref<EventSource | null>(null)

	const eventHandlers: Partial<Record<EventType, EventHandler<EventType>[]>> = {}

	const connect = async () => {
		const request: WiringFlowIdExecutionsSseGetRequest = {
			id: flowId,
			eventTypes,
		}
		const path = baseUrl + (await flowApi.wiringFlowIdExecutionsSseGetRequestOpts(request)).path
		eventSource.value = new EventSource(path, {
			fetch: (input, init) => fetch(input, injectAuth(init)),
		})
		eventTypes.forEach(eventType =>
			eventSource.value!.addEventListener(eventType, (ev) => {
				const handlers = eventHandlers[eventType]
				if (handlers == undefined) return

				const data = JSON.parse(ev.data)
				handlers.forEach(handler => handler(ev, data))
			}),
		)

		return eventSource.value
	}

	const disconnect = () => {
		if (!eventSource.value) return

		eventSource.value.close()
		eventSource.value = null
	}

	const addEventHandler = <T extends EventType>(
		eventType: T,
		handler: EventHandler<T>,
	) => {
		const handlers: EventHandler<T>[] = eventHandlers[eventType] ??= []
		handlers.push(handler)
	}

	return {
		connect,
		disconnect,
		addEventHandler,
	}
}

type FlowExecutionSse = ReturnType<typeof useFlowExecutionSse>
const flowExecutionSseKey = Symbol("flowExecutionSse") as InjectionKey<FlowExecutionSse>
export const provideFlowExecutionSse = (flowId: FlowId) => {
	const flowExecutionSse = useFlowExecutionSse(flowId)
	provide(flowExecutionSseKey, flowExecutionSse)

	return flowExecutionSse
}

export const useFlowExecutionEvent = <T extends EventType>(
	eventType: T,
	eventHandler: EventHandler<T>,
) =>
	inject(flowExecutionSseKey)!.addEventHandler(eventType, eventHandler)
