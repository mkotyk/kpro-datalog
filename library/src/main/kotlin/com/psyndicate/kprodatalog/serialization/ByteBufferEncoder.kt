package com.psyndicate.kprodatalog.serialization

import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> ByteBuffer.encode(value: T) = this.encode(value, T::class)

@ExperimentalUnsignedTypes
fun <T : Any> ByteBuffer.encode(value: T, klass: KClass<T>): ByteBuffer {
    val constructor = klass.constructors.first()
    constructor.parameters.forEach { kParameter ->
        val property = klass
            .memberProperties
            .find { it.name == kParameter.name }
            ?: error("Unable to find property matching constructor parameter")

        when (kParameter.type) {
            ByteArray::class.createType() -> {
                val size = kParameter
                    .annotations
                    .find { it is SerializedSize }
                    ?.let { (it as SerializedSize).size }
                    ?: error("ByteArray requires SerializedSize annotation")
                val memberValue = (property.get(value) as ByteArray)
                if (size != memberValue.size) {
                    error("Annotated size does not match value")
                }
                put(memberValue)
            }
            Long::class.createType() -> putLong(property.get(value) as Long)
            Int::class.createType() -> putInt(property.get(value) as Int)
            Short::class.createType() -> putShort(property.get(value) as Short)
            Byte::class.createType() -> put(property.get(value) as Byte)
            Boolean::class.createType() -> put(if (property.get(value) as Boolean) 1 else 0)
            UInt::class.createType() -> putInt((property.get(value) as UInt).toInt())
            UShort::class.createType() -> putShort((property.get(value) as UShort).toShort())
            UByte::class.createType() -> put((property.get(value) as UByte).toByte())
            Double::class.createType() -> putDouble(property.get(value) as Double)
            Float::class.createType() -> putFloat(property.get(value) as Float)
            else -> error("Unsupported type ${kParameter.type.javaClass.name} for property ${kParameter.name}")
        }
    }
    return this
}