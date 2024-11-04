package net.rk4z.s1.econgrowth.utils

import net.minecraft.server.level.ServerPlayer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

fun Entity.isPlayer(): Boolean {
    return this is Player
}

fun Entity.getPlayer(): Player? {
    return this as? Player
}

fun Player.toNMSPlayer(): ServerPlayer? {
    return (this as? CraftPlayer)?.handle
}