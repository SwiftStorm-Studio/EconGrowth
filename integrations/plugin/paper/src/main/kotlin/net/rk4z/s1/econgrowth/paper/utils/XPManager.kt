package net.rk4z.s1.econgrowth.paper.utils

import net.minecraft.world.entity.animal.Animal
import net.rk4z.s1.econgrowth.paper.EconGrowth
import org.bukkit.block.Block
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Wither
import java.util.*

object XPManager {
    private val random = Random()
    private val dataBase = EconGrowth.EGDB

    // ブロック破壊時に付与するXPを計算する
    fun calculateXPForBlockBreak(
        player: Player,
        block: Block,
        isPlayerPlacedBlock: Boolean
    ): Float {
        val uuid = player.uniqueId.toString()
        val playerLevel = dataBase.getLevel(uuid)

        if (isPlayerPlacedBlock || playerLevel == 1200) {
            // プレイヤーが設置したブロックはXPを付与しない
            return 0.0f
        }

        // 基礎XPを指数的に成長させる
        val baseXP = (xpMap[playerLevel]!! * (0.01f + random.nextFloat() * 0.06f)) * 0.6f

        // ボーナス計算
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

    // TODO: ギルド導入時に、戦争状態以外でのPVP時にXP付与を無効化するか、何らかのデメリットを付与
    // エンティティを倒した時に付与するXPを計算する
    fun calculateXPForKillEntity(
        player: Player,
        entity: Entity
    ): Float {
        val playerUUID = player.uniqueId.toString()
        val killedPlayerUUID = entity.uniqueId.toString()
        val playerLevel = dataBase.getLevel(playerUUID)
        val killedPlayerLevel = if (entity is Player) dataBase.getLevel(killedPlayerUUID) else 0

        val excludedEntities = setOf(
            EntityType.ARMOR_STAND,
            EntityType.ITEM_FRAME,
            EntityType.LEASH_KNOT,
            EntityType.PAINTING
        )

        if (
            playerLevel == 1200 ||
            entity.type in excludedEntities
        ) {
            return 0.0f
        }

        val baseXP = xpMap[playerLevel]!! * (0.01f + random.nextFloat() * 0.04f) * 0.8f

        val levelDiffFactor = if (entity is Player) {
            val levelDiff = playerLevel - killedPlayerLevel
            when {
                levelDiff < 0 -> 1.2f + (-levelDiff / 40.0f).coerceIn(0.0f, 2.0f).toFloat()
                else -> 1.0f + (levelDiff / 50.0f).coerceIn(0.0f, 1.5f).toFloat()
            }
        } else {
            1.0f
        }

        val mobTypeFactor = when (entity) {
            is Animal -> 0.1f
            is EnderDragon -> 10.0f
            is Wither -> 8.0f
            else -> 1.0f
        }

        val bonus = {
            val randomFactor = when (random.nextInt(10)) {
                in 0..5 -> random.nextFloat() * 0.3f + 1.1f
                else -> random.nextFloat() * 0.5f + 1.5f
            }
            randomFactor * levelDiffFactor * mobTypeFactor
        }

        val finalXP = (baseXP * bonus()).coerceAtMost(xpMap[playerLevel]!! * 0.15f)
        return finalXP.truncateToSecondDecimal()
    }
}