package me.snoty.backend.utils

import me.snoty.backend.integration.config.flow.NodeId
import kotlin.uuid.Uuid

fun NodeId.toUuid() = Uuid.parse(this)
