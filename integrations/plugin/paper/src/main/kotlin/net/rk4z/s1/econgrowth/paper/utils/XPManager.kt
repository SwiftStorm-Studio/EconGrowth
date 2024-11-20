package net.rk4z.s1.econgrowth.paper.utils

import net.rk4z.s1.econgrowth.paper.EconGrowth
import org.bukkit.event.block.BlockBreakEvent
import java.util.UUID

object XPManager {
    fun calculateXP(
        uuid: UUID,
        isPlayerPlacedBlock: Boolean,
        event: BlockBreakEvent
    ): Float {
        val us = uuid.toString()

        val dataBase = EconGrowth.EGDB

        if (isPlayerPlacedBlock) {
            return 0.0f
        }

        val playerLevel = dataBase.getLevel(us)
        val block = event.block
        val bh = block.type.hardness
        val idp = block.type.isDeepSlate()

        when (playerLevel) {
            in 0..10 -> { // 0 から 10 の間
                return if (bh < 1.0f) {
                    1.0f
                } else {
                    0.0f
                }
            }
            else -> {
                return 0.0f
            }
        }

    }
}