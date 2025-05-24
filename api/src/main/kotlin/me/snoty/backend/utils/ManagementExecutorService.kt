package me.snoty.backend.utils

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

const val MANAGEMENT_EXECUTOR = "ManagementPool"

@Single
@Named(MANAGEMENT_EXECUTOR)
fun provideManagementPool(): ScheduledExecutorService =
	Executors.newScheduledThreadPool(1)
