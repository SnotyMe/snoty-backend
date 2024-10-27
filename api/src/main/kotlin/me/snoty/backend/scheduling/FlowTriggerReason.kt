package me.snoty.backend.scheduling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class FlowTriggerReason {
	@Serializable
	@SerialName("Unknown")
	data object Unknown : FlowTriggerReason()

	@Serializable
	@SerialName("Scheduled")
	data object Scheduled : FlowTriggerReason()

	@Serializable
	@SerialName("Manual")
	data object Manual : FlowTriggerReason()
}
