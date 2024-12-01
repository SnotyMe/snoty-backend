package me.snoty.backend.server.resources.wiring.flow

import io.ktor.server.routing.*
import me.snoty.backend.integration.config.flow.NodeId
import org.koin.ktor.ext.get
import me.snoty.backend.server.plugins.void
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.integration.common.http.flowNotFound
import me.snoty.integration.common.http.invalidNodeId
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow

suspend fun RoutingContext.getPersonalFlowOrNull(): StandaloneWorkflow? {
	val user = call.getUser()
	val id = call.parameters["id"]?.letOrNull { NodeId(it) }
		?: return void { invalidNodeId() }

	val flow = get<FlowService>().getStandalone(id)
	if (flow?.userId != user.id) {
		return void { flowNotFound(flow) }
	}

	return flow
}
