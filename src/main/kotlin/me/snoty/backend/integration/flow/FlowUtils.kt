package me.snoty.backend.integration.flow

import me.snoty.backend.integration.flow.model.FlowNode

fun visualizeFlow(flow: List<FlowNode>, indent: Int = 1): String {
	val sb = StringBuilder()
	flow.forEach {
		sb.append("-".repeat(indent-1) + ">", "${it.id} (${it.descriptor})\n")
		if (it.next.isNotEmpty()) {
			sb.append("\n", visualizeFlow(it.next, indent + 2))
		}
	}
	return sb.toString()
}
