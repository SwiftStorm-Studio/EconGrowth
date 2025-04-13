@file:Suppress("unused")

package net.ririfa.econgrowth.paper.utils

import net.minecraft.server.level.ServerPlayer
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

data class ChangeInfo(
    val table: Table,
    val affectedColumns: List<Column<*>>
)

fun Float.truncateToSecondDecimal(): Float {
    return kotlin.math.floor(this * 100) / 100.0f
}

fun Entity.isPlayer(): Boolean {
    return this is Player
}

fun Entity.getPlayer(): Player? {
    return this as? Player
}

fun Player.toNMSPlayer(): ServerPlayer? {
    return (this as? CraftPlayer)?.handle
}

fun ServerPlayer.toPaperPlayer(): CraftPlayer {
    return this.bukkitEntity
}

fun Material.isDeepSlate(): Boolean {
    // あんまりよくないけど現状は全深層系ブロックが名前にDEEPSLATEを含むのでこれで判定
    return this.name.contains("DEEPSLATE", ignoreCase = true)
}