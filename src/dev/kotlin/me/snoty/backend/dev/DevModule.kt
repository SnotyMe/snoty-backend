package me.snoty.backend.dev

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan
object DevModule

val devModule = DevModule.module
