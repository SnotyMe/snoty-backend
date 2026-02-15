package me.snoty.backend.wiring.flow.execution

import com.sksamuel.hoplite.ConfigLoaderBuilder
import me.snoty.backend.config.addProperties
import me.snoty.backend.injection.DiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan
object InMemoryKoinModule

class InMemoryExecutionEventAdapter : ExecutionEventAdapter {
    override val supportedTypes: List<String> = listOf("in-memory")
    override val koinModule: DiModule = InMemoryKoinModule.module()

    override fun autoconfigure(configLoader: ConfigLoaderBuilder) {
        configLoader.addProperties(
            "${ExecutionEventAdapter.CONFIG_GROUP}.adapter" to supportedTypes.first()
        )
    }
}
