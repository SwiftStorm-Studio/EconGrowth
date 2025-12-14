package dev.swiftstorm.econgrowth.core

import dev.swiftstorm.akkaradb.common.ByteBufferL
import dev.swiftstorm.akkaradb.common.ShortUUID
import dev.swiftstorm.akkaradb.common.binpack.AdapterRegistry
import dev.swiftstorm.akkaradb.common.binpack.TypeAdapter
import dev.swiftstorm.econgrowth.core.logging.EconGrowthLogger
import java.util.UUID

class EconGrowthCore(
    val logger: EconGrowthLogger
) {
    companion object {
        lateinit var logger: EconGrowthLogger
    }

    init {
        EconGrowthCore.logger = logger
    }

    fun registerAdapters() {
        AdapterRegistry.register<ShortUUID>(shortUUIDAdapter)
    }

    private val shortUUIDAdapter = object : TypeAdapter<ShortUUID> {
        override fun estimateSize(value: ShortUUID): Int = 16

        override fun write(value: ShortUUID, buffer: ByteBufferL) {
            buffer.i64 = value.uuid.mostSignificantBits
            buffer.i64 = value.uuid.leastSignificantBits
        }

        override fun read(buffer: ByteBufferL): ShortUUID {
            val msb = buffer.i64
            val lsb = buffer.i64
            return ShortUUID.fromUUID(UUID(msb, lsb))
        }
    }
}