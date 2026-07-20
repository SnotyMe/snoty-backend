package me.snoty.integration.common.wiring.node

import kotlinx.serialization.Serializable

@Serializable
data class NodePosition(
	val x: Int,
	val y: Int,
	val width: Int,
	val height: Int,
)
