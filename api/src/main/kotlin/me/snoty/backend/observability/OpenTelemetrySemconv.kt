package me.snoty.backend.observability

import io.opentelemetry.api.common.AttributeKey

// https://github.com/open-telemetry/semantic-conventions/pull/731
val USER_ID: AttributeKey<String> = AttributeKey.stringKey("user.id")
val JOB_ID: AttributeKey<String> = AttributeKey.stringKey("job.id")
val NODE_ID: AttributeKey<String> = AttributeKey.stringKey("node.id")
val FLOW_ID: AttributeKey<String> = AttributeKey.stringKey("flow.id")
val APPENDER_LOG_LEVEL: AttributeKey<String> = AttributeKey.stringKey("appender.log_level")

val DB_SQL_TABLE: AttributeKey<String> = AttributeKey.stringKey("db.sql.table")
