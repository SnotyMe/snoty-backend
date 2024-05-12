package me.snoty.backend.config

import com.zaxxer.hikari.HikariDataSource

@JvmInline
value class DatabaseConfig(val value: HikariDataSource)
