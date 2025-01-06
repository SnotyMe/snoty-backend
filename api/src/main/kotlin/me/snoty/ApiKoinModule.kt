package me.snoty

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan
object ApiKoinModule

val apiModule = ApiKoinModule.module
