package me.snoty.backend.wiring.flow.execution

object RedisFlowExecutionChannelUtils {
    fun flowChannelName(flowId: String) = "flow-execution:flow:$flowId"
    fun userChannelName(userId: String) = "flow-execution:user:$userId"
}
