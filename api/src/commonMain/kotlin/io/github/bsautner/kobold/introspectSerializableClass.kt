package io.github.bsautner.kobold

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * This is added to compose generation so at runtime we can interrogate data classes for defaults
 */
@OptIn(ExperimentalSerializationApi::class) @Suppress("unused")
inline fun <reified T> introspectSerializableClass(): List<ConstructorParamInfo> {
    // Obtain the generated serializer for T
    val serial = serializer<T>()
    // Grab its descriptor
    val desc: SerialDescriptor = serial.descriptor

    // Iterate over each element in the descriptor
    return (0 until desc.elementsCount).map { index ->
        val name = desc.getElementName(index)
        val childDescriptor = desc.getElementDescriptor(index)

        ConstructorParamInfo(
            name = name,
            type = childDescriptor.serialName,     // e.g. kotlin.String, kotlin.Int, etc
            hasDefault = desc.isElementOptional(index)  // true if parameter has a default
        )
    }
}

data class ConstructorParamInfo(
    val name: String,
    val type: String,
    val hasDefault: Boolean
)


