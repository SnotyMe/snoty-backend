package me.snoty.backend.extension

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.adapter.Adapter
import me.snoty.extension.ExtensionContributor
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.annotation.Single
import org.koin.core.module.Module
import java.util.*
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmName

@Single
class ExtensionContributorLookup(
    private val koin: Koin,
) {
    private val logger = KotlinLogging.logger {}

	fun loadAndRegisterExtensions() {
        val loader = ServiceLoader.load(ExtensionContributor::class.java)
        loader.forEach(::loadExtension)
    }

    @OptIn(KoinInternalApi::class)
    private fun loadExtension(contributor: ExtensionContributor) {
        contributor.koinModule.scope(contributor.koinScope) {
            scoped { contributor }
        }

        contributor.koinModule.filterOutAdapterMappings()

        logger.trace { "Loading Extension ${contributor::class.simpleName} always-on mappings ${contributor.koinModule.mappings.map { it.value.beanDefinition }}" }
        koin.loadModules(listOf(contributor.koinModule))
    }

    @OptIn(KoinInternalApi::class)
    private fun Module.filterOutAdapterMappings() {
        val temporaryKoin = Koin()
        temporaryKoin.loadModules(this.includedModules + this)

        val adapters = temporaryKoin.instanceRegistry.instances
            .map { it.value.beanDefinition.primaryType }
            .filter { it.isSubclassOf(Adapter::class) }
            .distinctBy { it.jvmName }
            .map { temporaryKoin.get<Adapter>(it) }

        this.mappings
            .filter { (key, value) ->
                val filtered = adapters.filter {
                    val adapterModules = it.koinModule.includedModules + it.koinModule
                    adapterModules.any { module -> module.mappings.containsKey(key) }
                }

                if (filtered.isNotEmpty()) {
                    val filteredAdapters = filtered.mapNotNull { it::class.simpleName }
                    logger.trace { "Will remove ${value.beanDefinition} as the Koin Module of the Adapter(s) $filteredAdapters claims ownership" }
                }
                filtered.isNotEmpty()
            }.forEach {
                this.mappings.remove(it.key)
            }
    }
}
