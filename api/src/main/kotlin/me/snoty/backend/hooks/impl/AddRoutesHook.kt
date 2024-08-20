package me.snoty.backend.hooks.impl

import io.ktor.server.routing.*
import me.snoty.backend.hooks.LifecycleHook

typealias AddRoutesHook = LifecycleHook<Routing>
