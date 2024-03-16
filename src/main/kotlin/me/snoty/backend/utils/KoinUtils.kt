package me.snoty.backend.utils

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

inline fun <reified T> getKoinInstance(): T {
	return object : KoinComponent {
		val value: T by inject()
	}.value
}
