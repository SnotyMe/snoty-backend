package me.snoty.node.schedule

import me.snoty.backend.scheduling.NodeScheduler
import me.snoty.backend.scheduling.Schedulable
import me.snoty.backend.wiring.flow.NodeCreatedHook
import me.snoty.backend.wiring.flow.NodeDeletedHook
import me.snoty.backend.wiring.flow.NodeUpdatedHook
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.core.node.NodeWithSettings
import org.koin.core.annotation.Single

private val NodeSettings.schedule get() = when (this) {
	is Schedulable -> this.schedule
	else -> null
}

@Single
class ScheduleNodeCreatedUpdatedHook(
	private val nodeScheduler: NodeScheduler,
) : NodeUpdatedHook, NodeCreatedHook {
	override suspend fun invoke(node: NodeWithSettings) {
		val schedule = node.settings.schedule ?: return

		nodeScheduler.schedule(node, schedule)
	}
}

@Single
class ScheduleNodeDeletedHook(
	private val nodeScheduler: NodeScheduler,
) : NodeDeletedHook {
	override suspend fun invoke(node: NodeWithSettings) {
		// check if the node has a schedule, if not, we don't need to unschedule it anyway
		node.settings.schedule ?: return

		nodeScheduler.unschedule(node)
	}
}
