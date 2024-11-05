package net.rk4z.s1.econgrowth.xp

import org.bukkit.entity.Player

data class ActionDetail(
    val player: Player,
    val type: ActionType
)