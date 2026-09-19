package me.snoty.backend.wiring.flow.execution

import me.snoty.backend.adapter.redis.RedisKoinModule
import me.snoty.backend.adapter.redis.SUPPORTED_TYPES
import me.snoty.backend.injection.DiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [RedisKoinModule::class])
@ComponentScan
object RedisExecutionEventKoinModule

@Single
class RedisExecutionEventAdapter : ExecutionEventAdapter {
    override val supportedTypes: List<String> = SUPPORTED_TYPES
    override val koinModule: DiModule = RedisExecutionEventKoinModule.module()
}
