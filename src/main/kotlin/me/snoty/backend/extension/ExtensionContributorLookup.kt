package me.snoty.backend.extension

import me.snoty.extension.ExtensionContributor
import org.koin.core.Koin
import org.koin.core.annotation.Single
import java.util.*

@Single
class ExtensionContributorLookup(
    private val koin: Koin,
) {
    fun loadAndRegisterExtensions() {
        val loader = ServiceLoader.load(ExtensionContributor::class.java)
        loader.forEach { contributor ->
            koin.loadModules(listOf(contributor.koinModule))
        }
    }
}
