package me.snoty.backend.integration.config.mapping

data class Mappable(
	val mappingType: MappingType,
	val data: String
)

enum class MappingType {
	STATIC,
	JEKYLL
}
