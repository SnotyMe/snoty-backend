package me.snoty.backend.wiring.credential

import me.snoty.integration.common.wiring.NodeHandleContext

class CredentialMissingException(credentialType: String) :
    Exception("Missing credential of type '$credentialType'")

context(context: NodeHandleContext)
suspend inline fun <reified T : Credential> CredentialRef<T>?.resolve(userId: String) = resolveOrNull(userId)
	?: throw missingCredential<T>()

context(context: NodeHandleContext)
suspend inline fun <reified T : Credential> CredentialRef<T>?.resolveOrNull(userId: String): T? =
    this?.let {
        context.credentialService.resolve(userId, this.credentialId, T::class)?.data
            ?: throw missingCredential<T>()
    }

context(context: NodeHandleContext)
inline fun <reified T : Credential> missingCredential() = CredentialMissingException(
    credentialType = context.koin.get<CredentialDefinitionRegistry>().lookupByClass(T::class.java).type,
)
