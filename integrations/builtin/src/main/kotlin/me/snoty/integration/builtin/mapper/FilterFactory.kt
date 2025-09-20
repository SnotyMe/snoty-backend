package me.snoty.integration.builtin.mapper

import liqp.filters.Filter
import org.koin.core.component.KoinComponent
import org.slf4j.Logger

interface FilterFactory : KoinComponent {
	fun createFilter(logger: Logger): Filter
}
