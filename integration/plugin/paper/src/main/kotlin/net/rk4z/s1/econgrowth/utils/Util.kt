package net.rk4z.s1.econgrowth.utils

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

//TODO: If anything is missing, please add it in the PR.
val deepSlateList = listOf(
    Material.DEEPSLATE,

    Material.COBBLED_DEEPSLATE,
    Material.POLISHED_DEEPSLATE,
    Material.CRACKED_DEEPSLATE_BRICKS,
    Material.CRACKED_DEEPSLATE_TILES,
    Material.CHISELED_DEEPSLATE,

    Material.DEEPSLATE_BRICKS,
    Material.DEEPSLATE_TILES,
    Material.DEEPSLATE_BRICK_STAIRS,
    Material.DEEPSLATE_BRICK_SLAB,
    Material.DEEPSLATE_BRICK_WALL,
    Material.DEEPSLATE_TILE_STAIRS,
    Material.DEEPSLATE_TILE_SLAB,
    Material.DEEPSLATE_TILE_WALL,

    Material.POLISHED_DEEPSLATE_STAIRS,
    Material.POLISHED_DEEPSLATE_SLAB,
    Material.POLISHED_DEEPSLATE_WALL,

    Material.DEEPSLATE_COAL_ORE,
    Material.DEEPSLATE_IRON_ORE,
    Material.DEEPSLATE_COPPER_ORE,
    Material.DEEPSLATE_GOLD_ORE,
    Material.DEEPSLATE_REDSTONE_ORE,
    Material.DEEPSLATE_EMERALD_ORE,
    Material.DEEPSLATE_LAPIS_ORE,
    Material.DEEPSLATE_DIAMOND_ORE
)

fun Entity.isPlayer(): Boolean {
    return this is Player
}

fun Entity.getPlayer(): Player? {
    return this as? Player
}

fun Player.toNMSPlayer(): ServerPlayer? {
    return (this as? CraftPlayer)?.handle
}

fun Material.getBlockHardness(): Float {
    val nmsBlock: Block = CraftMagicNumbers.getBlock(this)
    return nmsBlock.defaultBlockState().destroySpeed
}

fun Material.isDeepSlate(): Boolean {
    return this in deepSlateList
}