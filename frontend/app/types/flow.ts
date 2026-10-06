import type { EnumeratedFlowExecutionStatusEnum } from "~/api/backend/generated"

export interface EnumeratedFlowExecution {
	flowId: string
	timestamp: Date
	status: EnumeratedFlowExecutionStatusEnum
}

export type FlowId = string
