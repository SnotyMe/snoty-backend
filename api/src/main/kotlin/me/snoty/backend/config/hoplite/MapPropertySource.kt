package me.snoty.backend.config.hoplite

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource

typealias MapSource = () -> Map<String, String>

class MapPropertySource(
	private val mapSource: MapSource,
	private val prefix: String = "",
) : SystemPropertiesPropertySource() {
	override fun node(context: PropertySourceContext): ConfigResult<Node> {
		val map = mapSource().filter { it.key.startsWith(prefix) }
		return if (map.isEmpty()) Undefined.valid() else map.toNode("sysprops") {
			it.removePrefix(prefix)
		}.valid()
	}
}

val systemPropertiesSourceMap: MapSource = {
	val systemProperties = System.getProperties()
	systemProperties.stringPropertyNames().associateWith { propertyName ->
		systemProperties.getProperty(propertyName)
	}
}

val envSourceMap: MapSource = {
	System.getenv()
}
