package me.snoty.backend.extension

import me.snoty.extension.ExtensionContributor
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.core.qualifier.StringQualifier
import java.util.*

@Single
class ExtensionContributorLookup(
    private val koin: Koin,
) {
    fun loadAndRegisterExtensions() {
        val loader = ServiceLoader.load(ExtensionContributor::class.java)
        loader.forEach { contributor ->
            contributor.koinModule.scope(StringQualifier(contributor.koinScope)) {
                scoped { contributor }
            }
            koin.loadModules(listOf(contributor.koinModule))
        }
    }
}
