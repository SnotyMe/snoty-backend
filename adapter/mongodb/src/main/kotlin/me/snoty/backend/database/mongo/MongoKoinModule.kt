package me.snoty.backend.database.mongo

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan("me.snoty.backend")
object MongoKoinModule

val mongoKoinModule = MongoKoinModule.module
