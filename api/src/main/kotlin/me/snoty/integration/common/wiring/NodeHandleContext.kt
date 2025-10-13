package me.snoty.integration.common.wiring

import me.snoty.backend.wiring.credential.CredentialService
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import org.koin.core.Koin
import org.slf4j.Logger

interface NodeHandleContext {
	val koin: Koin
	val intermediateDataMapperRegistry: IntermediateDataMapperRegistry
	val credentialService: CredentialService
	val logger: Logger
}

data class NodeHandleContextImpl(
	override val koin: Koin,
	override val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	override val credentialService: CredentialService,
	override val logger: Logger,
) : NodeHandleContext

context(ctx: NodeHandleContext)
inline val logger get() = ctx.logger
