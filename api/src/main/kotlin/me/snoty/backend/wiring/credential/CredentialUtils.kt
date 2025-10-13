package me.snoty.backend.wiring.credential

import me.snoty.integration.common.wiring.NodeHandleContext

class CredentialMissingException(credentialType: String, credentialId: String?) :
	Exception("Missing credential of type '$credentialType' with id '$credentialId'")

context(context: NodeHandleContext)
suspend inline fun <reified T : Credential> CredentialRef<T>.resolve(userId: String) = resolveOrNull(userId)
	?: throw CredentialMissingException(
		credentialType = context.koin.get<CredentialDefinitionRegistry>().lookupByClass(T::class.java).type,
		credentialId = this.credentialId,
	)

context(context: NodeHandleContext)
suspend inline fun <reified T : Credential> CredentialRef<T>.resolveOrNull(userId: String): T? =
	if (this.credentialId == null) null
	else context.credentialService.resolve(userId, this.credentialId, T::class)?.data
