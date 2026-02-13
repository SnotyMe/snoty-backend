package me.snoty.backend.dev

import me.snoty.backend.injection.DiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan
object DevModule {
    val module: DiModule = module()
}
