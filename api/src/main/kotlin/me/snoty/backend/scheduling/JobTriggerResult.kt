package me.snoty.backend.scheduling

import kotlinx.serialization.Serializable

@Serializable
data class JobTriggerResult(
    val jobId: String,
)
