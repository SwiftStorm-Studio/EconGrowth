package dev.swiftstorm.econgrowth.core

import dev.swiftstorm.akkaradb.common.ByteBufferL
import dev.swiftstorm.akkaradb.common.ShortUUID
import dev.swiftstorm.akkaradb.common.binpack.AdapterRegistry
import dev.swiftstorm.akkaradb.common.binpack.TypeAdapter
import dev.swiftstorm.econgrowth.core.database.DataBase
import dev.swiftstorm.econgrowth.core.logging.EconGrowthLogger
import java.nio.file.Path
import java.util.UUID

class EconGrowthCore(
    val logger: EconGrowthLogger
) {
    companion object {
        lateinit var logger: EconGrowthLogger
        lateinit var dataBase: DataBase
    }

    init {
        EconGrowthCore.logger = logger
    }

    fun registerAdapters(): EconGrowthCore {
        AdapterRegistry.register<ShortUUID>(shortUUIDAdapter)
        return this
    }

    fun initializeDataBase(dataFolder: Path, backupMaxSize: Int): EconGrowthCore {
        dataBase = DataBase(dataFolder, backupMaxSize)
        return this
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