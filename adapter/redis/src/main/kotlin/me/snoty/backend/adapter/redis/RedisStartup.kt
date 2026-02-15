package me.snoty.backend.adapter.redis

import io.lettuce.core.ClientOptions
import io.lettuce.core.MaintNotificationsConfig
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.annotation.Single

data class RedisConnectionString(val url: String)

@Single
fun provideRedisConfig(configLoader: ConfigLoader): RedisConnectionString =
    configLoader.load(REDIS_ADAPTER_TYPE)

@Single
fun provideRedisClient(redisConnectionString: RedisConnectionString): RedisClient =
    RedisClient
        .create(RedisURI.create(redisConnectionString.url))
        .apply {
            options = ClientOptions.builder()
                .maintNotificationsConfig(MaintNotificationsConfig.disabled())
                .build()
        }
