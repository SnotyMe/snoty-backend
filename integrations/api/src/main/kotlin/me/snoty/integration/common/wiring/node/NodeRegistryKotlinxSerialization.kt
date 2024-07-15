package me.snoty.integration.common.wiring.node

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Terrible workaround for whatever the hell the kotlinx.serialzation team was thinking when they
 * implemented arbitrary limitations regarding open polymorphism.
 * If they didn't create these shitty, nonsensical, static limitations, we wouldn't need this.
 */
@OptIn(InternalSerializationApi::class)
fun NodeRegistry.serializersModule() = SerializersModule {
	polymorphic(NodeSettings::class) {
		this@serializersModule.getHandlers().values.forEach {
			fun <T : NodeSettings> sc(clazz: KClass<T>) {
				subclass(clazz, clazz.serializer())
			}
			sc(it.settingsClass)
		}
	}
}
