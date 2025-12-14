package dev.swiftstorm.econgrowth.fabric

import dev.swiftstorm.econgrowth.core.EconGrowthCore
import dev.swiftstorm.econgrowth.fabric.logging.FabricEconGrowthLogger
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

class EconGrowthFabric : ModInitializer {
    companion object {
        const val MOD_ID = "econgrowth"

        val loader: FabricLoader = FabricLoader.getInstance()
        val serverDir: Path = loader.gameDir
        val modDir: Path = serverDir.resolve(MOD_ID)

        val logger: Logger = LoggerFactory.getLogger(EconGrowthCore::class.simpleName)
    }

    override fun onInitialize() {
        EconGrowthCore(FabricEconGrowthLogger)
            .registerAdapters()
            .initializeDataBase(modDir, 10)
    }
}