package me.snoty.backend.hooks.impl

import io.ktor.server.routing.*
import me.snoty.backend.hooks.LifecycleHook

fun interface AddRoutesHook : LifecycleHook<Route>
