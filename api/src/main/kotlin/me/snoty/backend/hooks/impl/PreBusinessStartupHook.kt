package me.snoty.backend.hooks.impl

import me.snoty.backend.hooks.LifecycleHook
import org.koin.core.Koin

/**
 * Starts between initializing integrations and starting the business logic (REST API & scheduler).
 */
fun interface PreBusinessStartupHook : LifecycleHook<Koin>
