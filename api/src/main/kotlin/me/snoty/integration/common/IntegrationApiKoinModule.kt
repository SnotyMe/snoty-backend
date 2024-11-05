package me.snoty.integration.common

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan
object IntegrationApiModule

val integrationApiModule = IntegrationApiModule.module
