package net.rk4z.s1.swiftbase.paper

import net.rk4z.s1.swiftbase.core.IPlayer
import org.bukkit.entity.Player

class PaperPlayerAdapter(private val player: Player) : IPlayer {
    override fun getLanguage(): String {
        return player.locale().language ?: "en"
    }
}

fun Player.adapt(): IPlayer {
    return PaperPlayerAdapter(this)
}