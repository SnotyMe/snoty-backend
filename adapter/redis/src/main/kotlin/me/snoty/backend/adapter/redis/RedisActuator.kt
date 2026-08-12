package me.snoty.backend.adapter.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.lettuce.RedisHealthCheck
import io.lettuce.core.RedisClient
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("redisHealthCheck")
fun provideRedisHealthCheck(redisClient: RedisClient): HealthCheck =
	RedisHealthCheck(redisClient.connect())
