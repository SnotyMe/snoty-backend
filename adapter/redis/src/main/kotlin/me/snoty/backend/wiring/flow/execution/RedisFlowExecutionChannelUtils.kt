package me.snoty.backend.wiring.flow.execution

import me.snoty.core.UserId

object RedisFlowExecutionChannelUtils {
    fun flowChannelName(flowId: String) = "flow-execution:flow:$flowId"
    fun userChannelName(userId: UserId) = "flow-execution:user:${userId.value}"
}
