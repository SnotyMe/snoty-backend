package me.snoty.core.user

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class UserId(val value: String)
