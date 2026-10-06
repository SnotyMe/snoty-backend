import type { FlowNodeLogLevelEnum } from "~/api/backend/generated"

export type NodeType = string

export type DynamicFormValues = Record<string, never>

export type LogLevel = `${FlowNodeLogLevelEnum}`
