package me.snoty.backend.wiring.flow.execution

import me.snoty.core.FlowId
import me.snoty.core.UserId

object RedisFlowExecutionChannelUtils {
    fun flowChannelName(flowId: FlowId) = "flow-execution:flow:${flowId.value}"
    fun userChannelName(userId: UserId) = "flow-execution:user:${userId.value}"
}
