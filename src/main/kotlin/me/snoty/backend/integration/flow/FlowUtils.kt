package me.snoty.backend.integration.flow

import me.snoty.integration.common.wiring.RelationalFlowNode

fun visualizeFlow(flow: List<RelationalFlowNode>, indent: Int = 1): String {
	val sb = StringBuilder()
	flow.forEach {
		sb.append("\n", "-".repeat(indent-1) + ">", "${it._id} (${it.descriptor})")
		if (it.next.isNotEmpty()) {
			sb.append(visualizeFlow(it.next, indent + 2))
		}
	}
	return sb.toString()
}
