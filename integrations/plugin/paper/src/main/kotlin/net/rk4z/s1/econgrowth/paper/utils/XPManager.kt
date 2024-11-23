package net.rk4z.s1.econgrowth.paper.utils

import net.rk4z.s1.econgrowth.paper.EconGrowth
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import java.util.*
import kotlin.math.pow

object XPManager {
    private val random = Random()

    // ブロック破壊時に付与するXPを計算する
    fun calculateXPForBlockBreak(
        player: Player,
        isPlayerPlacedBlock: Boolean,
        event: BlockBreakEvent
    ): Float {
        val uuid = player.uniqueId.toString()
        val dataBase = EconGrowth.EGDB
        val playerLevel = dataBase.getLevel(uuid)

        if (isPlayerPlacedBlock || playerLevel == 1200) {
            return 0.0f
        }

        // 基礎XPを指数的に成長させる
        val baseXP = (xpMap[playerLevel]!! * (0.01f + random.nextFloat() * 0.04f)) * 0.9f

        // ボーナス計算
        val block = event.block
        val hardness = block.type.hardness
        val deepslate = block.type.isDeepSlate()
        val bonus = {
            val randomFactor = when (random.nextInt(10)) {
                in 0..5 -> random.nextFloat() * 0.3f + 1.1f
                else -> random.nextFloat() * 0.5f + 1.5f
            }
            val adjustedHardness = hardness.coerceIn(1.0f, 1.75f)
            if (deepslate) {
                randomFactor * adjustedHardness * 1.5f
            } else {
                randomFactor * adjustedHardness
            }
        }

        // 最終XP計算
        val finalXP = baseXP * bonus()
        return finalXP.truncateToSecondDecimal()
    }
}