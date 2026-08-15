package me.snoty.core.node

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class NodeId(val value: String)
