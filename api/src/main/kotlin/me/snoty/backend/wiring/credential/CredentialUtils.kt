package me.snoty.backend.wiring.credential

import me.snoty.integration.common.wiring.NodeHandleContext

context(context: NodeHandleContext)
suspend inline fun <reified T : Credential> CredentialRef<T>.resolve(userId: String) =
	if (this.credentialId == null) null
	else context.credentialService.resolve(this.credentialId, T::class, userId)
