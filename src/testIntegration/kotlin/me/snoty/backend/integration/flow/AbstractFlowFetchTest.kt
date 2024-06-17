package me.snoty.backend.integration.flow

import me.snoty.backend.integration.flow.model.FlowNode

interface FlowTestContext {
	var flow: List<FlowNode>?
}

abstract class AbstractFlowFetchTest<C : FlowTestContext>(
	private val buildContext: () -> C
) {
	internal fun test(block: C.() -> Unit) {
		val context = buildContext()
		try {
			block(context)
		} catch (e: AssertionError) {
			val flow = context.flow
			if (flow != null) {
				println("Flow:\n${visualizeFlow(flow)}")
			}
			throw e
		}
	}
}
