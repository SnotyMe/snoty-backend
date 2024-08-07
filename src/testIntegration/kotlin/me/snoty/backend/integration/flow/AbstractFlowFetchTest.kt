package me.snoty.backend.integration.flow

import me.snoty.integration.common.wiring.RelationalFlowNode

interface FlowTestContext {
	var flow: List<RelationalFlowNode>?
}

/**
 * Abstract class for testing flow fetching.
 *
 * Runs assertions and visualizes the flow in case of an assertion error for easier debugging.
 */
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
