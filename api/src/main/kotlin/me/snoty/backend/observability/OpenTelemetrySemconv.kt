package me.snoty.backend.observability

import io.opentelemetry.api.common.AttributeKey

// https://github.com/open-telemetry/semantic-conventions/pull/731
val USER_ID = AttributeKey.stringKey("user.id")
val JOB_ID = AttributeKey.stringKey("job.id")
val FLOW_ID = AttributeKey.stringKey("flow.id")
val APPENDER_LOG_LEVEL = AttributeKey.stringKey("appender.log_level")
