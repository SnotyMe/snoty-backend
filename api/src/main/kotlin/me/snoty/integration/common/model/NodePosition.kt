package me.snoty.integration.common.model

enum class NodePosition(
	val logOutput: Boolean,
) {
	START(
		logOutput = true
	),
	MIDDLE(
		logOutput = true
	),
	END(
		logOutput = false
	)
}
