package me.snoty.backend.scheduling

import kotlinx.serialization.Serializable

@Serializable
enum class FlowTriggerReason {
	Unknown,
	Scheduled,
	Manual
}
