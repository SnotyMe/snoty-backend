package me.snoty.backend.wiring.flow.execution

import me.snoty.core.flow.FlowId
import me.snoty.core.user.UserId

object RedisFlowExecutionChannelUtils {
    fun flowChannelName(flowId: FlowId) = "flow-execution:flow:${flowId.value}"
    fun userChannelName(userId: UserId) = "flow-execution:user:${userId.value}"
}
