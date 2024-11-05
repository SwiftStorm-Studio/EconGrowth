package net.rk4z.s1.econgrowth.xp

import org.bukkit.Material
import org.bukkit.entity.EntityType

sealed class ActionType {
    data class Mining(val target: Material) : ActionType()
    data class Killing(val target: EntityType) : ActionType()
}