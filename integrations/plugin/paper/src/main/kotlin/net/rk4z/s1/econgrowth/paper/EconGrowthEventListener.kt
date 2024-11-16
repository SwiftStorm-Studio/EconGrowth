package net.rk4z.s1.econgrowth.paper

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

@Suppress("DuplicatedCode")
class EconGrowthEventListener : Listener {
    val dataBase = EconGrowth.dataBase

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

    }
}