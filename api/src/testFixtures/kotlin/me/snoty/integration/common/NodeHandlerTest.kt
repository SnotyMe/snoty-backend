package me.snoty.integration.common

import kotlinx.coroutines.runBlocking
import me.snoty.backend.test.IntermediateDataMapperRegistry
import me.snoty.backend.test.TestCredentialService
import me.snoty.backend.test.getClassNameFromBlock
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.NodeHandleContextImpl
import me.snoty.integration.common.wiring.node.NodeHandler
import org.koin.core.Koin
import org.slf4j.LoggerFactory

fun <T : NodeHandler> runNodeHandlerTest(
	nodeHandler: T,
	block: suspend context(NodeHandleContext, T) () -> Unit
): Unit = runBlocking {
	val nodeHandleContext = NodeHandleContextImpl(
		intermediateDataMapperRegistry = IntermediateDataMapperRegistry,
		credentialService = TestCredentialService,
		logger = LoggerFactory.getLogger(getClassNameFromBlock(block)),
		koin = Koin()
	)
	block(nodeHandleContext, nodeHandler)
}
