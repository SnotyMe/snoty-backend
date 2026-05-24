package me.snoty.integration.plugin.utils.koin

import com.squareup.kotlinpoet.ClassName

data class KoinEntities(
	val scope: KoinScopeReferences,
	val moduleClassName: ClassName,
)
