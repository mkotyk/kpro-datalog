package com.psyndicate.kprodatalog.serialization

import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType

inline fun <reified T : Any> ByteBuffer.decode(): T = this.decode(T::class)

@ExperimentalUnsignedTypes
@Suppress("UsePropertyAccessSyntax")
fun <T : Any> ByteBuffer.decode(klass: KClass<T>): T {
    val constructor = klass
        .constructors
        .find { it.parameters.isNotEmpty() }
        ?: error("Decode requires data class with one or more arguments in the constructor")
    val constructorParams: Map<KParameter, Any?> = constructor.parameters.map { kParameter ->
        val value: Any = when (kParameter.type) {
            ByteArray::class.createType() -> {
                val size = kParameter
                    .annotations
                    .find { it is SerializedSize }
                    ?.let { (it as SerializedSize).size }
                    ?: error("ByteArray requires SerializedSize annotation")
                ByteArray(size).also { get(it) }
            }
            Long::class.createType() -> getLong()
            Int::class.createType() -> getInt()
            Short::class.createType() -> getShort()
            Byte::class.createType() -> get()
            Boolean::class.createType() -> get()
            UInt::class.createType() -> getInt().toUInt()
            UShort::class.createType() -> getShort().toUShort()
            UByte::class.createType() -> get().toUByte()
            Double::class.createType() -> getDouble()
            Float::class.createType() -> getFloat()
            else -> error("Unsupported type ${kParameter.type} for property ${kParameter.name}")
        }
        kParameter to value
    }.toMap()
    return constructor.callBy(constructorParams)
}
