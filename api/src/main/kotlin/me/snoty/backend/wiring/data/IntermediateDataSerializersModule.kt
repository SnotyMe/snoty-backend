package me.snoty.backend.wiring.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

const val INTERMEDIATE_DATA_SERIALIZERS_MODULE = "IntermediateDataSerializerModule"

@Single
@Named(INTERMEDIATE_DATA_SERIALIZERS_MODULE)
fun provideIntermediateDataSerializersModule(intermediateDataMapperRegistry: IntermediateDataMapperRegistry) =
    SerializersModule {
        intermediateDataMapperRegistry.getAll()
            .forEach {
                if (it !is KSerializer<*>) return@forEach

                @Suppress("UNCHECKED_CAST")
                fun <T : IntermediateData> polymorphic() =
                    polymorphic(
                        baseClass = IntermediateData::class,
                        actualClass = it.intermediateDataClass as KClass<T>,
                        it as KSerializer<T>,
                    )

                polymorphic<IntermediateData>()
            }
    }
