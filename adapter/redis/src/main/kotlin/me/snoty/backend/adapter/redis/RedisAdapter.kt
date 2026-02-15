package me.snoty.backend.adapter.redis

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

const val REDIS_ADAPTER_TYPE = "redis"

val SUPPORTED_TYPES = listOf("redis", "valkey")

@Module
@ComponentScan
object RedisKoinModule
