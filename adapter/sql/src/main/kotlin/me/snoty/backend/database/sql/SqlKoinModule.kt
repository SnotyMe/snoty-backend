package me.snoty.backend.database.sql

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan("me.snoty.backend")
object SqlKoinModule

val sqlKoinModule = SqlKoinModule.module
